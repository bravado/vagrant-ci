package br.com.voicetechnology.ng.ipx.ejb.facade;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.LoopDetected;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserNotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.DefaultUserNotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.command.CommandConfigException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.command.LoginACDException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PBXServerException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallBackInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Target;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ForwardMode;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callback;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CostCenter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.NightModeScheduler;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Systemcalllog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.RecordFile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.rule.implement.AddressManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CallBackManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CallLogManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CallManager;
import br.com.voicetechnology.ng.ipx.rule.implement.ConfigManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CostCenterManager;
import br.com.voicetechnology.ng.ipx.rule.implement.GatewayManager;
import br.com.voicetechnology.ng.ipx.rule.implement.GroupManager;
import br.com.voicetechnology.ng.ipx.rule.implement.NightModeSchedulerManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PbxManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PbxuserManager;
import br.com.voicetechnology.ng.ipx.rule.implement.RecordFileManager;
import br.com.voicetechnology.ng.ipx.rule.implement.SessionManager;
import br.com.voicetechnology.ng.ipx.rule.implement.TerminalManager;
import br.com.voicetechnology.ng.ipx.rule.implement.voicemail.VoicemailManager;

/**
 * 
 * <!-- begin-user-doc --> A generated session bean <!-- end-user-doc --> * <!-- begin-xdoclet-definition -->
 * 
 * @ejb.bean name="PbxServerFacade" description="A session bean named PbxServerFacade" display-name="PbxServerFacade" jndi-name="br/com/voicetechnology/ng/ejb/facade/PBXServerFacade" type="Stateless" transaction-type="Container"
 * 
 * <!-- end-xdoclet-definition -->
 * @generated
 */
public abstract class PBXServerFacadeBean implements javax.ejb.SessionBean
{
	private Logger logger;
	private CallManager callManager;
	private GroupManager groupManager;
	private CallBackManager callBackManager;
	private ConfigManager configManager;
	private SessionManager sessionManager;
	private CallLogManager callLogManager;
	private PbxManager pbxManager;
	private AddressManager addManager;
	private PbxuserManager pbxUserManager;
	private VoicemailManager voiceMailManager;
	private GatewayManager gatewayManager;
	private TerminalManager terminalManager;
	private NightModeSchedulerManager nigthModeSchedulerManager;
	private RecordFileManager recordFileManager;
	private CostCenterManager costCenterManager;
	
