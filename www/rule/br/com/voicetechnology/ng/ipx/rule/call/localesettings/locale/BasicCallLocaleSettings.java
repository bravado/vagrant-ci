package br.com.voicetechnology.ng.ipx.rule.call.localesettings.locale;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CommandType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ConnectionCreationType;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.rule.implement.CallManager;

public abstract class BasicCallLocaleSettings extends CallManager
{
	private final String SAXA_PARK = "\\*40\\d{1,}";
	
	public BasicCallLocaleSettings() throws DAOException 
	{
		super((String) null);
	}
	
	protected CallInfo makeCallInfo(SipAddressParser originalFrom, SipAddressParser from, SipAddressParser originalTo, SipAddressParser to, SipAddressParser requestAddress, String domain, String display, String locale) throws DAOException
	{
		boolean isFromTerminal = isTerminal(from, domain);
		if(isToVoiceMail(domain, to))
			return new CallInfo(from, to, requestAddress, domain, true, isFromTerminal, display, locale);
		
		boolean isParkPosition = isParkPosition(to, domain);
		SipAddressParser tmp = !isParkPosition ? isSpeeddial(from, to, domain, isFromTerminal) : to;
		boolean isToTerminal = isTerminal(tmp, domain);
		boolean isSpeedDial = !to.equals(tmp);
		boolean isSpyCommand = isSpyCommand(tmp);
		boolean isCommandConfig = isParkPosition && parkDAO.isPositionBusy(tmp.getExtension(), domain) ? true : isCommandConfig(tmp) && !isSpyCommand; //TODO REFACTORY: remover validacao de posicao de park disponivel.
		boolean isToGroupRing = isCommandConfig || isParkPosition ? false : isToGroupRing(domain, to);
	
		return new CallInfo(from, originalFrom, tmp, originalTo, requestAddress, isSpeedDial, isCommandConfig, isParkPosition, isSpyCommand, isFromTerminal, isToTerminal, isToGroupRing, domain, display, locale);
	}
	
	protected boolean isSpyCommand(SipAddressParser to)
	{
		String spyCommand = new StringBuilder("*").append(CommandType.SPY_COMMAND_CALL_CENTER).toString();
		if(to.getExtension().startsWith(spyCommand))
		{
			to.setExtension(to.getExtension().replace(spyCommand, ""));
			return true;
		}
		
		return false;		
	}
	
	protected boolean isToVoiceMail(String domain, SipAddressParser sipTo) throws DAOException
	{
		List<Address> addList = addDAO.getVoicemailAddressList(domain);
		for(Address add : addList)
			if(sipTo.getExtension().equals(add.getAddress()))
				return true;
		return false;
	}

	protected boolean isParkPosition(SipAddressParser sipTo, String domain) throws DAOException
	{
		//dnakamashi - Flexible Dial Plan - version 3.0.6	
		if(sipTo.getExtension().startsWith("*"))
			return false;
		
		String to = sipTo.getExtension();				
		if(!isNumber(to) || to.length() > DialPlan.MAX_DIGITS)
			return false;
		
		DialPlan parkDialPlan = dPlanDAO.getDialPlanByTypeAndDomain(DialPlan.TYPE_PARK, domain);//dnakamashi --> 3.0.6 - Flexible Dial Plan		
		if(parkDialPlan == null)		
			return false;				
		//inicio --> dnakamashi - bug #6280 - version 3.0.6
		int pos = Integer.parseInt(to);
		boolean isInRange =  pos >= parkDialPlan.getStart().intValue() && pos <= parkDialPlan.getEnd().intValue();
		if(isInRange)			
			return true;
		
		return false;
		//fim --> dnakamashi - bug #6280 - version 3.0.6		
	}

	protected boolean isTerminal(SipAddressParser sipAddress, String domain) throws DAOException
	{
		Terminal t = tDAO.getTerminalByAddressAndDomain(sipAddress.getExtension(), domain);
		return t != null;
	}
	
	private boolean isToGroupRing(String domain, SipAddressParser sipTo) throws DAOException
	{
		Group group = gDAO.getGroupByAddressAndDomain(sipTo.getExtension(), domain);		
		
		if(group!= null && group.getGroupType() == Group.RING_GROUP)
			return true;		
		return false;
	}
	
	public String makeOrignNumber(RouteInfo routeInfo, boolean isFrom) throws DAOException
	{
		if(!isFrom && routeInfo.hasPreviousRouteInfo() && routeInfo.getToLeg().isPSTN() && routeInfo.getPreviousRouteInfo().getFromLeg().isPbxuser())
			return getOutgoingAddress(routeInfo.getPreviousRouteInfo().getFromLeg().getUser().getDomain().getDomain(), routeInfo.getPreviousRouteInfo().getFromLeg())+"@"+routeInfo.getPreviousRouteInfo().getFromLeg().getUser().getDomain().getDomain();
		else if((routeInfo.hasPreviousRouteInfo()) && (isFrom || routeInfo.getCreationCause() == ConnectionCreationType.FORWARD))//** 
			return routeInfo.getPreviousRouteInfo().getFromLeg().getSipAddress().getAddress();
		else
			return routeInfo.getFromLeg().getSipAddress().getAddress();
		
		//**dnakamashi-adicionada validação que verifica se a causa da criação da routeinfo foi um FORWARD, pois a configuração my/from display
		//influenciava no CDR
	}
	
	//dnakamashi -- Flexible DialPlan
	//Alterada a verificação de Park do Saxa
	protected SipAddressParser makeTo(String extension, String domain) throws DAOException
	{
		if(extension.matches(SAXA_PARK))
		{
			DialPlan parkDialPlan = dPlanDAO.getDialPlanByTypeAndDomain(DialPlan.TYPE_PARK, domain);
			String parkPosition = extension.substring(3);
			
			if(parkPosition.length() > 4)
				return new SipAddressParser(extension, domain);
			
			if(parkDialPlan.getStart() <= Integer.valueOf(parkPosition) && parkDialPlan.getEnd() >= Integer.valueOf(parkPosition))				
				return new SipAddressParser(extension.substring(3), domain);
		}		
		
		return new SipAddressParser(extension, domain);
	}

}
