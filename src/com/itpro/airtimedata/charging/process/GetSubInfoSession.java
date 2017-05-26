/**
 * 
 */
package com.itpro.airtimedata.charging.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.itpro.airtimedata.charging.structs.SubInfo;
import com.itpro.log4j.ITProLog4jCategory;
import com.itpro.util.ProcessingThread;
import com.itpro.util.Queue;

/**
 * @author ducgv
 *
 */
public class GetSubInfoSession extends ProcessingThread {
	public static final int FIELD_RESP_CODE = 0;
	public static final int FIELD_PRE_SUB_ID = 1;
	public static final int FIELD_PRE_SUB_TYPE = 2;
	public static final int FIELD_PRE_IN_ISDN = 3;
	public static final int FIELD_PRE_IN_STATUS = 4;
	public static final int FIELD_PRE_IN_ACT_STATUS = 5;
	public static final int FIELD_PRE_PRODUCT_CODE = 6;
	public static final int FIELD_PRE_GENERATION = 7;
	public static final int FIELD_PRE_ACTIVE_DATE = 8;
	public static final int FIELD_PRE_BASIC_ACCOUNT = 9;
	public static final int FIELD_PRE_PROMOTION_ACCOUNT = 10;
	public static final int FIELD_PRE_FREE_CALL_ACCOUNT = 11;
	public static final int FIELD_PRE_FREE_SMS_ACCOUNT = 12;
	public static final int FIELD_PRE_FREE_DATA1 = 13;
	public static final int FIELD_PRE_UNKNOWN2 = 14;
	public static final int FIELD_PRE_UNKNOWN3 = 15;
	public static final int FIELD_PRE_UNKNOWN4 = 16;
	public static final int FIELD_PRE_FREE_DATA2 = 17;
	
	public static final int FIELD_POS_SUB_ID = 1;
	public static final int FIELD_POS_SUB_TYPE = 2;
	public static final int FIELD_POS_ISDN = 3;
	public static final int FIELD_POS_STATUS = 4;
	public static final int FIELD_POS_ACT_STATUS = 5;
	public static final int FIELD_POS_PRODUCT_CODE = 6;
	public static final int FIELD_POS_GENERATION = 7;
	public static final int FIELD_POS_FIRST_CONNECT = 8;
	public static final int FIELD_POS_CUS_ID = 9;
	
	private String msisdn;
	public GetSubInfoSession(String msisdn, Queue queueResp, ITProLog4jCategory logger) {
		// TODO Auto-generated constructor stub
		this.msisdn = msisdn;
		this.queueResp = queueResp;
		this.logger = logger;
	}
	
