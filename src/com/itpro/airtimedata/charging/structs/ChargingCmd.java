/**
 * 
 */
package com.itpro.airtimedata.charging.structs;
/**
 * @author ducgv
 *
 */
import java.sql.Timestamp;

public class ChargingCmd {
	public final static int RESULT_OK = 0;
	public OfferRecord offerRecord;
	public Timestamp charge_date;
	public int chargeValue;
	public int resultCode;
	public String resultString;
	public ChargingCmd(OfferRecord offerRecord){
		this.offerRecord = offerRecord;
		chargeValue = offerRecord.package_price+offerRecord.package_service_fee;
	}
	
	public String toString(){
		String str = "ChargingCmd:"
				+ " msisdn:"+offerRecord.msisdn
				+ "; chargeValue:"+chargeValue
				+ "; resultCode:"+resultCode
				+ "; resultString:"+resultString;
		return str;
	}
}
