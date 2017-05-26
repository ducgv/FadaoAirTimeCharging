/**
 * 
 */
package com.itpro.airtimedata.charging.structs;

import com.itpro.util.Queue;

/**
 * @author ducgv
 *
 */
public class InsertCDRCmd {
	public final static int RESULT_OK = 0;
	public InsertCDRCmd(CDRRecord cdrRecord, Queue queueResp){
		this.cdrRecord = cdrRecord;
		this.queueResp = queueResp;
	}
	public CDRRecord cdrRecord;
	public Queue queueResp;
	public int resultCode = 0;
}
