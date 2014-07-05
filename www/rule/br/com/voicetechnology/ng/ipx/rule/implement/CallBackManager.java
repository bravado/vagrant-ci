package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.InvalidForwardException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.command.CallBackException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.jms.implement.JMSCallBackPublisher;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallbackDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserterminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipsessionlogDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallBackInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CommandType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callback;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuserterminal;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CallbackInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.call.localesettings.CallLocaleSettings.DisplaySettings;
import br.com.voicetechnology.ng.ipx.rule.call.validationcall.CallBackValidator;
import br.com.voicetechnology.ng.ipx.rule.call.validationcall.ClickToMessageValidator;
import br.com.voicetechnology.ng.ipx.rule.call.validationcall.ClickToTalkValidator;

public class CallBackManager extends CallManager
{
	private PbxuserDAO puDAO;
	private CalllogDAO clDAO;
	private CallbackDAO cbDAO;
	private ActivecallDAO acDAO;
	private SipsessionlogDAO sslDAO;
	private PbxDAO pbxDAO;
	private PbxuserterminalDAO ptDAO;
	private ConfigDAO confDAO;
	
	public CallBackManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		puDAO = dao.getDAO(PbxuserDAO.class);
		clDAO = dao.getDAO(CalllogDAO.class);
		cbDAO = dao.getDAO(CallbackDAO.class);
		acDAO = dao.getDAO(ActivecallDAO.class);
		sslDAO = dao.getDAO(SipsessionlogDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		ptDAO = dao.getDAO(PbxuserterminalDAO.class);
		confDAO = dao.getDAO(ConfigDAO.class);
	}
	
	public ReportResult findCallbacks(Report<CallbackInfo> report) throws DAOException 
	{
		ReportDAO<Callback, CallbackInfo> callbackReportDAO = dao.getReportDAO(CallbackDAO.class);
		//Long size = callbackReportDAO.getReportCount(report);
		List<Callback> callbackList = callbackReportDAO.getReportList(report);
		if(report.getInfo().getFrom() != null && !report.getInfo().getFrom().equals("**"))
			callbackList = manageCallbackResults(callbackList, report.getInfo().getFrom());
		List<CallbackInfo> callbackInfoList = new ArrayList<CallbackInfo>();
		for(Callback callback : callbackList)
		{
			User from = callback.getPbxuserFrom().getUser();
			User to = callback.getPbxuserTo().getUser();
			CallbackInfo info = new CallbackInfo(callback, from.getUsername(), to.getUsername());
			callbackInfoList.add(info);
		}
		return new ReportResult<CallbackInfo>(callbackInfoList, callbackList.size());
	}

	private List<Callback> manageCallbackResults(List<Callback> callBackList, String searched) throws DAOException
	{
		List<Callback> newCallBackList = new ArrayList<Callback>();
		boolean allowAdd = false;
		if(callBackList != null && callBackList.size() > 0)
			for(Callback cb : callBackList)
			{
				allowAdd = isValid(cb, true, searched);
				if(!allowAdd)
					allowAdd = isValid(cb, false, searched);

				if(allowAdd)
					newCallBackList.add(cb);
			}
		return newCallBackList;
	}
	
	private boolean isValid(Callback cb, boolean isSipFrom, String searched)
	{
		searched = searched.replaceAll("\\*", "");
		String[] arraySip = isSipFrom ? cb.getSipFrom().split(":") : cb.getSipTo().split(":");
		String[] arrayDomain = arraySip[1].split("@");
		if((arraySip[0].contains(searched) && !arrayDomain[0].contains(searched)) || (arrayDomain[1].contains(searched) && !arrayDomain[0].contains(searched)))
			return false;
		return true;
	}
	
