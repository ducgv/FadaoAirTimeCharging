/**
 * 
 */
package com.itpro.airtime.charging.structs;

import java.sql.Timestamp;

/**
 * @author Giap Van Duc
 *
 */
public class TerminatedEvent {
	public int id;
	public String msisdn;
	public Timestamp date_time;
	public int status;
	public String toString(){
		String result = "TerminatedEvent:";
		result += " msisdn:"+msisdn;
		result += "; date_time:"+date_time.toString();
		return result;
	}
}
