package br.com.voicetechnology.ng.ipx.rule.call.localesettings;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;

public interface CallLocaleSettings 
{

	public enum DisplaySettings
	{
		SHOW, HIDE;
	}
	
	public CallInfo getCallInfo(SipAddressParser from, SipAddressParser to, SipAddressParser requestAddress, String domain, String display) throws Exception;
	
//	public SipAddressParser makeTo(SipAddressParser sipAddress);
	
	public DisplaySettings getToDisplaySettings(SipAddressParser originalTo, boolean hideANI);
	
	public String makePrefix(String prefix, String domainPrefix, boolean hideANI);
	
	public String makeOrignNumber(RouteInfo routeInfo, boolean isFrom) throws DAOException;
}
