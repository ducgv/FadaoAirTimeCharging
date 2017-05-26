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
	public static final int OFFER_CHARGE_STATUS_PAID = 1;
	public static final int OFFER_CHARGE_STATUS_BAD_DEBIT = 2;
	
	public int offer_id;
	public int offer_type;
	public String msisdn;
	public String sub_product_code;
	public int sub_id;
	public Timestamp req_date;
	public String package_name;
	public int package_data_amount;
	public int package_price;
	public int package_service_fee;
	public Timestamp charge_date = null;
	public int charge_result_code;
	public String charge_result_string;
	public int status; //'0-new, 1-waiting confirm, 2-success, 3-reject, 4-cancel, 5-expired',
	public int charge_status; //0-uncharge, 1-have charge, 2-bad debit;
	public RechargeEventRecord rechargeEventRecord;
	
	public String toString(){
		String result = "OfferRecord: ";
		result+="; msisdn:"+msisdn;
		result+="; sub_product_code:"+sub_product_code;
		result+="; sub_id:"+sub_id;
		result+="; offer_id:"+offer_id;
		result+="; offer_type:"+offer_type;
		result+="; req_date:"+req_date;
		result+="; package_name:"+package_name;
		result+="; package_data_amount:"+package_data_amount;
		result+="; package_service_fee:"+package_service_fee;
		result+="; status:"+status;
		if(charge_date!=null)
			result+="; charge_date:"+charge_date;
		result+="; charge_status:"+charge_status;
		return result;
	}

}