	public PBXServerFacadeBean()
	{
		try
		{
			logger = Logger.getLogger(this.getClass());
			logger.debug("Creating instance of " + this.getClass().getName());
			callManager = new CallManager(logger.getName());
			groupManager = new GroupManager(logger.getName());
			callBackManager = new CallBackManager(logger.getName());
			configManager = new ConfigManager(logger.getName());
			sessionManager = new SessionManager(logger.getName());
			callLogManager = new CallLogManager(logger.getName());
			pbxManager = new PbxManager(logger.getName());
			addManager = new AddressManager(logger.getName());
			pbxUserManager = new PbxuserManager(logger.getName());
			voiceMailManager= new VoicemailManager(logger.getName());
			gatewayManager = new GatewayManager(logger.getName());
			terminalManager = new TerminalManager(logger.getName());
			nigthModeSchedulerManager = new NightModeSchedulerManager(logger);
			recordFileManager = new RecordFileManager(logger);
			costCenterManager = new CostCenterManager(logger);
		} catch(Exception e)
		{
			PBXServerException ex = new PBXServerException("Error in PBXServerFacade construtor!", e);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * 
	 * <!-- begin-xdoclet-definition -->
	 * 
	 * @ejb.create-method view-type="remote" <!-- end-xdoclet-definition -->
	 * @generated
	 * 
	 * //TODO: Must provide implementation for bean create stub
	 */
	public void ejbCreate()
	{
		//do nothing
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public Domain getRootDomain() throws RemoteException
	{
		try
		{
			return pbxManager.getRootDomain();
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getRootDomain was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	

	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public CallInfo getCallInfo(SipAddressParser from, SipAddressParser to, SipAddressParser requestUser, String domain, String display) throws PBXServerException
	{
		try
		{
			logger.debug("Start getCallInfo, from: " + from.toString() + " to: " + to.toString() + " domain: " + domain);
			CallInfo callInfo = callManager.getCallInfo(from, to, requestUser, domain, display);
			logger.debug("End getCallInfo, from: " + from.toString() + " to: " + to.toString() + " domain: " + domain);
			return callInfo;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallInfo was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 * @throws UserNotFoundException 
	 * @throws DefaultUserNotFoundException 
	 * @throws ValidationException 
	 */
	@SuppressWarnings("unchecked")
	public RouteInfo getRouteInfo(CallInfo callInfo, boolean enableForward, ValidationModes mode) throws PBXServerException, UserNotFoundException, LoopDetected, DefaultUserNotFoundException, ValidationException
	{
		try
		{
			logger.debug("Start getRouteInfo, callInfo: \n" + callInfo.toString() + " enableForward: " + enableForward + " validationMode: " + mode.toString());
			logger.debug("Getting RouteInfo");

			RouteInfo routeInfo = callManager.getRouteInfo(callInfo, mode);
			logger.debug("Validation of RouteInfo, routeInfo:" + routeInfo.toString() + " enableForward: " + enableForward + " validationMode: " + mode.toString());

			mode = routeInfo.getFromLeg().isParkAgent() ? ValidationModes.RETURN_CALL_MODE : mode;
			
			routeInfo = callManager.validateRouteInfo(routeInfo, enableForward, mode);
			logger.debug("Final RouteInfo, routeInfo: " + routeInfo);
			logger.debug("Setting targetList");

			routeInfo.setTargetList(callManager.getTargets(routeInfo.getFromLeg(), routeInfo.getToLeg(), routeInfo.getCallInfo().getLocale(), routeInfo.getCallInfo().getRequestUser(), callInfo.getDomain()));
			logger.debug("TargetList: " + routeInfo.getTargetList());
			logger.debug("Setting timeout");

			routeInfo.setTimeout(callManager.setTimeout(routeInfo.getToLeg()));
			logger.debug("Timeout: " + routeInfo.getTimeout());
			logger.debug("End getRouteInfo, routeInfo: \n" + routeInfo.toString());
			return routeInfo;
		}catch(UserNotFoundException e)
		{
			throw e;
		}catch(LoopDetected e)
		{
			throw e;
		} catch(DefaultUserNotFoundException e)
		{
			throw e;
		}catch(ValidationException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getRouteInfo was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 * @throws UserNotFoundException 
	 * @throws DefaultUserNotFoundException 
	 * @throws ValidationException 
	 */
	@SuppressWarnings("unchecked")
	public RouteInfo tryForward(RouteInfo routeInfo, ForwardMode forwardMode) throws PBXServerException, UserNotFoundException, LoopDetected, DefaultUserNotFoundException, ValidationException
	{
		try
		{
			logger.debug("Start tryForward, routeInfo: \n" + routeInfo.toString() + " forwardMode: " + forwardMode);
			logger.debug("Getting tryForward");
			routeInfo = callManager.tryForward(routeInfo, forwardMode);
			logger.debug("Final RouteInfo, routeInfo: " + routeInfo);
			logger.debug("Setting targetList");
			routeInfo.setTargetList(callManager.getTargets(routeInfo.getFromLeg(), routeInfo.getToLeg(), routeInfo.getCallInfo().getLocale(), routeInfo.getCallInfo().getRequestTranferUser(), routeInfo.getCallInfo().getDomain()));
			logger.debug("TargetList: " + routeInfo.getTargetList());
			logger.debug("Setting timeout");			
			routeInfo.setTimeout(callManager.setTimeout(routeInfo.getToLeg()));
			logger.debug("Timeout: " + routeInfo.getTimeout());
			logger.debug("End tryForward, routeInfo: \n" + routeInfo.toString() + " forwardMode: " + forwardMode);
			return routeInfo;
		}catch(UserNotFoundException e)
		{
			throw e;
		}catch(LoopDetected e)
		{
			throw e;
		}catch(DefaultUserNotFoundException e)
		{
			throw e;
		}catch(ValidationException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when tryForward was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<Target> getTargetSipSessionList(RouteInfo routeInfo)
	{
		try
		{
			logger.debug("Start getTargetSipSessionList, routeInfo: " + routeInfo.toString());
			logger.debug("Execute getTargetList");
			List<Target> targetList = callManager.getTargetList(routeInfo.getToLeg(), routeInfo.getTargetList());
			logger.debug("Execute getContactList");
			sessionManager.getContactList(routeInfo, targetList);
			logger.debug("Next targets: " + targetList);
			logger.debug("End getTargetSipSessionList");
			return targetList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getTargetSipSessionList was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<Target> getTargets(Leg fromLeg, Leg toLeg, String locale, SipAddressParser transferRequestUsername, String domain)
	{
		try
		{
			logger.debug("Start getTargets");
			logger.debug("Execute getTargets");
			
			List<Target> targetList = callManager.getTargets(fromLeg, toLeg, locale, transferRequestUsername, domain);
			
			logger.debug("End getTargets");
			return targetList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getTargets was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<Target> getMediaTargetSipSessionList(RouteInfo routeInfo)
	{
		try
		{
			logger.debug("Start getMediaTargetSipSessionList, routeInfo: " + routeInfo.toString());
			logger.debug("Execute getTargetList");
			
			String userName = User.MUSICSERVER_NAME;
			String domain = routeInfo.getCallInfo().getDomain();
			
			List<Target> targetList = new ArrayList<Target>();;
			Target t = new Target(userName, domain);
			targetList.add(t);
			logger.debug("Execute getContactList");
			sessionManager.makeMediaTargets(targetList);
			routeInfo.setTargetList(targetList);
			
			logger.debug("Next targets: " + targetList);
			logger.debug("End getTargetSipSessionList");
			return targetList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getTargetSipSessionList was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void updateGatewayLastHit(Target completedCall, List<Target> gatewaysTried, boolean isFailed)
	{
		try
		{
			logger.debug("Start updateGatewayLastHit, target: " + completedCall);
			gatewayManager.updateGatewayLastHit(completedCall, gatewaysTried, isFailed);
			logger.debug("End updateGatewayLastHit, target: " + completedCall);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when updateGatewayLastHit was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public SipAddressParser verifyCorrectToAddress(RouteInfo routeInfo, SipAddressParser toAddress)
	{
		try
		{
			logger.debug("Start verifyCorrectToAddress, routeiInfo: " + routeInfo.toString());
			SipAddressParser sipTo = callManager.verifyCorrectToAddress(routeInfo, toAddress);
			logger.debug("End verifyCorrectToAddress, routeiInfo: " + routeInfo.toString());
			return sipTo;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when verifyCorrectToAddress was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public SipAddressParser verifyCorrectFromAddress(RouteInfo routeInfo,SipAddressParser originalFrom)
	{
		try
		{
			logger.debug("Start verifyCorrectFromAddress, routeInfo: " + routeInfo.toString()+" originalfrom: "+originalFrom);
			SipAddressParser sipFrom = callManager.verifyCorrectFromAddress(routeInfo, originalFrom);
			logger.debug("End verifyCorrectFromAddress, routeInfo: " + routeInfo.toString()+" originalfrom: "+originalFrom);
			return sipFrom;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when verifyCorrectFromAddress was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public RouteInfo verifyCorrectRouteInfo(Long ownerKey, RouteInfo routeInfo)
	{
		try
		{
			logger.debug("Start verifyCorrectFromAddress, routeInfo: " + routeInfo.toString()+" ownerKey: "+ownerKey);
			RouteInfo correctRouteInfo= callManager.verifyCorrectRouteInfo(ownerKey, routeInfo);
			logger.debug("End verifyCorrectFromAddress, routeInfo: " + routeInfo.toString()+" ownerKey: "+ownerKey);
			return correctRouteInfo;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when verifyCorrectFromAddress was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	
	
	//callback
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<CallBackInfo> getCallBackInfoListByDomainKey(Long domainKey)
	{
		try
		{
			logger.debug("Start getCallBackInfoListByDomainKey, domainKey: " + domainKey);
			List<CallBackInfo> list = callBackManager.getCallBackInfoListByDomainKey(domainKey);
			logger.debug("End getCallBackInfoListByDomainKey, domainKey: " + domainKey);
			return list;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallBackInfoListByDomainKey was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public CallBackInfo getCallBackInfoByKey(Long callbackKey)
	{
		try
		{
			logger.debug("Start getCallBackInfoByKey, callbackKey: " + callbackKey);
			 CallBackInfo callbackInfo = callBackManager.getCallBackInfoByKey(callbackKey);
			logger.debug("End getCallBackInfoByKey, callbackKey: " + callbackKey);
			return callbackInfo;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCallBackInfoByKey was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void setCallBackOff(Callback callback)
	{
		try
		{
			logger.debug("Start setCallBackOff");
			callBackManager.setCallBackOff(callback);
			logger.debug("End setCallBackOff");
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when setCallBackOff was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<CallBackInfo> getClickToDialListByDomainKey(Long domainKey)
	{
		try
		{
			logger.debug("Start getClickToDialListByDomainKey, domainKey: " + domainKey);
			List<CallBackInfo> list = callBackManager.getClickToDialListByDomainKey(domainKey);
			if(list.size() > 0)
				logger.debug("End getClickToDialListByDomainKey, domainKey: " + domainKey + " there are " + list.size() + " Click to Dial to be executed!");
			return list;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getClickToDialListByDomainKey was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void updateCallBack(Callback callBack)
	{
		try
		{
			logger.debug("Start updateCallBack, callBack: " + callBack.toString());
			callBackManager.saveCallBack(callBack);
			logger.debug("End updateCallBack, callBack: " + callBack.toString());
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when updateCallBack was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public CallBackInfo decrememtCallBackAttempt(String from, String to)
	{
		try
		{
			logger.debug("Start decrementCallBackAttempt, from: " + from+ " to: " + to);
			CallBackInfo callBackInfo = callBackManager.decrementCallBackAttempt(from, to);
			logger.debug("End decrementCallBackAttempt, from: " + from+ " to: " + to);
			return callBackInfo;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when decrementCallBackAttempt was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void removeCallBack(Long callBackKey)
	{
		try
		{
			logger.debug("Start removeCallBack, callBackKey: " + callBackKey);
			callBackManager.removeCallBackByKey(callBackKey);
			logger.debug("End removeCallBack, callBackKey: " + callBackKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when removeCallBack was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void removeCallBack(String from, String to)
	{
		try
		{
			logger.debug("Start executeRemoveCallBack, from: " + from+ " to: " + to);
			callBackManager.removeCallBack(from, to);
			logger.debug("End executeRemoveCallBack, from: " + from+ " to: " + to);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeRemoveCallBack was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	//callLog
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Calllog createLog(Long ownerKey, boolean isOwner, SipAddressParser sipFrom, SipAddressParser sipTo, String display, String callID, String userAgent, int callStatus, Long anotherPbxuserKey, String myAddress)
	{
		try
		{
			logger.debug("Start createLog");
			Calllog log = callLogManager.createCallLog(ownerKey, isOwner, sipFrom, sipTo, display, callID, userAgent, callStatus, anotherPbxuserKey, myAddress);
			logger.debug("End createLog");
			return log;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveCallLog was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void saveCallLog(Collection<Calllog> logs)
	{
		try
		{
			logger.debug("Start saveCallLog, log");
			callLogManager.saveCallLog(logs);
			logger.debug("End saveCallLog, log");
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveCallLog was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
		
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void saveCallLog(Calllog log)
	{
		try
		{
			logger.debug("Start saveCallLog, log");
			callLogManager.saveCallLog(log);
			logger.debug("End saveCallLog, log");
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveCallLog was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void removeCallLog(Calllog log)
	{
		try
		{
			logger.debug("Start removeCallLog, log");
			callLogManager.removeCalllog(log);
			logger.debug("End removeCallLog, log");
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when removeCallLog was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Systemcalllog saveSystemCalllog(Systemcalllog systemCalllog)
	{
		try
		{
			logger.debug("Start saveSystemCalllog, systemCalllog");
			callLogManager.saveSystemCalllog(systemCalllog);
			logger.debug("End saveSystemCalllog, systemCalllog");
			return systemCalllog;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveSystemCalllog was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	//activecall
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void saveActivecall(Activecall ac)
	{
		try
		{
			logger.debug("Start saveActivecall, activecall: " + ac.toString());
			callManager.saveActivecall(ac);
			logger.debug("End saveActivecall, activecall: " + ac.toString());
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void removeActivecall(Long acKey)
	{
		try
		{
			logger.debug("Start removeActivecall, activecall: " + acKey);
			callManager.removeActivecall(acKey);
			logger.debug("End removeActivecall, activecall: " + acKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when removeActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void removeActivecallListByCallID(String callID)
	{
		try
		{
			logger.debug("Start removeActivecallListByCallID, callID: " + callID);
			callManager.removeActivecallListByCallID(callID);
			logger.debug("End removeActivecallListByCallID, callID: " + callID);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when removeActivecallListByCallID " +
					"was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}

	}
	
	//pbx server
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<Pbx> getPbxListByFarmIP(String farmIP)
	{
		try
		{
			logger.debug("Start getPbxListByFarmIP, farmIP: " + farmIP);
			List<Pbx> pbxList = pbxManager.getPbxListByFarmIP(farmIP);
			logger.debug("End getPbxListByFarmIP, farmIP: " + farmIP);
			return pbxList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getPbxListByFarmIP was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Address getAddressWithPbxuserAndConfig(String address, String domain)
	{
		try
		{
			logger.debug("Start getAddressWithPbxuserAndConfig, address: " + address + "@" + domain);
			Address add = addManager.getAddressWithPbxuserAndConfig(address, domain);
			logger.debug("End getAddressWithPbxuserAndConfig, address: " + address + "@" + domain);
			return add;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAddressWithPbxuserAndConfig was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Address getAddressWithPbxuser(String address, Long domainKey)
	{
		try
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Start getAddressWithPbxuserAndConfig, address: ").append(address).append(" to domainKey: ").append(domainKey));
			
			Address add = addManager.getAddress(address, domainKey);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("End getAddressWithPbxuserAndConfig, address: ").append(address).append(" to domainKey: ").append(domainKey));
			return add;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAddressWithPbxuserAndConfig was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Address getAddressWithPbxuserAndConfig(String address)
	{
		try
		{
			logger.debug("Start getAddressWithPbxuserAndConfig, address: " + address);
			Address add = addManager.getAddressWithPbxuserAndConfig(address);
			logger.debug("End getAddressWithPbxuserAndConfig, address: " + address);
			return add;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAddressWithPbxuserAndConfig was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void clearPBX(Long pbxKey)
	{
		try
		{
			logger.debug("Start clearPBX, key: " + pbxKey);
			pbxManager.clearPBX(pbxKey);
			logger.debug("End clearPBX, key: " + pbxKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when clearPBX was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
        
        /**
         * @ejb.interface-method view-type="remote"
         * @generated
         * @ejb.transaction type="Required"
         * @throws PBXServerException
         */
    public Pbxuser getAssociatedPbxUserByTerminalPbxuserKey(Long terminalPbxuserkey)
    {
        try
        {
        	if(logger.isDebugEnabled())
        		logger.debug(new StringBuilder("Getting assosiated PBX user by Terminal key ").append(terminalPbxuserkey).toString());
        	
            Pbxuser pbxUser = terminalManager.getAssociatedPbxUserByTerminalPbxuserKey(terminalPbxuserkey);

            if(pbxUser != null)
            {
            	if(logger.isDebugEnabled())
            		logger.debug(new StringBuilder("PBX user with key ").append(pbxUser.getKey()).append("\" was found").toString());
                return pbxUser;
            } else
            {
            	if(logger.isDebugEnabled())
            		logger.debug(new StringBuilder("No PBX user associated with Terminal key ").append(terminalPbxuserkey).toString());
                return null;
            }
        } catch (Throwable t)
        {
            PBXServerException ex = new PBXServerException("A error ocurrs getting associated PBX user by Terminal key", t);
            logger.error(ex.getLocalizedMessage());
            throw ex;
        }
    }

    /**
     * @ejb.interface-method view-type="remote"
     * @generated
     * @ejb.transaction type="Required"
     * @throws PBXServerException
     */
    public Pbxuser getPbxuserByAddressAndDomain(String address, String domain)
    {
        try
        {
            logger.debug("Getting assosiated PBX user and User by address: " + address + "and domain : " + domain);
            return pbxUserManager.getPbxuserByAddressAndDomain(address, domain);
        } catch (Throwable t)
        {
            PBXServerException ex = new PBXServerException("A error ocurrs getting associated PBX user by address and domain", t);
            logger.error(ex.getLocalizedMessage());
            throw ex;
        }
    }    

    /**
     * @ejb.interface-method view-type="remote"
     * @generated
     * @ejb.transaction type="Required"
     * @throws PBXServerException
     */
    public Pbxuser getPbxuserByTerminal(String terminal, String domain)
    {
        try
        {
            logger.debug("Getting assosiated PBX user and User by terminal: " + terminal + "and domain : " + domain);
            return pbxUserManager.getPbxuserByTerminal(terminal, domain);
        } catch (Throwable t)
        {
            PBXServerException ex = new PBXServerException("A error ocurrs getting associated PBX user by terminal and domain", t);
            logger.error(ex.getLocalizedMessage());
            throw ex;
        }
    }
    
    /**
     * @ejb.interface-method view-type="remote"
     * @generated
     * @ejb.transaction type="Required"
     * @throws PBXServerException
     */
    public Pbxuser getPbxuserAndUser(Long pbxuserKey)
    {
        try
        {
            logger.debug("Getting assosiated PBX user and User by PBX user key " + pbxuserKey);
            return pbxUserManager.getPbxuserAndUser(pbxuserKey);
        } catch (Throwable t)
        {
            PBXServerException ex = new PBXServerException("A error ocurrs getting associated PBX user by Terminal key", t);
            logger.error(ex.getLocalizedMessage());
            throw ex;
        }
    }
	
	/**
     * @ejb.interface-method view-type="remote"
     * @generated
     * @ejb.transaction type="Required"
     * @throws PBXServerException
     */
    public Long getPbxuserKeyByAddressAndDomainKey(String extension, Long domainKey)
    {
    	try
        {
    		if(logger.isDebugEnabled())
    			logger.debug(new StringBuilder("Getting PBX user and User by extension ").append(extension).append(" and domain Key ").append(domainKey));
            return pbxUserManager.getPbxuserKeyByAddressAndDomainKey(extension, domainKey);
        } catch (Throwable t)
        {
            PBXServerException ex = new PBXServerException("A error ocurrs getting pbxuserKey by extension and domain key", t);
            logger.error(ex.getLocalizedMessage());
            throw ex;
        }
    }
	
	// command and config
	/**
     * @throws CommandConfigException
     * @ejb.interface-method view-type="remote"
     * @generated
     * @ejb.transaction type="Required"
     * @throws PBXServerException
     */
	public void executeLoginACD(SipAddressParser sipFrom, String extension) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeLoginACD, from: " + sipFrom.toString());
			//Extension > 0 contem ramal do usuário para Login CallCenter
			//Extension vazia é Login ACD.
			if(extension.length() > 0)
				configManager.validateCommandOwner(sipFrom, extension, true);
			else
				configManager.validateCommandOwner(sipFrom);
			groupManager.executeLoginACD(sipFrom);
			logger.debug("End executeLoginACD, from: " + sipFrom.toString());
		}catch(LoginACDException e)
		{
			throw e;
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeLoginACD was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void executeLogoutACD(SipAddressParser sipFrom, String extension) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeLogoutACD, from: " + sipFrom.toString());
			if(extension.length()>0)
				configManager.validateCommandOwner(sipFrom, extension, false);
			else
				configManager.validateCommandOwner(sipFrom);
			groupManager.executeLogoutACD(sipFrom);
			logger.debug("End executeLogoutACD, from: " + sipFrom.toString());
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeLogoutACD was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecallToCapture(String extensionToCapture, String capturer, String domain)
	{
		try
		{
			logger.debug("Start getActivecallToCapture, capturer: " + capturer + "@" + domain + " extensionToCapture:" + extensionToCapture);
			Activecall ac = callManager.getActivecallToCapture(extensionToCapture, capturer, domain);
			logger.debug("End getActivecallToCapture, capturer: " + capturer + "@" + domain + " extensionToCapture:" + extensionToCapture);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecallToCapture was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecall(Long activecallKey)
	{
		try
		{
			logger.debug("Start getActivecall by activeCallKey: " + activecallKey);
			Activecall ac = callManager.getActivecall(activecallKey);
			logger.debug("End getActivecall by activeCallKey: " + activecallKey);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActiveCall by activeCallKey: " + activecallKey + " was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecall(String address, String domain)
	{
		try
		{
			logger.debug(new StringBuilder("Start getActivecall, from: ").append(address).append("@").append(domain).toString());
			Activecall ac = callManager.getActivecall(address, domain);
			logger.debug(new StringBuilder("End getActivecall, from: ").append(address).append("@").append(domain).toString());
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecall(String address, String domain, int state)
	{
		try
		{
			logger.debug("Start getActivecall, from: " + address + "@" + domain);
			Activecall ac = callManager.getActivecall(address, domain, state);
			logger.debug("End getActivecall, from: " + address + "@" + domain);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecall(Long pbxuserKey, int state)
	{
		try
		{
			logger.debug("Start getActivecall by pbxuser, pbxuserkey: " + pbxuserKey);
			Activecall ac = callManager.getActivecall(pbxuserKey, state);
			logger.debug("End getActivecall by pbxuserKey, pbxuserkey: " + pbxuserKey);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecall(Long pbxuserKey, String callId)
	{
		try
		{
			logger.debug("Start getActivecall by Pbxuser, pbxuserkey: " + pbxuserKey + " on callId: "+callId);
			Activecall ac = callManager.getActivecall(pbxuserKey, callId);
			logger.debug("End getActivecall by Pbxuser, pbxuserkey: " + pbxuserKey+ " on callId: "+callId);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecall(String address, String domain, int state, String callId)
	{
		try
		{
			logger.debug("Start getActivecall, from: " + address + "@" + domain);
			Activecall ac = callManager.getActivecall(address, domain, state, callId);
			logger.debug("End getActivecall, from: " + address + "@" + domain);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecallInGroup(String address, String domain, int state)
	{
		try
		{
			logger.debug("Start getActivecallInGroup, from: " + address + "@" + domain);
			Activecall ac = callManager.getActivecallInGroup(address, domain, state);
			logger.debug("End getActivecallInGroup, from: " + address + "@" + domain);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecallInGroup was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void executeAddCallBack(SipAddressParser sipFrom) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeAddCallBack, from: " + sipFrom.toString());
			configManager.validateCommandOwner(sipFrom);
			callBackManager.executeAddCallBack(sipFrom);
			logger.debug("End executeAddCallBack, from: " + sipFrom.toString());
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeAddCallBack was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void executeRemoveCallBack(SipAddressParser sipFrom) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeRemoveCallBack, from: " + sipFrom.toString());
			configManager.validateCommandOwner(sipFrom);
			callBackManager.executeRemoveCallBack(sipFrom);
			logger.debug("End executeRemoveCallBack, from: " + sipFrom.toString());
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeRemoveCallBack was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void executeDNDConfig(SipAddressParser sipFrom, int dndStatus) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeDNDConfig, from: " + sipFrom);
			configManager.validateCommandOwner(sipFrom);
			configManager.executeDNDConfig(sipFrom, dndStatus);
			logger.debug("End executeDNDConfig, from: " + sipFrom);
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeDNDConfig was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void executeForward(SipAddressParser sipFrom, String target, int forwardMode, int forwardStatus) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeForward, from: " + sipFrom);
			configManager.validateCommandOwner(sipFrom);
			configManager.executeForward(sipFrom, target, forwardMode, forwardStatus);
			logger.debug("End executeForward, from: " + sipFrom);
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeForward was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void executeFollowMe(SipAddressParser sipFrom, String target, int followMeStatus) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeFollowMe, from: " + sipFrom);
			configManager.validateCommandOwner(sipFrom);
			configManager.executeFollowMe(sipFrom, target, followMeStatus);
			logger.debug("End executeFollowMe, from: " + sipFrom);
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeFollowMe was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void executeNightmode(SipAddressParser sipFrom, String groupAddress, int status, int nightmodeType) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeNightmode, from: " + sipFrom+" address: "+groupAddress+" status: "+status+" nightmodeType: "+nightmodeType);
			configManager.validateCommandOwner(sipFrom);
			configManager.executeNightmode(sipFrom, groupAddress, status, nightmodeType);
			logger.debug("End executeNightmode, from: " + sipFrom+" address: "+groupAddress+" status: "+status+" nightmodeType: "+nightmodeType);
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeNightmode was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void executeGroupForward(SipAddressParser sipFrom, String commandData, int forwardMode, int forwardStatus) throws CommandConfigException
	{
		try
		{
			logger.debug("Start executeGroupForward, from: " + sipFrom+" Command Data: "+commandData+" status: "+forwardStatus+" forwardMode :" +forwardMode);
			configManager.validateCommandOwner(sipFrom);
			configManager.executeGroupForward(sipFrom, commandData, forwardStatus, forwardMode);
			logger.debug("End executeGroupForward, from: " + sipFrom+" Command Data: "+commandData+" status: "+forwardStatus+" forwardMode :" +forwardMode);
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executeGroupForward was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Long sendVoiceMailNotifications()
	{
		Long numberNotificationSent = 0l;
		try
		{
			logger.debug("Start sendVoiceMailNotifications");
			numberNotificationSent = voiceMailManager.sendVoiceMailNotifications();
			logger.debug("End sendVoiceMailNotifications");
			return numberNotificationSent;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when sendVoiceMailNotifications was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		} 
		
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Group getGroupByNameAndAssociatedUser(String groupName, String username, String domainName)
	{
		try
		{
			logger.debug("Start getGroupByNameAndAssociatedUser");
			Group group = groupManager.getGroupByNameAndAssociatedUser(groupName, username, domainName);
			logger.debug("End getGroupByNameAndAssociatedUser");
			return group;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getGroupByNameAndAssociatedUser was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public String makeOrignNumber(RouteInfo routeInfo, boolean isFrom)
	{
		try
		{
			logger.debug("Start makeOrignNumber");
			String orignNumber = callManager.makeOrignNumber(routeInfo, isFrom);
			logger.debug("End makeOrignNumber");
			return orignNumber;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when makeOrignNumber was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 
			
		}
	}
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public boolean verifyForwardEnable(Long pbxuserKey, ForwardMode forwardMode)
	{
		try
		{
			logger.debug("Start verifyForwardEnable pbxuserKey: "+pbxuserKey+" ForwardMode: "+forwardMode.name());
			boolean forwardEneble = pbxUserManager.verifyForwardEnable(pbxuserKey,forwardMode);
			logger.debug("End verifyForwardEnable pbxuserKey: "+pbxuserKey+" ForwardMode: "+forwardMode.name());
			return forwardEneble;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when verifyForwardEnable was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 
			
		}
	}
	
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public String getOutgoingAddress(Long domainKey, Leg fromLeg)
	{
		try
		{
			logger.debug("Start getOutgoingAddress");
			String outgoingAddress = callManager.getOutgoingAddress(domainKey, fromLeg);
			logger.debug("End getOutgoingAddress");
			return outgoingAddress;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getOutgoingAddress was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 
			
		}
	}
	
	
	
	/**
 	 * @throws CommandConfigException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public boolean verifyReverseReturnCall(SipAddressParser from, SipAddressParser to, SipAddressParser returnAddress)
	{
		try
		{
			logger.debug("Start verifyReverseReturnCall");
			boolean isReverse = callManager.verifyReverseReturnCall(from,to,returnAddress);
			logger.debug("End verifyReverseReturnCall");
			return isReverse;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when verifyReverseReturnCall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 
			
		}
	}
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<NightModeScheduler> getNightModeSchedulerByPbx(Long pbxKey)
	{
		try
		{
			return nigthModeSchedulerManager.getNightModeSchedulerListByPbxKey(pbxKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getNightModeSchedulerByPbx was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void changeNightModeStatus(Long pbxKey, int status) throws RemoteException
	{
		try
		{
			pbxManager.changeNightModeStatus(pbxKey, status);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getNightModeSchedulerByPbx was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public List<Pbx> getAllPbxWithPbxPreference() throws RemoteException
	{
		try
		{
			return pbxManager.getAllPbxWithPbxPreference();
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAllPbxWithPbxPreference was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void saveRecordFile(RecordFile recordFile) throws RemoteException
	{
		try
		{
			recordFileManager.save(recordFile);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveRecordFile was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void changeEletronicLockStatus(SipAddressParser sipFrom, int status, String pin) throws RemoteException{
		try
		{
			configManager.validateCommandOwner(sipFrom);
			pbxUserManager.changeEletronicLockStatus(sipFrom, status, pin);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveRecordFile was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Pbxpreference getPbxPreference(Long pbxKey) throws RemoteException{
		try
		{
			return pbxManager.getPbxPreference(pbxKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveRecordFile was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Pbxpreference getPbxPreference(String domain) throws RemoteException{
		try
		{
			return pbxManager.getPbxPreference(domain);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveRecordFile was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public void changeACDLoginStatus(Long puKey, boolean logged) throws RemoteException
	{
		try
		{
			groupManager.changeACDLogin(puKey, logged);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveRecordFile was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	
	/**
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public CostCenter getCostCenter(Long key) throws RemoteException
	{
		try
		{
			return costCenterManager.getCostCenter(key);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getCostCenter was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex; 

		}
	}
	
	/**
     * @throws CommandConfigException
     * @ejb.interface-method view-type="remote"
     * @generated
     * @ejb.transaction type="Required"
     * @throws PBXServerException
     */
	public void executePause(SipAddressParser sipFrom, String pauseCode) throws CommandConfigException
	{
		try
		{
			logger.debug(new StringBuilder("Start executePause, from: ").append(sipFrom.toString()));
			configManager.validateCommandOwner(sipFrom);
			groupManager.executePause(sipFrom, pauseCode);
			logger.debug(new StringBuilder("End executePause, from: ").append(sipFrom.toString()));
		}catch(CommandConfigException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when executePause was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
}