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