	public void executeAddCallBack(SipAddressParser sipFrom) throws CallBackException, DAOException, ValidateObjectException, ValidationException
	{
		Pbxuser from = puDAO.getPbxuserByAddressAndDomain(sipFrom.getExtension(), sipFrom.getDomain());
		if(from == null)
			throw new CallBackException("Origination not exists! CallBack only be made between pbxuser and pbxuser!");

		if(from.getUser().getAgentUser().intValue() == User.TYPE_TERMINAL)
			from = puDAO.getPbxuserByTerminal(sipFrom.getExtension(), sipFrom.getDomain());
		
		Calllog cl = clDAO.getByKey(from.getCalllogKey());
        if(cl.getStatus().intValue() != CallStateEvent.DESTINATION_BUSY && cl.getStatus().intValue() != CallStateEvent.UNAVALIABLE)
        	throw new CallBackException("Can't generate call back. Last call was not disconnected by busy cause.");
		
        SipAddressParser sipTo = isSpeeddial(sipFrom, new SipAddressParser(cl.getAddress()), sipFrom.getDomain(), false);
        Pbxuser to = puDAO.getPbxuserByAddressAndDomain(sipTo.getExtension(), sipTo.getDomain());
		if(to == null)
			throw new CallBackException("Destination not exists! CallBack only be made between pbxuser and pbxuser!");
		
		Callback callback = new Callback(from, to, sipFrom, sipTo, Callback.CALLBACK_ATTEMPTS, Callback.TYPE_CALL_BACK);
		
		new CallBackValidator().validate(callback);
		addCallback(callback);
		sendCallbackJMSMessage(callback);
	}
	
	public void addCallback(Callback callback) throws DAOException, ValidateObjectException, ValidationException, CallBackException
	{
		Calendar cal = Calendar.getInstance();
		callback.setFirstTryTime(cal);
		callback.setLastTryTime(cal);
		callback.setExecuting(Callback.EXECUTING_OFF);
		cbDAO.save(callback);
	}

	public void saveCallBack(Callback callBack) throws DAOException, ValidateObjectException
	{
		cbDAO.save(callBack);
	}
	
	public void removeCallBackByKey(Long callBackKey) throws DAOException
	{
		Callback cb = cbDAO.getByKey(callBackKey);
		cbDAO.remove(cb);
	}
	
	public void removeCallBack(String from, String to) throws DAOException
	{
		Callback callBack = cbDAO.getCallbackByFromAndTo(from, to);
		if(callBack != null)
			cbDAO.remove(callBack);
	}
	
	public CallBackInfo decrementCallBackAttempt(String from, String to) throws DAOException, ValidateObjectException
	{
		Callback callback = cbDAO.getCallbackByFromAndTo(from, to);
		if(callback != null)
		{
			Integer attemptRemains = callback.getCallBackAttempt().intValue() - 1;
			if(attemptRemains == 0)
				cbDAO.remove(callback);
			else
			{
				callback.setCallBackAttempt(attemptRemains);
				callback.setExecuting(Callback.EXECUTING_OFF);
				cbDAO.save(callback);
			}
		
			return getCallBackInfoByKey(callback.getKey());
		}
		return null;
	}
	
	public List<CallBackInfo> getCallBackInfoListByDomainKey(Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Callback> list = cbDAO.getCallbackListByDomainKey(domainKey);
		List<CallBackInfo> callBackInfoList = new ArrayList<CallBackInfo>();
		
		for(Callback callback: list)
		{
			CallBackInfo callBackInfo = new CallBackInfo(callback);
			callBackInfo.setFromIsReady(isReady(callback.getPbxuserFrom()));
			callBackInfo.setToIsReady(isReady(callback.getPbxuserTo()));
			callBackInfoList.add(callBackInfo);
			
			callback.setExecuting(Callback.EXECUTING_ON);
			cbDAO.save(callback);

		}
		
		return callBackInfoList;		
	}
	
