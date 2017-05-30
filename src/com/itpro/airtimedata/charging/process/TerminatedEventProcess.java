/**
 * 
 */
package com.itpro.airtimedata.charging.process;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;

import com.itpro.airtimedata.charging.db.DbConnection;
import com.itpro.airtimedata.charging.main.Config;
import com.itpro.airtimedata.charging.main.GlobalVars;
import com.itpro.airtimedata.charging.structs.OfferRecord;
import com.itpro.airtimedata.charging.structs.TerminatedEvent;
import com.itpro.airtimedata.charging.structs.UpdateOfferRecordCmd;
import com.itpro.util.ProcessingThread;
import com.itpro.util.Queue;

/**
 * @author Giap Van Duc
 *
 */
public class TerminatedEventProcess extends ProcessingThread {
	DbConnection connection = null;
	private Vector<TerminatedEvent> terminatedEvents = new Vector<TerminatedEvent>();
	private Hashtable<String, TerminatedEvent> listTerminatedEventProcessing = new Hashtable<String, TerminatedEvent>();
	public boolean isConnected = false;
	private long nextTime;
	private Queue queueUpdateOfferRecordResp = new Queue();
	private Vector<TerminatedEvent> listUpdateTerminatedFailed = new Vector<TerminatedEvent>();
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
		while(!listUpdateTerminatedFailed.isEmpty()){
			TerminatedEvent terminatedEvent = listUpdateTerminatedFailed.remove(0);
			try {
				connection.updateTerminatedEvent(terminatedEvent);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				listUpdateTerminatedFailed.add(terminatedEvent);
				logError("Update "+terminatedEvent.toString()+"; error:"+e.getMessage());
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
		nextTime = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.itpro.util.ProcessingThread#process()
	 */
	@Override
	protected void process() {
		// TODO Auto-generated method stub
		if(!isConnected)
			return;
		
		getTerminatedEvents();
		if (!terminatedEvents.isEmpty()) {
			TerminatedEvent terminatedEvent = terminatedEvents.remove(0);
			OnTerminatedEvent(terminatedEvent);
		}
		
		UpdateOfferRecordCmd updateOfferRecordCmdResp = (UpdateOfferRecordCmd) queueUpdateOfferRecordResp.dequeue();
		if(updateOfferRecordCmdResp!=null){
			OnUpdateOfferRecordCmdResp(updateOfferRecordCmdResp);
		}
	}

	public void updateTerminatedEvent(TerminatedEvent terminatedEvent) {
		// TODO Auto-generated method stub
		try {
			connection.updateTerminatedEvent(terminatedEvent);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			listUpdateTerminatedFailed.add(terminatedEvent);
			logError("Update "+terminatedEvent.toString()+"; Error:"+e.getMessage()); 
		}
	}

	private void OnTerminatedEvent(TerminatedEvent terminatedEvent) {
		// TODO Auto-generated method stub
		if(listTerminatedEventProcessing.get(terminatedEvent.msisdn)!=null){
			terminatedEvent.status = 3;
			updateTerminatedEvent(terminatedEvent);
			return;
		}
		try {
			OfferRecord offerRecord = connection.getUnPaidOfferRecord(terminatedEvent.msisdn);
			if(offerRecord!=null){
				listTerminatedEventProcessing.put(terminatedEvent.msisdn, terminatedEvent);
				offerRecord.last_charge_date = new Timestamp(System.currentTimeMillis());
				offerRecord.charge_status = OfferRecord.OFFER_CHARGE_STATUS_BAD_DEBIT;
				//offerRecord.charge_result_code = 0;
				//offerRecord.charge_result_string = "Terminated Event, date:"+terminatedEvent.date_time.toString();
				UpdateOfferRecordCmd updateOfferCmd = new UpdateOfferRecordCmd(offerRecord, queueUpdateOfferRecordResp);
				updateOfferRecord(updateOfferCmd);
				logInfo(offerRecord.toString());
			}
			else{
				terminatedEvent.status = 3;
				updateTerminatedEvent(terminatedEvent);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logError("getUnPaidOfferRecord, msisdn:"+terminatedEvent.msisdn+"; error:"+e.getMessage());
			terminatedEvents.add(terminatedEvent);
		}
	}
	
	private void OnUpdateOfferRecordCmdResp(UpdateOfferRecordCmd updateOfferRecordCmdResp) {
		// TODO Auto-generated method stub
		if(updateOfferRecordCmdResp.resultCode==UpdateOfferRecordCmd.RESULT_OK){
			TerminatedEvent terminatedEvent = listTerminatedEventProcessing.get(updateOfferRecordCmdResp.offerRecord.msisdn);
			terminatedEvent.status = 2;
			updateTerminatedEvent(terminatedEvent);
		}
		else{
			//re-update when error
			updateOfferRecord(updateOfferRecordCmdResp);
		}
	}

	private void updateOfferRecord(UpdateOfferRecordCmd updateOfferCmd) {
		// TODO Auto-generated method stub
		GlobalVars.offerTableAccess.queueUpdateOfferRecordReq.enqueue(updateOfferCmd);
	}

	public void getTerminatedEvents() {
		if(GlobalVars.stopModuleFlag)
			return;
		long curTime = System.currentTimeMillis();
		if (terminatedEvents.size() == 0) {
			if (nextTime < curTime) {
				try {
					terminatedEvents = connection.getTerminatedEventList();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logError("Get outOfDataEvents error:" + e.getMessage());
					isConnected = false;
					terminatedEvents = new Vector<TerminatedEvent>();
				}
				if (terminatedEvents.size() == 0) {
					nextTime = curTime + 5000;
				} else {
					nextTime = curTime;
				}
			}
		}
	}
}
