package br.com.voicetechnology.ng.ipx.rule.call.localesettings;

import java.util.Hashtable;
import java.util.Map;

import br.com.voicetechnology.ng.ipx.rule.call.localesettings.locale.BrazilCallLocaleSettings;
import br.com.voicetechnology.ng.ipx.rule.call.localesettings.locale.JapanCallLocaleSettings;
import br.com.voicetechnology.ng.ipx.rule.call.localesettings.locale.USACallLocaleSettings;

public class CallLocaleSettingsFactory 
{
	static final private String JAPAN = "ja_jp";
	static final private String USA = "en_us";
	static final private String BRAZIL = "pt_br";
	
	private static Map<String, CallLocaleSettings> map;
	
	public static CallLocaleSettings getCallLocaleSettings(String locale)
	{
		if(map == null)
			map = makeFactory();
		CallLocaleSettings settings = map.get(locale.toLowerCase());
		if(settings != null)
			return settings;
		throw new CallLocaleSettingsNotFound("Locale: " + locale + " not found, please check your settings!"); 
	}
	
	private static Map<String, CallLocaleSettings> makeFactory()
	{
		try
		{
			Map<String, CallLocaleSettings> factoryMap = new Hashtable<String, CallLocaleSettings>();
			factoryMap.put(JAPAN, new JapanCallLocaleSettings());
			factoryMap.put(USA, new USACallLocaleSettings());
			factoryMap.put(BRAZIL, new BrazilCallLocaleSettings());
			return factoryMap;
		}catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
	}
	
}
