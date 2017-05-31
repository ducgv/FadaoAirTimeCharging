/**
 * 
 */
package com.itpro.airtimedata.charging.main;

import com.itpro.cfgreader.CfgReader;
import com.itpro.general.General;
import com.itpro.log4j.ITProLog4j;
import com.itpro.util.Params;
import com.logica.smpp.pdu.PDU;

/**
 * @author Giap Van Duc
 *
 */
public class AirTimeDataCharging {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AirTimeDataCharging main = new AirTimeDataCharging();
		main.start();
	}
	
	public void start(){
		General.checkPlatform();
		if(General.getPlatform()==General.PLATFORM_UNKNOWN){
			System.out.println("Error: System is not support for: "+General.os);
//			return;
		}
		if(General.getPlatform()==General.PLATFORM_WINDOW)
			Config.homePath = "C:\\itpro\\airTimeData\\airTimeDataCharging\\";
		else
			Config.homePath = "/opt/itpro/airTime/airTimeCharging/";
		
		GlobalVars.logManager = new ITProLog4j();
		GlobalVars.logManager.Initialize(Config.homePath+"config"+General.filePathSeparator+Config.logConfigFileName,Config.homePath+"log"+Config.homePath+"config"+General.filePathSeparator);
		GlobalVars.logManager.Start();
		GlobalVars.logger = GlobalVars.logManager.GetInstance("AirTimeCharging",
				Config.homePath+"log"+General.filePathSeparator,
				"AirTimeCharging",1,1,1,1,1,1,1,true);
		loadConfig();
		
		PDU.Init();
		
		startSystem();
		
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void startSystem() {
		// TODO Auto-generated method stub
		GlobalVars.smsMTTableAccess.setLogPrefix("[SmsMT] ");
		GlobalVars.smsMTTableAccess.setLogger(GlobalVars.logger);
		GlobalVars.smsMTTableAccess.start();
		

		GlobalVars.offerTableAccess.setLogPrefix("[Offer] ");
		GlobalVars.offerTableAccess.setLogger(GlobalVars.logger);
		GlobalVars.offerTableAccess.start();

		GlobalVars.cdrTableAccess.setLogPrefix("[CDR] ");
		GlobalVars.cdrTableAccess.setLogger(GlobalVars.logger);
		GlobalVars.cdrTableAccess.start();
		
		GlobalVars.chargingProcess.setLogPrefix("[Service] ");
		GlobalVars.chargingProcess.setLogger(GlobalVars.logger);		
		GlobalVars.chargingProcess.start();
		
//		GlobalVars.terminatedEventProcess.setLogPrefix("[TerminatedEvent] ");
//		GlobalVars.terminatedEventProcess.setLogger(GlobalVars.logger);		
//		GlobalVars.terminatedEventProcess.start();
	    GlobalVars.paymentGWInterface.setLogPrefix("[paymentGWInterface] ");
		GlobalVars.paymentGWInterface.setLogger(GlobalVars.logger);
		GlobalVars.paymentGWInterface.start();
		
		GlobalVars.airTimeDataChargingCli.setLogger(GlobalVars.logger);
		GlobalVars.airTimeDataChargingCli.setLogPrefix("[CLI] ");
		GlobalVars.airTimeDataChargingCli.setRequestTimeout(Config.cliRequestTimeout);
		GlobalVars.airTimeDataChargingCli.setListenPort(Config.cliListenPort);
		GlobalVars.airTimeDataChargingCli.setTimeoutErrorString("Timeout");
		GlobalVars.airTimeDataChargingCli.setSyntaxErrorString("Wrong Syntax");
		GlobalVars.airTimeDataChargingCli.start();
	}

	public void loadConfig() {
		CfgReader cfgReader = new CfgReader();
		String file = Config.homePath+"config"+General.filePathSeparator+Config.sysConfigFileName;
		cfgReader.load(file,";");
		
		cfgReader.setGroup("CLI");
		Config.cliListenPort = cfgReader.getInt("ListenPort", 1443);		
		Config.cliRequestTimeout = cfgReader.getInt("RequestTimeout", 60);
		
		cfgReader.setGroup("DB");
		Config.dbServerName = cfgReader.getString("ServerIpAddr", "10.10.1.58");
		Config.dbDatabaseName = cfgReader.getString("DbName", "airtime");
		Config.dbUserName = cfgReader.getString("UserName", "airtime");;
		Config.dbPassword = cfgReader.getString("Password", "airtime!@#itpro2017");
		
		cfgReader.setGroup("payment gw");
		  
		Config.profileSubScriber_spID=cfgReader.getString("profileSubScriber_spID", "1000117480");
		Config.profileSubScriber_spPassword=cfgReader.getString("profileSubScriber_spPassword", "fadaoAASS@ADVetl");
	    
		Config.charging_spID=cfgReader.getString("charging_spID", "1000117481");
		Config.charging_spPassword=cfgReader.getString("charging_spPassword", "fadaoAASS@ADVetl");
		Config.charging_serviceID=cfgReader.getString("charging_serviceID", "33334");
		if(cfgReader.isChanged())
			cfgReader.save(file);
		
		Config.messageContents[Config.LANG_EN] = new Params();
		Config.messageContents[Config.LANG_LA] = new Params();
	}
	
	
}
