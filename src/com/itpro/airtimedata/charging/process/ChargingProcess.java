/**
 * 
 */
package com.itpro.airtimedata.charging.process;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import com.itpro.airtimedata.charging.cli.CLICmd;
import com.itpro.airtimedata.charging.db.DbConnection;
import com.itpro.airtimedata.charging.main.Config;
import com.itpro.airtimedata.charging.main.GlobalVars;
import com.itpro.airtimedata.charging.structs.CDRRecord;
import com.itpro.airtimedata.charging.structs.ChargingCmd;
import com.itpro.airtimedata.charging.structs.InsertCDRCmd;
import com.itpro.airtimedata.charging.structs.MTRecord;
import com.itpro.airtimedata.charging.structs.OfferRecord;
import com.itpro.airtimedata.charging.structs.RechargeEventRecord;
import com.itpro.airtimedata.charging.structs.SmsTypes;
import com.itpro.airtimedata.charging.structs.SubInfo;
import com.itpro.airtimedata.charging.structs.UpdateOfferRecordCmd;
import com.itpro.airtimedata.charging.util.DataPackageDisplay;
import com.itpro.cli.CmdRequest;
import com.itpro.util.Params;
import com.itpro.util.ProcessingThread;
import com.itpro.util.Queue;

/**
 * @author Giap Van Duc
 *
 */
public class ChargingProcess extends ProcessingThread {
	DbConnection connection = null;
	public boolean isConnected = false;
	private long nextTimeGetRechargeEvents = System.currentTimeMillis();
	public Queue queueCmdCLIRequest = new Queue();
	Vector<RechargeEventRecord> rechargeEventRecords = new Vector<RechargeEventRecord>();
	Hashtable<String, ChargingCmd> listChargeCmdProcessing = new Hashtable<String, ChargingCmd>();
	private long lastTime;	
	public static boolean isExceed = false;
	public static int chargingTps = 0;	
	public static int concurrent = 0;
	public int rechargeEventProcessingCount;
	public Queue queueChargingCmdResp = new Queue();
	public Queue queueUpdateOfferRecordResp = new Queue();
	public Queue queueInsertCDRRecordResp = new Queue();
	public Queue queueUpdateRechargeSummaryResp = new Queue();
	private Hashtable<String, OfferRecord> listRequestProcessing = new Hashtable<String, OfferRecord>();
	private Queue queueGetSubInfoResp = new Queue();
	private Vector<RechargeEventRecord> listRechargeEventRecordUpdateFailed = new Vector<RechargeEventRecord>();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.itpro.util.ProcessingThread#OnHeartBeat()
	 */
	@Override
	protected void OnHeartBeat() {
		// TODO Auto-generated method stub
		if (connection == null) {
			Connect();
		} else if (!isConnected) {
			connection.close();
			Connect();
		}
	}

