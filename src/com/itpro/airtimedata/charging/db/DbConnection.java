/**
 * 
 */
package com.itpro.airtimedata.charging.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;

import com.itpro.airtimedata.charging.structs.CDRRecord;
import com.itpro.airtimedata.charging.structs.MTRecord;
import com.itpro.airtimedata.charging.structs.OfferRecord;
import com.itpro.airtimedata.charging.structs.RechargeEventRecord;
import com.itpro.airtimedata.charging.structs.TerminatedEvent;
import com.itpro.util.MySQLConnection;


/**
 * @author Giap Van Duc
 *
 */
public class DbConnection extends MySQLConnection {
	
	public static final long DAY_MILISECONDS = 86400000; //24*60*60*1000
	
	public Timestamp dateAdd(Timestamp timestamp, int day){
		Timestamp result = new Timestamp(timestamp.getTime()+(long)day*DAY_MILISECONDS);
		return result;
	}
	
	public DbConnection(String serverIpAddr, String databaseName,
			String userName, String password) {
		super(serverIpAddr, databaseName, userName, password);
		// TODO Auto-generated constructor stub
	}
	
	public static boolean subsCheck(String csAddr,String csRule){
		int nRuleLen = csRule.length();
		int nLenAddr = csAddr.length();
		if(nRuleLen > nLenAddr)
			return false;
		for(int i=0;i<nRuleLen;i++){
			if(csRule.charAt(i)=='*')
				break;
			if(csRule.charAt(i)=='X')
				continue;
			if(csRule.charAt(i)!=csAddr.charAt(i))
				return false;
		}
		int nSub=nLenAddr-nRuleLen;
		for(int i=nRuleLen-1;i>=0;i--){
			if(csRule.charAt(i)=='*')
				break;
			if(csRule.charAt(i)=='X')
				continue;
			if(csRule.charAt(i)!=csAddr.charAt(i+nSub))
				return false;
		}
		return true;
	}
   public int getChargingTransactionId() throws SQLException{
        PreparedStatement ps=connection.prepareStatement(
        "select next_seq('pw_charging_trans_id') as transaction_id");
        ps.execute();
        ResultSet rs = ps.getResultSet();
        int result = 0;
        if(rs.next()) {
            result = rs.getInt(1);
        }
        rs.close();
        ps.close();
        return result;
    }
	public void insertSmsMtRecord(MTRecord mtRecord) throws SQLException{
		PreparedStatement ps = null;		
		ps=connection.prepareStatement("INSERT INTO sms_mt (sms_type,msisdn,content, offer_id) VALUES (?,?,?,?)");
		ps.setInt(1, mtRecord.sms_type);
		ps.setString(2, mtRecord.msisdn);
		ps.setBytes(3, mtRecord.content);
		ps.setInt(4, mtRecord.offer_id);
		ps.execute();
		ps.close();
	}

