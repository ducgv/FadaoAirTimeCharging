/**
 * 
 */
package com.itpro.airtimedata.charging.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author ducgv
 *
 */
public class DataPackageDisplay {
    static SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy");
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
	
	public static String getDateFormat(Date date){
	    return sdf.format(date);
	}
}
