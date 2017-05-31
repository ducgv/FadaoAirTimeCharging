/**
 * 
 */
package com.itpro.airtimedata.charging.structs;

import java.sql.Timestamp;

/**
 * @author ducgv
 *
 */
public class OfferRecord {
	public static final int OFFER_STATUS_NEW = 0;
	public static final int OFFER_STATUS_WAIT_CONFIRM = 1;
	public static final int OFFER_STATUS_ADDING_DATA = 2;
	public static final int OFFER_STATUS_SUCCESS = 3;
	public static final int OFFER_STATUS_ADD_DATA_FAILED = 4;
	public static final int OFFER_STATUS_REJECT = 5;
	public static final int OFFER_STATUS_CANCEL = 6;
	public static final int OFFER_STATUS_EXPIRE = 7;

    public static final int OFFER_CHARGE_STATUS_UNPAID = 0;
    public static final int OFFER_CHARGE_STATUS_CONTINUE = 1;
    public static final int OFFER_CHARGE_STATUS_DONE = 2;
    public static final int OFFER_CHARGE_STATUS_BAD_DEBIT = 3;
	
	public int offer_id;
	public int offer_type;
	public String msisdn;
	public int province_code;
	public Timestamp req_date;
	public String package_name;
	public int package_value;
	public int package_service_fee;
	public int paid_value;
	public Timestamp last_charge_date = null;
	public int skiped_first_recharge;
	public int status; //'0-new, 1-waiting confirm, 2-success, 3-reject, 4-cancel, 5-expired',
	public int charge_status; //0-for first charge, 1-continue charge, 2-charge done, 3-bad debt
	public RechargeEventRecord rechargeEventRecord;
	
	public String toString(){
		String result = "OfferRecord: ";
		result+="; msisdn:"+msisdn;
		result+="; province_code:"+province_code;
		result+="; offer_id:"+offer_id;
		result+="; offer_type:"+offer_type;
		result+="; req_date:"+req_date;
		result+="; package_name:"+package_name;
		result+="; package_value:"+package_value;
	    result+="; paid_value:"+paid_value;
		result+="; package_service_fee:"+package_service_fee;
		result+="; status:"+status;
		if(last_charge_date!=null)
			result+="; charge_date:"+last_charge_date;
		result+="; skiped_first_recharge:"+skiped_first_recharge;
		result+="; charge_status:"+charge_status;
		return result;
	}

}
