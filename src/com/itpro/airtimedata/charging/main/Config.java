/**
 * 
 */
package com.itpro.airtimedata.charging.main;


import com.itpro.util.Params;

/**
 * @author Giap Van Duc
 *
 */
public class Config {
	public static String	homePath;
	public static final String sysConfigFileName = "airTimeDataCharging.cfg";
	public static final String logConfigFileName = "loggerAirTimeDataCharging.cfg";
	
	public static int		cliListenPort;	
	public static int 		cliRequestTimeout;

	public static String 	dbServerName;
	public static String 	dbDatabaseName;
	public static String 	dbUserName;
	public static String 	dbPassword;
	
	public static String sendTimeAllow = "";
	
	public static final int LANG_EN = 0;
	public static final int LANG_LA = 1;
	public static int smsLanguage;
	
//	public static Object mutex = new Object();
	public static Params[]	messageContents = new Params[2];
	public static Params	serviceConfigs = new Params();
	public static int maxChargingConcurrent;
	public static int maxChargingTPS;
	   //PaymentGW
    public static String    profileSubScriber_spID;
    public static String    profileSubScriber_spPassword;
    // charging
    public static String    charging_spID;
    public static String    charging_serviceID;
    public static String    charging_spPassword;
    public static int       MULTIPLIER=100;
}
