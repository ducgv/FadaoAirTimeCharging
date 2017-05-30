/**
 * 
 */
package com.itpro.airtimedata.charging.main;

import com.itpro.airtimedata.charging.cli.AirTimeDataChargingCli;
import com.itpro.airtimedata.charging.process.CDRTableAccess;
import com.itpro.airtimedata.charging.process.ChargingProcess;
import com.itpro.airtimedata.charging.process.OfferTableAccess;
import com.itpro.airtimedata.charging.process.SmsMTTableAccess;
import com.itpro.airtimedata.charging.process.TerminatedEventProcess;
import com.itpro.log4j.ITProLog4j;
import com.itpro.log4j.ITProLog4jCategory;
import com.itpro.paymentgw.PaymentGWInterface;

/**
 * @author Giap Van Duc
 *
 */
public class GlobalVars {
	public static ITProLog4j logManager;
	public static ITProLog4jCategory logger;
	
	public static final int MIN_SUBMIT_SEQ = 0x00000001;
	public static final int MAX_SUBMIT_SEQ = 0x0FFFFFFF;
	
	public static int nSeq = MIN_SUBMIT_SEQ;
	
	public static synchronized int GetSeq(){
		if(nSeq==MAX_SUBMIT_SEQ)
			nSeq = MIN_SUBMIT_SEQ;
		return nSeq++;
	}
	
	public static AirTimeDataChargingCli airTimeDataChargingCli = new AirTimeDataChargingCli();
	public static SmsMTTableAccess smsMTTableAccess = new SmsMTTableAccess();
	public static OfferTableAccess offerTableAccess = new OfferTableAccess();
	public static ChargingProcess chargingProcess = new ChargingProcess();
	public static CDRTableAccess cdrTableAccess = new CDRTableAccess();
	public static TerminatedEventProcess terminatedEventProcess = new TerminatedEventProcess();
	public static PaymentGWInterface paymentGWInterface=new PaymentGWInterface();
	public static boolean stopModuleFlag = false;
	public static boolean isChargingError = false;
	public static long lastTimeChargingError;
	
}
