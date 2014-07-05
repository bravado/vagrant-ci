package br.com.voicetechnology.ng.ipx.rule.call.localesettings.locale;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.rule.call.localesettings.CallLocaleSettings;

public class BrazilCallLocaleSettings extends BasicCallLocaleSettings implements CallLocaleSettings
{
	private final String BRAZIL_LOCALE = "pt_br";

	public BrazilCallLocaleSettings() throws DAOException 
	{
		super();
	}
	
	public CallInfo getCallInfo(SipAddressParser originalFrom, SipAddressParser originalTo, SipAddressParser requestAddress, String domain, String display) throws DAOException
	{
		SipAddressParser from = new SipAddressParser(originalFrom.getAddress());
		SipAddressParser to = makeTo(originalTo.getExtension(), originalTo.getDomain());
		return makeCallInfo(originalFrom, from, originalTo, to, requestAddress, domain, display, BRAZIL_LOCALE);
	}

	public DisplaySettings getToDisplaySettings(SipAddressParser originalTo, boolean hideANI)
	{
		return hideANI ? DisplaySettings.HIDE : DisplaySettings.SHOW;
	}

	public String makePrefix(String prefix, String domainPrefix, boolean hideANI)
	{
		if(domainPrefix != null)
			return domainPrefix;
		
		return prefix;
	}
	
	public String makeOrignNumber(RouteInfo routeInfo, boolean isFrom) throws DAOException
	{
		return super.makeOrignNumber(routeInfo, isFrom);
	}


}