	public CallBackInfo getCallBackInfoByKey(Long callbackKey) throws DAOException, ValidateObjectException
	{
		
		CallBackInfo callBackInfo = null;
		Callback callback = cbDAO.getCallbackFullByKey(callbackKey);
		if(callback != null)
		{
			callBackInfo = new CallBackInfo(callback);
			callBackInfo.setFromIsReady(isReady(callback.getPbxuserFrom()));
			callBackInfo.setToIsReady(isReady(callback.getPbxuserTo()));
				
			callback.setExecuting(Callback.EXECUTING_ON);
			cbDAO.save(callback);
		}

		return callBackInfo;		
	}

	public void setCallBackOff(Callback callback) throws DAOException, ValidateObjectException
	{
		callback.setExecuting(Callback.EXECUTING_OFF);
		cbDAO.save(callback);
	}
	
	public List<CallBackInfo> getClickToDialListByDomainKey(Long domainKey) throws DAOException, ValidateObjectException
	{
		List<Callback> callbackList = cbDAO.getClickToDialListByDomainKey(domainKey);
		List<CallBackInfo> callBackInfoList = new ArrayList<CallBackInfo>();
		
		for(Callback callback: callbackList)
		{
			CallBackInfo callBackInfo = new CallBackInfo(callback);
			callBackInfoList.add(callBackInfo);
			
			callback.setExecuting(Callback.EXECUTING_ON);
			cbDAO.save(callback);
		}
		
		return callBackInfoList;
	}
	
	private boolean isReady(Pbxuser pbxuser) throws DAOException
	{
		if(pbxuser.getConfig().getDndStatus().intValue() == Config.DND_ON)
			return false;
		if(acDAO.getActiveCallListByPbxuser(pbxuser.getKey()).size() > 0)
			return false;
		if(sslDAO.getActiveSipsessionlogListByPbxuser(pbxuser.getKey()).size() == 0)
		{
			List<Pbxuserterminal> ptList = ptDAO.getPbxuserterminalByPbxuser(pbxuser.getKey());
			if(ptList.size() > 0)
				for(Pbxuserterminal pt : ptList)
					if(sslDAO.getActiveSipsessionlogListByPbxuser(pt.getTerminal().getPbxuserKey()).size() > 0)
						return true;
			return false;
		}		
		return true;
	}
	
	public void executeRemoveCallBack(SipAddressParser sipFrom) throws DAOException
	{
		List<Callback> cbList = cbDAO.getCallbackListByPbxuser(sipFrom.getExtension(), sipFrom.getDomain());
		for(Callback cb : cbList)
			cbDAO.remove(cb);
	}
	
	public void deleteCallbacks(List<Long> callbackKeyList) throws DAOException 
	{
		for (Long key : callbackKeyList) 
			this.deleteCallback(key);
	}

	public void deleteCallback(Long key) throws DAOException 
	{
		Callback callback = cbDAO.getByKey(key);
		if(callback != null)
			cbDAO.remove(callback);
	}

	public void createClickToMessage(Long pbxuserKey, String toAddress) throws Exception
	{
		Pbxuser pu = puDAO.getPbxuserFull(pbxuserKey);
		Address add = addDAO.getAddress(toAddress, pu.getUser().getDomainKey());
		Config config = null;
		if(add == null)
			throw new ValidateObjectException("Cannot click to message to this address beacause it's voicemail disable!", Config.class, config, ValidateType.INVALID);

		if(add.getPbxuserKey() != null)
			config = confDAO.getByKey(puDAO.getByKey(add.getPbxuserKey()).getConfigKey());
		else if(add.getGroupKey() != null)
			config = confDAO.getByKey(gDAO.getByKey(add.getGroupKey()).getConfigKey());

		if(config == null)
			throw new ValidateObjectException("Cannot click to message to this address beacause it's voicemail disable!", Config.class, config, ValidateType.INVALID);

		if(config.getDisableVoicemail() == Config.VOICEMAIL_OFF)
			throw new InvalidForwardException("Voicemail is off!", CallStateEvent.UNAVALIABLE);
		if(add.getPbxuserKey() != null)
			new ClickToMessageValidator().validate(pu, puDAO.getByKey(add.getPbxuserKey()));
		else 
			new ClickToMessageValidator().validate(pu, gDAO.getByKey(add.getGroupKey()));
		toAddress = CommandType.COMMAND_IDENTIFIER + CommandType.VOICEMAIL + toAddress;
		createClickToTalk(pbxuserKey, toAddress);
	}
	
