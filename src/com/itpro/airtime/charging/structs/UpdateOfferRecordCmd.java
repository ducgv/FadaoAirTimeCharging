/**
 * 
 */
package com.itpro.airtime.charging.structs;

import com.itpro.util.Queue;

/**
 * @author ducgv
 *
 */
public class UpdateOfferRecordCmd {
	public static final int RESULT_OK = 0;
	public UpdateOfferRecordCmd(OfferRecord offerRecord, Queue queueResp){
		this.offerRecord = offerRecord;
		this.queueResp = queueResp;
	}
	public OfferRecord offerRecord;
	public int resultCode;
	public Queue queueResp;
	public String toString(){
		String str = "UpdateOfferCmd:"
				+ " msisdn:"+offerRecord.msisdn
				+ " offer_id:"+offerRecord.offer_id
				+ " last_charge_date:"+offerRecord.last_charge_date
				+ "; charge_status:"+offerRecord.charge_status;
		return str;
	}
}
