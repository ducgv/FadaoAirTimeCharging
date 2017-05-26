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
	public String sub_product_code;
	public int sub_id;
	public int offer_id;
	public int offer_type;
	public String package_name;
	public int package_price;
	public int service_fee;
	public int charge_value;
	public int result_code;
	public String result_string;
	public int status;
	public String toString(){
		String str= "CDRRecord: "
				+ "msisdn:"+msisdn
				+ "; sub_product_code:"+sub_product_code
				+ "; sub_id:"+sub_id
				+ "; date_time:"+date_time
				+ "; offer_id:"+offer_id
				+ "; offer_type:"+offer_type
				+ "; package_name:"+package_name
				+ "; package_price:"+package_price
				+ "; service_fee:"+service_fee
				+ "; charge_value:"+charge_value
				+ "; result_code:"+result_code
				+ "; result_string:"+result_string
				+ "; status:"+status;
		return str;
		
	}
}
