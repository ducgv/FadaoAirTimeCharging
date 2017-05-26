/**
 * 
 */
package com.itpro.airtimedata.charging.structs;

import java.sql.Date;
/**
 * @author ducgv
 *
 */
public class SubInfo {
	public String msisdn;
	public int subId;
	public int subType = 0;
	public String subProductCode = "";
	public boolean isActive = false;
	public Date activeDate;
	public int balance = 0;
	public int freeData1;
	public int freeData2;
	public int numDaysActive = 0;
	public int respCode = 0;
	public String respString = "";
	public String detail = "";
	
	public String toString(){
		String result = "SubInfo:";
		result+=" msisdn:"+msisdn;
		if(respCode==11){
			result+="; subType:"+subType;
			result+="; isActive:"+isActive;
			result+="; activeDate:"+activeDate;
			result+="; balance:"+balance;
			result+="; freeData1:"+freeData1+" KB";
			result+="; freeData2:"+freeData2+" KB";
			result+="; numDaysActive:"+numDaysActive;
			result+="; respCode:"+respCode;
			result+="; detail:"+detail;
		}
		else if(respCode==22){
			result+="; subType:"+subType;
			result+="; isActive:"+isActive;
			result+="; activeDate:"+activeDate;
			result+="; numDaysActive:"+numDaysActive;
			result+="; respCode:"+respCode;
			result+="; detail:"+detail;
		}
		else{
			result+="; respCode:"+respCode;
			result+="; respString:"+respString;
		}
		return result;
	}
	/* respCode:
	 11: prepaid
	 22: pospaid
	 99: The remote IP is not allowed
	 77: The isdn does not exists
	 88: Database has some error, please contact administrator
	 -1: Unhandled error, please contact administrator
	 * */
}
