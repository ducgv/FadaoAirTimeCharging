/**
 * 
 */
package com.itpro.airtimedata.charging.structs;

import java.sql.Timestamp;

/**
 * @author ducgv
 *
 */
public class CDRRecord {
	public static final int CHARGE_SUCCESS = 2;
	public static final int CHARGE_FAILED = 3;
	public Timestamp date_time;
	public String msisdn;
	public String sub_id;
	public int province_code;
	public int offer_id;
	public int offer_type;
	public String package_name;
	public int package_value;
	public int service_fee;
	public int charge_value;
	public int paid_value_before;
	public int result_code;
	public String result_string;
	public int status;
	public String spID;
	public int transactionID;
	public String serviceID;
	public String toString(){
		String str= "CDRRecord: "
				+ "msisdn:"+msisdn
				+ "; sub_id:"+sub_id
				+ "; province_code:"+province_code
				+ "; spID:"+spID
				+ "; transactionID:"+transactionID
				+ "; serviceID:"+serviceID
				+ "; date_time:"+date_time
				+ "; offer_id:"+offer_id
				+ "; offer_type:"+offer_type
				+ "; package_name:"+package_name
				+ "; package_value:"+package_value
				+ "; paid_value_before:"+paid_value_before
				+ "; service_fee:"+service_fee
				+ "; charge_value:"+charge_value
				+ "; result_code:"+result_code
				+ "; result_string:"+result_string
				+ "; status:"+status;
		return str;
		
	}
}