	public void updateOfferRecord(OfferRecord offerRecordUpdate) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		ps=connection.prepareStatement("UPDATE offers SET charge_status = ?, last_charge_date =?, paid_value=?,skiped_first_recharge=? WHERE offer_id=?");
		ps.setInt(1, offerRecordUpdate.charge_status);
		ps.setTimestamp(2, offerRecordUpdate.last_charge_date);
	    ps.setInt(3, offerRecordUpdate.paid_value);
	    ps.setInt(4, offerRecordUpdate.skiped_first_recharge);
		ps.setInt(5, offerRecordUpdate.offer_id);
		ps.execute();
		ps.close();
	}

	public Vector<RechargeEventRecord> getRechargeEventRecords() throws SQLException {
		// TODO Auto-generated method stub
		Vector<RechargeEventRecord> rechargeEventRecords = new Vector<RechargeEventRecord>();
		PreparedStatement ps=connection.prepareStatement(
				"SELECT id, date_time, msisdn, recharge_value FROM recharge_event WHERE `status` = 0");
		ps.setMaxRows(30);
		ps.execute();
		ResultSet rs = ps.getResultSet();
		while(rs.next()) {
			RechargeEventRecord rechargeEventRecord = new RechargeEventRecord();
			rechargeEventRecord.id = rs.getInt("id");
			rechargeEventRecord.date_time = rs.getTimestamp("date_time");
			String msisdn = rs.getString("msisdn");
			rechargeEventRecord.msisdn = msisdn.startsWith("856")?msisdn.replaceFirst("856", ""):msisdn;
			rechargeEventRecord.recharge_value = rs.getInt("recharge_value");
			rechargeEventRecord.status = 0;
			rechargeEventRecords.add(rechargeEventRecord);
		}
		rs.close();
		ps.close();
		if(rechargeEventRecords.size()>0){
			ps=connection.prepareStatement("UPDATE recharge_event SET status = 1 WHERE id = ?");				
			for(RechargeEventRecord moRecord: rechargeEventRecords){				
				ps.setInt(1, moRecord.id);					
				ps.addBatch();					
			}
			ps.executeBatch();
			ps.close();
		}			
		return rechargeEventRecords;
	}
    public void getListOfferRecord(Vector<OfferRecord> offerRecords) throws SQLException {
        // TODO Auto-generated method stub
        PreparedStatement ps=connection.prepareStatement("call get_list_offer_record()");
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while(rs.next()) {
            OfferRecord offerRecord = new OfferRecord();
            offerRecord.offer_id = rs.getInt("offer_id");
            offerRecord.offer_type = rs.getInt("offer_type");
            offerRecord.msisdn = rs.getString("msisdn");
            offerRecord.req_date = rs.getTimestamp("req_date");
            offerRecord.package_name = rs.getString("package_name");
            offerRecord.paid_value=rs.getInt("paid_value");
            offerRecord.package_value = rs.getInt("package_value");
            offerRecord.package_service_fee = rs.getInt("package_service_fee");
            offerRecord.last_charge_date = rs.getTimestamp("last_charge_date");
            offerRecord.skiped_first_recharge = rs.getInt("skiped_first_recharge");
            offerRecords.add(offerRecord);
        }
        rs.close();
        ps.close(); 
    }
	
	
	public OfferRecord getUnPaidOfferRecord(String msisdn) throws SQLException {
		// TODO Auto-generated method stub
		OfferRecord offerRecord = null;
		PreparedStatement ps=connection.prepareStatement(
				"select offer_id,offer_type,msisdn,req_date,package_name,package_value,paid_value,"
						+ "package_service_fee,charge_status,last_charge_date,skiped_first_recharge FROM offers WHERE msisdn = ? AND status =? AND (charge_status = 0 or charge_status = 1)");
		ps.setString(1, msisdn);
		ps.setInt(2, OfferRecord.OFFER_STATUS_SUCCESS);
		ps.execute();
		
		ResultSet rs = ps.getResultSet();
		if(rs.next()) {
			offerRecord = new OfferRecord();
			offerRecord.offer_id = rs.getInt("offer_id");
			offerRecord.offer_type = rs.getInt("offer_type");
			offerRecord.msisdn = rs.getString("msisdn");
			offerRecord.req_date = rs.getTimestamp("req_date");
			offerRecord.package_name = rs.getString("package_name");
			offerRecord.paid_value=rs.getInt("paid_value");
			offerRecord.package_value = rs.getInt("package_value");
			offerRecord.package_service_fee = rs.getInt("package_service_fee");
			offerRecord.charge_status=rs.getInt("charge_status");
			offerRecord.last_charge_date = rs.getTimestamp("last_charge_date");
			offerRecord.skiped_first_recharge = rs.getInt("skiped_first_recharge");
			return offerRecord;
		}
		rs.close();
		ps.close();
		return offerRecord;
	}

	public void insertCDRRecord(CDRRecord cdrRecord) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		String sql = "INSERT INTO cdr(date_time,msisdn,offer_id,offer_type,package_name,package_value,"
				+ "service_fee,charge_value,result_code,result_string,status,transactionID,spID,serviceID,paid_value_before) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		ps=connection.prepareStatement(sql);
		ps.setTimestamp(1,cdrRecord.date_time);
		ps.setString(2,cdrRecord.msisdn);
		ps.setInt(3,cdrRecord.offer_id);
		ps.setInt(4,cdrRecord.offer_type);
		ps.setString(5,cdrRecord.package_name);
		ps.setInt(6,cdrRecord.package_value);
		ps.setInt(7,cdrRecord.service_fee);
		ps.setInt(8,cdrRecord.charge_value);
		ps.setInt(9,cdrRecord.result_code);
		ps.setString(10,cdrRecord.result_string);
		ps.setInt(11,cdrRecord.status);	
		ps.setInt(12,cdrRecord.transactionID);
		ps.setString(13,cdrRecord.spID);
		ps.setString(14,cdrRecord.serviceID);
		ps.setInt(15,cdrRecord.paid_value_before);
		ps.execute();
		ps.close();
	}

	/*
	public int generateOutOfDataReport() throws SQLException {
		// TODO Auto-generated method stub
		int rowsCount = 0;
		PreparedStatement ps=connection.prepareStatement("select generate_out_of_data_report()");
		ps.execute();
		ResultSet rs = ps.getResultSet();
		if(rs.next()) {
			rowsCount = rs.getInt(1);
		}
		rs.close();
		ps.close();
		return rowsCount;
	}
	*/
	public void updateRechargeEvent(RechargeEventRecord rechargeEventRecord) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		ps=connection.prepareStatement("UPDATE recharge_event SET status = ? WHERE id = ?");				
		ps.setInt(1, rechargeEventRecord.status);
		ps.setInt(2, rechargeEventRecord.id);
		ps.execute();
		ps.close();
	}
	
	public Vector<TerminatedEvent> getTerminatedEventList() throws SQLException {
		// TODO Auto-generated method stub
		Vector<TerminatedEvent> terminatedEvents = new Vector<TerminatedEvent>();
		PreparedStatement ps=connection.prepareStatement(
				"SELECT id, msisdn, date_time FROM terminated_event WHERE `status` = 0");
		ps.setMaxRows(30);
		ps.execute();
		ResultSet rs = ps.getResultSet();
		while(rs.next()) {
			TerminatedEvent terminatedEvent = new TerminatedEvent();
			terminatedEvent.id = rs.getInt("id");
			terminatedEvent.msisdn = rs.getString("msisdn");
			terminatedEvent.date_time = rs.getTimestamp("date_time");
			terminatedEvent.status = 0;
			terminatedEvents.add(terminatedEvent);
		}
		rs.close();
		ps.close();
		if(!terminatedEvents.isEmpty()){
			ps=connection.prepareStatement("UPDATE terminated_event SET status = 1 WHERE id = ?");				
			for(TerminatedEvent terminatedEvent: terminatedEvents){				
				ps.setInt(1, terminatedEvent.id);					
				ps.addBatch();					
			}
			ps.executeBatch();
			ps.close();
		}			
		return terminatedEvents;
	}
	
	public void updateTerminatedEvent(TerminatedEvent terminatedEvent) throws SQLException {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;		
		ps=connection.prepareStatement("UPDATE terminated_event SET status=? WHERE id=?");
		ps.setInt(1, terminatedEvent.status);
		ps.setInt(2, terminatedEvent.id);		
		ps.execute();
		ps.close();
	}

	public void updateWarningChargingError(boolean isError) {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;		
		try {
			ps=connection.prepareStatement("UPDATE warnings SET is_error = ? WHERE warning_type = 'charging'");
			ps.setInt(1, isError?1:0);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
}
