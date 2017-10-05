/**
 * 
 */
package com.itpro.airtime.charging.process;

import java.sql.SQLException;

import com.itpro.airtime.charging.db.DbConnection;
import com.itpro.airtime.charging.main.Config;
import com.itpro.airtime.charging.structs.InsertCDRCmd;
import com.itpro.airtime.charging.structs.UpdateOfferRecordCmd;
import com.itpro.util.ProcessingThread;
import com.itpro.util.Queue;

/**
 * @author Giap Van Duc
 *
 */
public class CDRTableAccess extends ProcessingThread {
	public Queue queueInsertCDRRecordReq = new Queue();
	DbConnection connection = null;
	public boolean isConnected = false;

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
		if(isConnected){
			try {
				connection.checkConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				logError("Check DB connection error:"+e.getMessage());
				isConnected = false;
			}
		}
	}

	private void OnConnected() {

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
		InsertCDRCmd insertCDRCmd = (InsertCDRCmd) queueInsertCDRRecordReq.dequeue();
		if(insertCDRCmd!=null){
			try {
				connection.insertCDRRecord(insertCDRCmd.cdrRecord);
				insertCDRCmd.resultCode = UpdateOfferRecordCmd.RESULT_OK;
				insertCDRCmd.queueResp.enqueue(insertCDRCmd);
				logInfo(insertCDRCmd.cdrRecord.toString());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				insertCDRCmd.resultCode = -1;
				logError("Insert " + insertCDRCmd.cdrRecord.toString()+ "; error:" + e.getMessage());
				insertCDRCmd.queueResp.enqueue(insertCDRCmd);
				isConnected = false;
			}
			
		}

	}
}
