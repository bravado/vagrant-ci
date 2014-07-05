package br.com.voicetechnology.ng.ipx.rule.call.localesettings.locale;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.KoushiKubunNotAllowedException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.UserNotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.CallInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.rule.call.localesettings.CallLocaleSettings;

public class JapanCallLocaleSettings extends BasicCallLocaleSettings implements CallLocaleSettings 
{
	private final String HIDE_ANI = "184\\d{6,}";
	private final String SHOW_ANI = "186\\d{6,}";
	
	private final String KOUSHI_KUBUN_NUMBER;
	private final String JAPAN_LOCALE = "ja_jp";

	public JapanCallLocaleSettings() throws DAOException 
	{
		super();
		KOUSHI_KUBUN_NUMBER = IPXProperties.getProperty(IPXPropertiesType.KOUSHI_KUBUN_NUMBER) + IPXProperties.getProperty(IPXPropertiesType.KOUSHI_KUBUN_PREFIX);
	}

	public CallInfo getCallInfo(SipAddressParser originalFrom, SipAddressParser originalTo, SipAddressParser requestAddress, String domain, String display) throws DAOException, UserNotFoundException, KoushiKubunNotAllowedException
	{
		boolean isKoushiKubun = originalTo.getExtension().startsWith(KOUSHI_KUBUN_NUMBER);
		SipAddressParser from = makeFrom(originalFrom, isKoushiKubun);
		SipAddressParser to = makeTo(originalTo, isKoushiKubun);
		return makeCallInfo(originalFrom, from, originalTo, to, requestAddress, domain, display, JAPAN_LOCALE);
	}
	
	private SipAddressParser makeFrom(SipAddressParser from, boolean isKoushiKubun) throws UserNotFoundException, DAOException, KoushiKubunNotAllowedException
	{
		if(!isKoushiKubun)
			return new SipAddressParser(from.getAddress());
		
		Pbxuser pu = puDAO.getKoushiKubunOwnerByNumber(from.getExtension());
		if(pu == null)
			throw new UserNotFoundException("Koushi kubun user not found, please check your config!!!", true);
		
		if(pu.getConfig().getAllowedKoushiKubun() != Config.ALLOWED_KOUSHIKUBUN)
			throw new KoushiKubunNotAllowedException("User don't have permission to do Koushi Kubun");
			
		
		return new SipAddressParser(pu.getUser().getUsername(), pu. getUser().getDomain().getDomain());
	}
	
	private SipAddressParser makeTo(SipAddressParser sipAddress, boolean isKoushiKubun) throws DAOException
	{
		String extension = isKoushiKubun ? sipAddress.getExtension().substring(KOUSHI_KUBUN_NUMBER.length()) : sipAddress.getExtension();
		if(extension.matches(HIDE_ANI) || extension.matches(SHOW_ANI))
			return new SipAddressParser(extension.substring(3), sipAddress.getDomain());
		else
			return super.makeTo(extension, sipAddress.getDomain());		
	}

	public DisplaySettings getToDisplaySettings(SipAddressParser originalTo, boolean hideANI)
	{
		boolean isKoushiKubun = originalTo.getExtension().startsWith(KOUSHI_KUBUN_NUMBER);
		String extension = isKoushiKubun ? originalTo.getExtension().substring(KOUSHI_KUBUN_NUMBER.length()) : originalTo.getExtension();
		if(extension.matches(SHOW_ANI))
			return DisplaySettings.SHOW;
		if(extension.matches(HIDE_ANI))
			return DisplaySettings.HIDE;
		return hideANI ? DisplaySettings.HIDE : DisplaySettings.SHOW;
	}

	public String makePrefix(String prefix, String domainPrefix, boolean hideANI)
	{
		if(hideANI)
			return prefix != null ? prefix + "184" : "184";
		return prefix != null ? prefix : null;
	}

	public String makeOrignNumber(RouteInfo routeInfo, boolean isFrom) throws DAOException
	{
		if(routeInfo.getCallInfo().getOriginalTo().getExtension().startsWith(KOUSHI_KUBUN_NUMBER))
			return routeInfo.getCallInfo().getOriginalFrom().getAddress();
		else
			return super.makeOrignNumber(routeInfo, isFrom);			
	}

}