	public void createClickToTalk(Long pbxuserKey, String toAddress) throws Exception
	{
		Pbxuser from = puDAO.getPbxuserAndUser(pbxuserKey);
		Domain domain = dmDAO.getByKey(from.getUser().getDomainKey());
		SipAddressParser sipFrom = new SipAddressParser(from.getUser().getUsername(), domain.getDomain());
		SipAddressParser sipTo = new SipAddressParser(toAddress, domain.getDomain());

		CallInfo callInfo = getCallInfo(sipFrom, sipTo, null, domain.getDomain(), sipTo.getExtension());
		RouteInfo routeInfo = getRouteInfo(callInfo, ValidationModes.CALLBACK_MODE);
		validateRouteInfo(routeInfo, true, ValidationModes.FULL_MODE);
		
		Pbx pbx = pbxDAO.getPbxByDomain(domain.getKey());
		routeInfo.getFromLeg().getSipAddress().setExtension(from.getUser().getUsername());
		makeDisplay(pbx, routeInfo.getFromLeg(), routeInfo.getToLeg(), false, DisplaySettings.SHOW);		
		makeDisplay(pbx, routeInfo.getToLeg(), routeInfo.getFromLeg(), true, DisplaySettings.SHOW);
		sipFrom.setExtension(from.getUser().getUsername());
		
		// altera��o para click to call realizado para speeddial(shared ou private)
		sipTo.setExtension(routeInfo.getToLeg().getSipAddress().getExtension());

		
		Pbxuser to = routeInfo.getToLeg().isPbxuser() ? routeInfo.getToLeg().getPbxuser() : null;
		Callback callback = new Callback(from, to, sipFrom, sipTo, Callback.CLICK_TO_DIAL_ATTEMPTS, Callback.TYPE_CLICK_TO_DIAL);
		callback.setExecuting(Callback.EXECUTING_OFF);
		callback.setFirstTryTime(Calendar.getInstance());
		
		new ClickToTalkValidator().validate(routeInfo.getFromLeg(), routeInfo.getToLeg());
		
		Calendar cal = Calendar.getInstance();
		callback.setFirstTryTime(cal);
		callback.setLastTryTime(cal);
		callback.setExecuting(Callback.EXECUTING_OFF);
		sendClickToDialJMSMessage(callback);
	}

	public Callback getCallbackByFromAndTo(String from, String to) throws DAOException
	{
		return cbDAO.getCallbackByFromAndTo(from, to);
	}
	
	private void sendClickToDialJMSMessage(Callback callback)
    {
    	JMSCallBackPublisher publisher;
		try
		{
			publisher = new JMSCallBackPublisher();			
			publisher.publishClickToDial(callback);
			publisher.close();
		} catch (Exception e)
		{
			if(logger.isDebugEnabled())
			{
				StringBuilder error = new StringBuilder("Error to send JMS to notify Click-to-dial event: ");
				error.append(" from "+callback.getSipFrom());
				error.append(" to "+callback.getSipTo());
				logger.error(error.toString(), e);
			}
		}
		
    }
	
    private void sendCallbackJMSMessage(Callback callback)
    {
        JMSCallBackPublisher publisher;
        try
        {
            publisher = new JMSCallBackPublisher();         
            publisher.publishCallback(callback);
            publisher.close();
        } catch (Exception e)
        {
            if(logger.isDebugEnabled())
            {
                StringBuilder error = new StringBuilder("Error to send JMS to notify Callback event: ");
                error.append(" from "+callback.getSipFrom());
                error.append(" to "+callback.getSipTo());
                logger.error(error.toString(), e);
            }
        }
    }
}