/**
 * 
 */
package com.itpro.airtime.charging.cli;

import com.itpro.airtime.charging.main.GlobalVars;
import com.itpro.cli.CmdReqProcess;
import com.itpro.cli.CmdRequest;
/**
 * @author Giap Van Duc
 *
 */
public class AirTimeDataChargingCli extends CmdReqProcess {

	/* (non-Javadoc)
	 * @see com.itpro.cli.CmdReqProcess#OnHeartBeat()
	 */
	@Override
	protected void OnHeartBeat() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.itpro.cli.CmdReqProcess#OnRequest(com.itpro.cli.CmdRequest)
	 */
	@Override
	protected void OnRequest(CmdRequest cmdRequest) {
		// TODO Auto-generated method stub
		if(cmdRequest.cmd.equalsIgnoreCase("Reload")){
			String target = cmdRequest.params.get("target");
			if(target!=null){
				if(target.equalsIgnoreCase(CLICmd.SERVICE_CONFIG)||target.equalsIgnoreCase(CLICmd.MESSAGE_CONTENTS)){
					GlobalVars.chargingProcess.queueCmdCLIRequest.enqueue(cmdRequest);				
				}
				else{
					cmdRequest.result.put("Result", "failed");
					cmdRequest.result.put("Error", "Syntax error");
					cmdRequest.queueResp.enqueue(cmdRequest);
				}
			}
			else{
				cmdRequest.result.put("Result", "failed");
				cmdRequest.result.put("Error", "Syntax error");
				cmdRequest.queueResp.enqueue(cmdRequest);
			}
		}
		else if(cmdRequest.cmd.equalsIgnoreCase("Stop")){
			String target = cmdRequest.params.get("module");
			if(target!=null){
				if(target.equalsIgnoreCase(CLICmd.MODULE_CHARGING)){
					GlobalVars.stopModuleFlag = true;
					cmdRequest.result.put("Result", "success");
					cmdRequest.queueResp.enqueue(cmdRequest);			
				}
				else{
					cmdRequest.result.put("Result", "failed");
					cmdRequest.result.put("Error", "Syntax error");
					cmdRequest.queueResp.enqueue(cmdRequest);
				}
			}
			else{
				cmdRequest.result.put("Result", "failed");
				cmdRequest.result.put("Error", "Syntax error");
				cmdRequest.queueResp.enqueue(cmdRequest);
			}
		}
		else{			
			cmdRequest.result.put("Result", "failed");
			cmdRequest.result.put("Error", "Syntax error");
			cmdRequest.queueResp.enqueue(cmdRequest);			
		}
	}

}
