/**
 * 
 */
package com.itpro.paymentgw;

import java.util.Hashtable;

import com.itpro.airtime.charging.main.Config;
import com.itpro.paymentgw.cmd.KeepAliveCmd;
import com.itpro.paymentgw.cmd.LoginCmd;
import com.itpro.paymentgw.cmd.PaymentGWCmd;
import com.itpro.util.ProcessingThread;
import com.itpro.util.Queue;

/**
 * @author Giap Van Duc
 *
 */
public class PaymentGWInterface extends ProcessingThread {	
	private static final int MIN_SEQ = 0x00000001;
	private static final int MAX_SEQ = 0x0fffffff;
	private int curSeq;
	private int getSeq(){
		if(curSeq>MAX_SEQ){
			curSeq = MIN_SEQ;
		}
		return curSeq++;
	}
	
	public static final int LOGIN_STATE_NONE = 0;
    public static final int LOGIN_STATE_WAIT = 1;
    public static final int LOGIN_STATE_SUCCESS = 2; 

    public String CURRENT_TOKEN = "";
    public int loginState = 0; //0: not login, 1: loging in, 2: logged in;
    public long lastKeepAliveTime = 0;
	Hashtable<Integer, PaymentGWCmd> userCmdProcessingList = new Hashtable<Integer, PaymentGWCmd>();
	
//	private AccountServiceLocator accountServiceLocator;
	
	public Queue queueUserRequest = new Queue();
	private Queue queueResp = new Queue();
	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#OnHeartBeat()
	 */
	@Override
	protected void OnHeartBeat() {
	    if(loginState == LOGIN_STATE_NONE){
            login();
        }
        if(loginState==LOGIN_STATE_SUCCESS){
            long curTime = System.currentTimeMillis();
            if(curTime>=lastKeepAliveTime+150000){
                keepAlive();
                lastKeepAliveTime=curTime;
            }
        }
	}

	

	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#initialize()
	 */
	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		setHeartBeatInterval(5000);
		setLogPrefix("[PaymentGWInterface] ");
		PaymentGWResultCode.init();
		login();
	}
    private void login() {
        // TODO Auto-generated method stub
        LoginCmd loginCmd = new LoginCmd();
        loginCmd.spID = Config.profileSubScriber_spID;
        loginCmd.spPassword = Config.profileSubScriber_spPassword;
        loginCmd.transactionId =getSeq();
        loginCmd.queueResp = queueResp;
        if(loginCmd.transactionId!=-1){
            logInfo(loginCmd.getReqString());
            PaymentGWSession paymentGWSession = new PaymentGWSession(loginCmd, queueResp);
            paymentGWSession.setLogger(logger);
            paymentGWSession.start();
            loginState = LOGIN_STATE_WAIT;
        }
    }
    private void OnLoginResp(LoginCmd loginCmdResp) {
        // TODO Auto-generated method stub
        logInfo(loginCmdResp.getRespString());
        if(loginCmdResp.result == PaymentGWResultCode.R_SUCCESS&&loginCmdResp.resultCode == PaymentGWResultCode.RC_LOGIN_SUCCESS){
            CURRENT_TOKEN = loginCmdResp.token;
            loginState = LOGIN_STATE_SUCCESS;
            lastKeepAliveTime = System.currentTimeMillis();
        }
        else{
            loginState = LOGIN_STATE_NONE;
        }
    }
    private void keepAlive() {
        // TODO Auto-generated method stub
        KeepAliveCmd keepAliveCmd = new KeepAliveCmd();
        keepAliveCmd.token = CURRENT_TOKEN;
        keepAliveCmd.queueResp = queueResp;
        logInfo(keepAliveCmd.getReqString());
        queueUserRequest.enqueue(keepAliveCmd);

    }
    
    private void OnKeepAliveResp(KeepAliveCmd keepAliveCmdResp) {
        // TODO Auto-generated method stub
        logInfo(keepAliveCmdResp.getRespString());
        if(keepAliveCmdResp.result == PaymentGWResultCode.R_SUCCESS&&keepAliveCmdResp.resultCode == PaymentGWResultCode.RC_KEEP_ALIVE_SUCCESS){
            lastKeepAliveTime = System.currentTimeMillis();
        }
        else{
            loginState = LOGIN_STATE_NONE;
        }
    }
	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#process()
	 */
	@Override
	protected void process() {
		// TODO Auto-generated method stub	
		PaymentGWCmd paymentGWCmd = (PaymentGWCmd)queueUserRequest.dequeue();
		if(paymentGWCmd!=null){
		    if(loginState!=LOGIN_STATE_SUCCESS){
		        paymentGWCmd.resultCode=-1;
		        paymentGWCmd.resultString="Payment GW not login.";
		        paymentGWCmd.queueResp.enqueue(paymentGWCmd);
		        
		    }else{
    			paymentGWCmd.seq = getSeq();
    			logInfo(paymentGWCmd.getReqString());			
    			PaymentGWSession paymentGWSession = new PaymentGWSession(paymentGWCmd, queueResp);
    			paymentGWSession.setLogger(logger);
    			userCmdProcessingList.put(paymentGWCmd.seq, paymentGWCmd);
    			paymentGWSession.start();
		    }

		}

		PaymentGWCmd userCmdResp = (PaymentGWCmd)queueResp.dequeue();
		if(userCmdResp!=null){
		    if( userCmdResp instanceof LoginCmd ){
		        OnLoginResp((LoginCmd)userCmdResp);
		    }else if( userCmdResp instanceof KeepAliveCmd ){
                OnKeepAliveResp((KeepAliveCmd)userCmdResp);
            }else{
    			userCmdProcessingList.remove(userCmdResp.seq);
    			logInfo(userCmdResp.getRespString());
    			userCmdResp.queueResp.enqueue(userCmdResp);		
		    }

		}
	}
}
