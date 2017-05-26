/**
 * 
 */
package com.itpro.airtimedata.charging.util;

/**
 * @author ducgv
 *
 */
public class DataPackageDisplay {
	public static String getDataAmountString(int dataAmount){
		if(dataAmount>=1024){
			if(dataAmount%1024==0)
				return String.format("%d GB", dataAmount/1024);
			else
				return String.format("%.2f GB", (double)dataAmount/1024);
		}
		else 
			return String.format("%d MB", dataAmount);
	}
	
	public static String getNumberString(int number){
		return String.format("%,d", number);
	}
}
