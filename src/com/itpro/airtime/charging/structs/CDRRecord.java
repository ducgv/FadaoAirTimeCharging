/**
 * 
 */
package com.itpro.airtime.charging.structs;

import java.sql.Timestamp;

/**
 * @author ducgv
 *
 */
public class CDRRecord {
	public static final int CHARGE_SUCCESS = 2;
	public static final int CHARGE_FAILED = 3;
	public static final int CHARGE_BAD_DEBIT = 4;
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
	public int old_paid_value;
	public String remark;
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
				+ ((remark!=null&&remark.equals("CUTOVER"))?"; old_paid_value:"+old_paid_value+"; remark:"+remark:"")
				+ "; status:"+status;
		return str;
		
	}
}