	private void OnConnected() {
		logInfo("Connected to DB");
		try {
			if(!Config.messageContents[Config.LANG_EN].isLoaded)	
				connection.getParams(Config.messageContents[Config.LANG_EN], "msg_content_en");
			if(!Config.messageContents[Config.LANG_LA].isLoaded)
				connection.getParams(Config.messageContents[Config.LANG_LA], "msg_content_la");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logError("Load MessageContents error:" + e.getMessage());
			isConnected = false;
		}

		try {
			if(!Config.serviceConfigs.isLoaded)
				connection.getParams(Config.serviceConfigs, "service_config");

			Config.smsLanguage = Config.LANG_EN;
			String smsLanguage = Config.serviceConfigs.getParam("SMS_LANGUAGE");
			if (smsLanguage.equalsIgnoreCase("LA")){
				Config.smsLanguage = Config.LANG_LA;
			}
			Config.maxChargingConcurrent = Integer.parseInt(Config.serviceConfigs.getParam("MAX_CHARGING_CONCURRENT"));
			Config.maxChargingTPS = Integer.parseInt(Config.serviceConfigs.getParam("MAX_CHARING_TPS"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logError("Load ServiceConfigs error:" + e.getMessage());
			isConnected = false;
		}
		while(!listRechargeEventRecordUpdateFailed.isEmpty()){
			RechargeEventRecord rechargeEventRecord = listRechargeEventRecordUpdateFailed.remove(0);
			try {
				connection.updateRechargeEvent(rechargeEventRecord);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				listRechargeEventRecordUpdateFailed.add(rechargeEventRecord);
				logError("Update "+rechargeEventRecord.toString()+"; error:"+e.getMessage());
				isConnected = false;
				return;
			}
		}
	}

	private void Connect() {
		connection = new DbConnection(Config.dbServerName, Config.dbDatabaseName, Config.dbUserName, Config.dbPassword);
		Exception exception = null;
		try {
			isConnected = connection.connect();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			exception = e;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			exception = e;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			exception = e;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			exception = e;
		}

		if (exception != null) {
			isConnected = false;
			logError("Connect to DB: error:" + exception.getMessage());
		} else {
			OnConnected();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.itpro.util.ProcessingThread#initialize()
	 */
	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		setHeartBeatInterval(5000);
		Connect();
		nextTimeGetRechargeEvents = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.itpro.util.ProcessingThread#process()
	 */
	@Override
	protected void process() {
		// TODO Auto-generated method stub
		CmdRequest cmdRequest = (CmdRequest) queueCmdCLIRequest.dequeue();
		if (cmdRequest != null) {
			OnCliCmdReq(cmdRequest);
		}
		if (connection == null || !isConnected)
			return;
		getRechargeEventRecords();
		UpdateTraffic();

		if (!rechargeEventRecords.isEmpty() && !isExceed){		
			if (concurrent < Config.maxChargingConcurrent) {
				RechargeEventRecord rechargeEventRecord = rechargeEventRecords.remove(0);
				String msisdn = rechargeEventRecord.msisdn.startsWith("856")?rechargeEventRecord.msisdn.replaceFirst("856", ""):rechargeEventRecord.msisdn;
				if(listChargeCmdProcessing.get(msisdn)==null){
					try {
						OfferRecord offerRecord = connection.getUnPaidOfferRecord(msisdn);
						if(offerRecord!=null){
							offerRecord.rechargeEventRecord = rechargeEventRecord;
							offerRecord.rechargeEventRecord.status = 3; //default
							if(listRequestProcessing.get(msisdn)==null){
								listRequestProcessing.put(msisdn, offerRecord);
								GetSubInfoSession getSubInfoSession = new GetSubInfoSession(msisdn, queueGetSubInfoResp , logger);
								getSubInfoSession.start();
							}
							else{
								rechargeEventRecord.status = 3;
								finishProcessRechargeEventRecord(rechargeEventRecord);
							}
						}
						else{
							rechargeEventRecord.status = 3;
							finishProcessRechargeEventRecord(rechargeEventRecord);
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						logError("Get UnpaidOfferRecord for msisdn:"+msisdn+"; error:"+e.getMessage());
						rechargeEventRecords.add(0, rechargeEventRecord);
						isConnected = false;
						return;
					}

				}
				else{
					rechargeEventRecord.status = 3;
					finishProcessRechargeEventRecord(rechargeEventRecord);
				}
			}
		}
		
		ChargingCmd chargingCmdResp = (ChargingCmd) queueChargingCmdResp.dequeue();
		if(chargingCmdResp!=null){
			OnChargingCmdResp(chargingCmdResp);
		}
		UpdateOfferRecordCmd updateOfferRecordCmdResp = (UpdateOfferRecordCmd) queueUpdateOfferRecordResp.dequeue();
		if(updateOfferRecordCmdResp!=null){
			OnUpdateOfferRecordCmdResp(updateOfferRecordCmdResp);
		}
		InsertCDRCmd insertCDRCmdResp = (InsertCDRCmd) queueInsertCDRRecordResp.dequeue();
		if(insertCDRCmdResp!=null){
			OnInsertCDRCmdResp(insertCDRCmdResp);
		}
		SubInfo subInfoResp = (SubInfo) queueGetSubInfoResp.dequeue();
		if(subInfoResp!=null){
			OnGetSubInfoResp(subInfoResp);
		}
	}


	private void OnGetSubInfoResp(SubInfo subInfoResp) {
		// TODO Auto-generated method stub
		OfferRecord offerRecord = listRequestProcessing.get(subInfoResp.msisdn);
		if(subInfoResp.respCode == 11 || subInfoResp.respCode == 22){
			if(offerRecord.sub_id!=subInfoResp.subId){
				offerRecord.charge_date = new Timestamp(System.currentTimeMillis());
				offerRecord.charge_status = OfferRecord.OFFER_CHARGE_STATUS_BAD_DEBIT;
				offerRecord.charge_result_code = subInfoResp.subId;
				offerRecord.charge_result_string = "The subscriber has changed sub_id";
				UpdateOfferRecordCmd updateOfferCmd = new UpdateOfferRecordCmd(offerRecord, queueUpdateOfferRecordResp);
				updateOfferRecord(updateOfferCmd);
			}
			else{
				ChargingCmd chargingCmd = new ChargingCmd(offerRecord);
				if(chargingCmd.chargeValue<=subInfoResp.balance){
					listChargeCmdProcessing.put(offerRecord.msisdn, chargingCmd);
					ChargingSession chargingSession = new ChargingSession(chargingCmd, queueChargingCmdResp, logger);
					chargingSession.start();
					chargingTps++;
					concurrent++;
				}
				else{
					listChargeCmdProcessing.put(offerRecord.msisdn, chargingCmd);
					chargingCmd.charge_date = new Timestamp(System.currentTimeMillis());
					chargingCmd.resultCode = 155;
					chargingCmd.resultString = "The subscriber does not have enough balance";
					queueChargingCmdResp.enqueue(chargingCmd);
				}
			}
		}
		else{
			listRequestProcessing.remove(offerRecord.msisdn);
			rechargeEventRecords.add(offerRecord.rechargeEventRecord);
			logError("Putback to re-check getSubInfo later, msisdn:"+offerRecord.msisdn);
		}
	}

	private void OnInsertCDRCmdResp(InsertCDRCmd insertCDRCmdResp) {
		// TODO Auto-generated method stub
		ChargingCmd chargingCmdResp = listChargeCmdProcessing.get(insertCDRCmdResp.cdrRecord.msisdn);
		if(insertCDRCmdResp.resultCode==InsertCDRCmd.RESULT_OK){
			listChargeCmdProcessing.remove(insertCDRCmdResp.cdrRecord.msisdn);
			if(chargingCmdResp.resultCode == ChargingCmd.RESULT_OK){
				UpdateOfferRecordCmd updateOfferCmd = new UpdateOfferRecordCmd(chargingCmdResp.offerRecord, queueUpdateOfferRecordResp);
				updateOfferRecord(updateOfferCmd);
			}
			else{
				listRequestProcessing.remove(insertCDRCmdResp.cdrRecord.msisdn);
				RechargeEventRecord rechargeEventRecord = chargingCmdResp.offerRecord.rechargeEventRecord;
				finishProcessRechargeEventRecord(rechargeEventRecord);
			}
			
		}
		else{
			//re-insert when error
			insertCDRCmd(chargingCmdResp);
		}
	}

	private void OnUpdateOfferRecordCmdResp(UpdateOfferRecordCmd updateOfferRecordCmdResp) {
		// TODO Auto-generated method stub
		if(updateOfferRecordCmdResp.resultCode==UpdateOfferRecordCmd.RESULT_OK){
			listRequestProcessing.remove(updateOfferRecordCmdResp.offerRecord.msisdn);
			RechargeEventRecord rechargeEventRecord = updateOfferRecordCmdResp.offerRecord.rechargeEventRecord;
			finishProcessRechargeEventRecord(rechargeEventRecord);
		}
		else{
			//re-update when error
			updateOfferRecord(updateOfferRecordCmdResp);
		}
	}

	private void insertCDRCmd(ChargingCmd chargingCmdResp) {
		// TODO Auto-generated method stub
		CDRRecord cdrRecord = new CDRRecord();
		cdrRecord.msisdn = chargingCmdResp.offerRecord.msisdn;
		cdrRecord.sub_product_code = chargingCmdResp.offerRecord.sub_product_code;
		cdrRecord.sub_id = chargingCmdResp.offerRecord.sub_id;
		cdrRecord.date_time = chargingCmdResp.charge_date;
		cdrRecord.offer_id = chargingCmdResp.offerRecord.offer_id;
		cdrRecord.offer_type = chargingCmdResp.offerRecord.offer_type;
		cdrRecord.package_name = chargingCmdResp.offerRecord.package_name;
		cdrRecord.package_price = chargingCmdResp.offerRecord.package_price;
		cdrRecord.service_fee = chargingCmdResp.offerRecord.package_service_fee;
		cdrRecord.charge_value = chargingCmdResp.offerRecord.package_price+chargingCmdResp.offerRecord.package_service_fee;
		cdrRecord.result_code = chargingCmdResp.resultCode;
		cdrRecord.result_string = chargingCmdResp.resultString;
		cdrRecord.status = chargingCmdResp.resultCode == 0? CDRRecord.CHARGE_SUCCESS:CDRRecord.CHARGE_FAILED;
		InsertCDRCmd insertCDRCmd = new InsertCDRCmd(cdrRecord, queueInsertCDRRecordResp);
		insertCDRCmd.cdrRecord = cdrRecord;
		GlobalVars.cdrTableAccess.queueInsertCDRRecordReq.enqueue(insertCDRCmd);
	}

	private void OnChargingCmdResp(ChargingCmd chargingCmdResp) {
		// TODO Auto-generated method stub
		concurrent--;
		if(chargingCmdResp.resultCode==ChargingCmd.RESULT_OK){
			String content = Config.messageContents[Config.smsLanguage].getParam("CONTENT_CHARGE_SUCCESS");
			content = content.replaceAll("<CHARGE_VALUE>", DataPackageDisplay.getNumberString(chargingCmdResp.chargeValue));
			content = content.replaceAll("<DATA_AMOUNT>", DataPackageDisplay.getDataAmountString(chargingCmdResp.offerRecord.package_data_amount));
			content = content.replaceAll("<BORROW_DATE>", (new Date(chargingCmdResp.offerRecord.req_date.getTime()).toString()));
			sendSms("856"+chargingCmdResp.offerRecord.msisdn, content, SmsTypes.CHARGING, chargingCmdResp.offerRecord.offer_id);
			chargingCmdResp.offerRecord.charge_status = OfferRecord.OFFER_CHARGE_STATUS_PAID;
			chargingCmdResp.offerRecord.charge_date = chargingCmdResp.charge_date;
			chargingCmdResp.offerRecord.charge_result_code = chargingCmdResp.resultCode;
			chargingCmdResp.offerRecord.charge_result_string = chargingCmdResp.resultString;
			chargingCmdResp.offerRecord.rechargeEventRecord.status = 2;
		}
		
		if(chargingCmdResp.resultCode == ChargingCmd.RESULT_OK||chargingCmdResp.resultCode==55){
			if(GlobalVars.isChargingError){
				GlobalVars.isChargingError = false;
				GlobalVars.lastTimeChargingError = System.currentTimeMillis();
				connection.updateWarningChargingError(false);
			}
			
		}else if(chargingCmdResp.resultCode!=155){
			if(GlobalVars.isChargingError == false){
				GlobalVars.isChargingError = true;
				GlobalVars.lastTimeChargingError = System.currentTimeMillis();
			}
			else if(GlobalVars.lastTimeChargingError + 15*60000 < System.currentTimeMillis()){
				GlobalVars.lastTimeChargingError = System.currentTimeMillis();
				connection.updateWarningChargingError(true);
			}
			
		}
		
		insertCDRCmd(chargingCmdResp);
	}

	private void updateOfferRecord(UpdateOfferRecordCmd updateOfferCmd) {
		// TODO Auto-generated method stub
		GlobalVars.offerTableAccess.queueUpdateOfferRecordReq.enqueue(updateOfferCmd);
	}

	private void finishProcessRechargeEventRecord(RechargeEventRecord rechargeEventRecord) {
		// TODO Auto-generated method stub
		rechargeEventProcessingCount--;
		try {
			connection.updateRechargeEvent(rechargeEventRecord);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logError("Update "+rechargeEventRecord.toString()+"; error:"+e.getMessage());
			listRechargeEventRecordUpdateFailed.add(rechargeEventRecord);
			isConnected = false;
		}
	}
	
	private void getRechargeEventRecords() {
		if(GlobalVars.stopModuleFlag)
			return;
		// TODO Auto-generated method stub
		if(rechargeEventProcessingCount>0||!rechargeEventRecords.isEmpty())
			return;
		long curTime = System.currentTimeMillis();
		if(rechargeEventRecords.isEmpty()){
			if(nextTimeGetRechargeEvents<curTime){
				try {
					rechargeEventRecords = connection.getRechargeEventRecords();
					rechargeEventProcessingCount = rechargeEventRecords.size();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logError("Get RechargeEventRecords error:"+e.getMessage());
					isConnected = false;
					rechargeEventRecords = new Vector<RechargeEventRecord>();
					rechargeEventProcessingCount = 0;
				}									
				if(rechargeEventRecords.isEmpty()){
					nextTimeGetRechargeEvents = curTime + 1000;						
				}
				else {						
					nextTimeGetRechargeEvents = curTime;
				}
			}
		}
	}

	private void OnCliCmdReq(CmdRequest cmdRequest) {
		// TODO Auto-generated method stub
		if(cmdRequest.cmd.equalsIgnoreCase("Reload")){
			if (cmdRequest.params.get("target").equalsIgnoreCase(CLICmd.SERVICE_CONFIG)) {
				if (connection != null && isConnected) {
					try {
						connection.getParams(Config.serviceConfigs, "service_config");

						Config.smsLanguage = Config.LANG_EN;
						String smsLanguage = Config.serviceConfigs.getParam("SMS_LANGUAGE");
						if (smsLanguage.equalsIgnoreCase("LA")){
							Config.smsLanguage = Config.LANG_LA;
						}
						Config.maxChargingConcurrent = Integer.parseInt(Config.serviceConfigs.getParam("MAX_CHARGING_CONCURRENT"));
						Config.maxChargingTPS = Integer.parseInt(Config.serviceConfigs.getParam("MAX_CHARING_TPS"));
						cmdRequest.result.put("Result", "success");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						logError("Load ServiceConfigs error" + e.getMessage());
						isConnected = false;
						cmdRequest.result.put("Result", "failed");
						cmdRequest.result.put("Error", e.getMessage());
					}
				} else {
					cmdRequest.result.put("Result", "failed");
					cmdRequest.result.put("Error", "Not connected to DB");
				}
				cmdRequest.queueResp.enqueue(cmdRequest);
			} else if (cmdRequest.params.get("target").equalsIgnoreCase(CLICmd.MESSAGE_CONTENTS)) {
				if (connection != null && isConnected) {
					try {
						Config.messageContents[Config.LANG_EN] = new Params();
						Config.messageContents[Config.LANG_LA] = new Params();
						connection.getParams(Config.messageContents[Config.LANG_EN], "msg_content_en");
						connection.getParams(Config.messageContents[Config.LANG_LA], "msg_content_la");
						cmdRequest.result.put("Result", "success");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						logError("Load MessageContents error:" + e.getMessage());
						isConnected = false;
						cmdRequest.result.put("Result", "failed");
						cmdRequest.result.put("Error", e.getMessage());
					}

				} else {
					cmdRequest.result.put("Result", "failed");
					cmdRequest.result.put("Error", "Not connected to DB");
				}
				cmdRequest.queueResp.enqueue(cmdRequest);
			}
		}
	}
	
	private void sendSms(String msisdn, String content, int sms_type, int offer_id) {
		// TODO Auto-generated method stub
		MTRecord mtRecord = new MTRecord(msisdn, content, sms_type, offer_id);
		GlobalVars.smsMTTableAccess.queueInsertMTReq.enqueue(mtRecord);
	}

	private void UpdateTraffic(){
		long curTime=System.currentTimeMillis();
		if(curTime-lastTime>=1000){
			lastTime=curTime;
			chargingTps  = 0;
		}		
		int maxMsgPerSecond = Config.maxChargingTPS;
		isExceed = (chargingTps>=maxMsgPerSecond)?true:false;
	}
}	


