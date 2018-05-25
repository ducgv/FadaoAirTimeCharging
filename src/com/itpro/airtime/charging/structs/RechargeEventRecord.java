/**
 * 
 */
package com.itpro.airtime.charging.structs;

import java.sql.Timestamp;

/**
 * @author ducgv
 *
 */
public class RechargeEventRecord {
	public static final int STATUS_CHARGE_SUCCESS = 2;
	public static final int STATUS_CHARGE_FAILED = 3;
	public static final int STATUS_UNBORROW = 4;
	public static final int STATUS_SKIP_FIRST_CHARGE = 5;
	public int id;
	public Timestamp date_time;
	public String msisdn;
	public int recharge_value;
	public int status;
	public String toString(){
		String str = "RechargeEvent:"+
				" date_time:"+date_time+
				"; msisdn:"+msisdn+
				"; recharge_value:"+recharge_value;
		return str;
	}
	
}