	private Queue queueResp = null;
	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#OnHeartBeat()
	 */
	@Override
	protected void OnHeartBeat() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#initialize()
	 */
	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		setLogPrefix("[GetSubInfoSession] ");
	}

	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#process()
	 */
	@Override
	protected void process() {
		// TODO Auto-generated method stub
		SubInfo subInfo = new SubInfo();
		if(msisdn.startsWith("856"))
			subInfo.msisdn = msisdn.replaceFirst("856", "");
		else
			subInfo.msisdn = msisdn;
		
		logInfo("Req GetSubInfo: msisdn:"+subInfo.msisdn);
		
		String cmd = "/opt/itpro/airTimeData/scripts/getSubInfo.php " + subInfo.msisdn;

		Runtime run = Runtime.getRuntime();
		Process pr = null;
		try {
			pr = run.exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			subInfo.respCode = -3;
			subInfo.respString = e.getMessage();
			queueResp.enqueue(subInfo);
			logError("Resp "+subInfo.toString());
			stop();
			return;
		}
		try {
			pr.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			subInfo.respCode = -3;
			subInfo.respString = e.getMessage();
			queueResp.enqueue(subInfo);
			logError("Resp "+subInfo.toString());
			stop();
			return;
		}
		BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String result = "";
		String line;
		try {
			while ((line = buf.readLine()) != null) {
				result += line;
			}
			buf.close();
			//logInfo("getSubInfo(" + msisdn + "):" + result);
			subInfo.detail = result;
			String[] arrResult = result.split("[|]");
			if(arrResult[0].equals("11")||arrResult[0].equals("22")){
				String activeDateString;
				if(arrResult[0].equals("11")){
					subInfo.respCode = 11;
					subInfo.subType = 1;
					subInfo.subId = Integer.parseInt(arrResult[FIELD_PRE_SUB_ID]);
					subInfo.subProductCode = arrResult[FIELD_PRE_PRODUCT_CODE];
					subInfo.isActive = ((arrResult[FIELD_PRE_IN_STATUS].equals("1")||arrResult[FIELD_PRE_IN_STATUS].equals("2")) && arrResult[FIELD_PRE_IN_ACT_STATUS].equals("00"))?true:false;
					activeDateString = arrResult[FIELD_PRE_ACTIVE_DATE];
					try{
						subInfo.balance = (int)Double.parseDouble(arrResult[FIELD_PRE_BASIC_ACCOUNT]);
					}catch (java.lang.NumberFormatException e) {
						subInfo.balance = 0;
					}
					try{
						subInfo.freeData1 = (int)Double.parseDouble(arrResult[FIELD_PRE_FREE_DATA1]);
					}catch (java.lang.NumberFormatException e) {
						subInfo.freeData1 = 0;
					}
					try{
						subInfo.freeData2 = (int)Double.parseDouble(arrResult[FIELD_PRE_FREE_DATA2]);
					}catch (java.lang.NumberFormatException e) {
						subInfo.freeData2 = 0;
					}
				}
				else{ //arrResult[0].equals("22")
					subInfo.respCode = 22;
					subInfo.subType = 2;
					subInfo.isActive = ((arrResult[FIELD_POS_STATUS].equals("1")||arrResult[FIELD_POS_STATUS].equals("2")) && arrResult[FIELD_POS_ACT_STATUS].equals("00"))?true:false;
					activeDateString = arrResult[FIELD_POS_FIRST_CONNECT];
				}
				DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
				try {
					Date activeDate = formatter.parse(activeDateString);
					Date currentDateTime = new Date(System.currentTimeMillis());
					String currentDateString = formatter.format(currentDateTime);
					Date currentDate = formatter.parse(currentDateString);
					subInfo.activeDate = new java.sql.Date(activeDate.getTime());
					//d.getTime();
					subInfo.numDaysActive = (int)((currentDate.getTime() - activeDate.getTime())/86400000); //86,400,000 = 24h * 3600s * 1000ms
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					subInfo.respCode = -3;
					subInfo.respString = e.getMessage();
					queueResp.enqueue(subInfo);
					logError("Resp "+subInfo.toString());
					stop();
					return;
				}
				queueResp.enqueue(subInfo);
				logInfo("Resp "+subInfo.toString());
				stop();
				return;
			}
			else if(arrResult.length==2){
				subInfo.respCode = Integer.parseInt(arrResult[FIELD_RESP_CODE]);
				subInfo.respString = arrResult[1];
				queueResp.enqueue(subInfo);
				logError("Resp "+subInfo.toString());
				stop();
				return;
			}
			else{
				subInfo.respCode = -1;
				subInfo.respString = arrResult[0];
				queueResp.enqueue(subInfo);
				logError("Resp "+subInfo.toString());
				stop();
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			subInfo.respCode = -3;
			subInfo.respString = e.getMessage();
			queueResp.enqueue(subInfo);
			logError("Resp "+subInfo.toString());
			stop();
			return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			subInfo.respCode = -3;
			subInfo.respString = e.getMessage();
			queueResp.enqueue(subInfo);
			logError("Resp "+subInfo.toString());
			stop();
			return;
		}
	}
}
