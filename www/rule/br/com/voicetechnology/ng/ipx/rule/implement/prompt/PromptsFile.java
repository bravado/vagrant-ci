package br.com.voicetechnology.ng.ipx.rule.implement.prompt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ipx.prompts.prompt.Prompts;
import br.com.voicetechnology.ng.ipx.pojo.db.prompt.PromptFile;
import br.com.voicetechnology.ng.ipx.pojo.db.voicemail.VMLocales;
import br.com.voicetechnology.ng.ipx.rule.implement.voicemail.DynaPrompts;

public class PromptsFile
{
	private Hashtable<String, Properties> map;
	private Prompts prompts;
	private Logger logger;
	
	public PromptsFile()
	{
		map = createMaps();
		prompts = new Prompts();
	}
	
	private Logger getLogger()
	{
		if(logger == null)
			logger = Logger.getLogger(this.getClass());
		return logger;
	}
	
	public String[] getPromptPath(String locale, PromptFile prompt)
	{
		Properties bundle = map.get(locale.toLowerCase());
		if(bundle == null)
			return null;
		String value =  bundle.getProperty(prompt.toString());
		if(value == null)
			return null;
		String[] values = value.split("\\s+");
		List<String> result = new ArrayList<String>();
		for(String tmp : values)
			addStaticValue(tmp, bundle, result);
		return result.toArray(new String[result.size()]);
	}
	
	public String[] getPromptPath(String locale, PromptFile prompt, DynaPrompts dynamic)
	{
		Properties bundle = map.get(locale.toLowerCase());
		if(bundle == null)
			return null;
		String value =  bundle.getProperty(prompt.toString());
		if(value == null)
			return null;
		String[] values = value.split("\\s+");
		List<String> result = new ArrayList<String>();
		for(String tmp : values)
		{
			if(tmp.startsWith("{"))
				addDynaValue(tmp.substring(1, tmp.length() -1), locale, bundle, dynamic, result);
			else 
				addStaticValue(tmp, bundle, result);
		}
		return result.toArray(new String[result.size()]);
	}
	
	private void addStaticValue(String value, Properties bundle, List<String> result)
	{
		String tmp = bundle.getProperty(value);
		if(tmp == null || tmp.length() == 0)
			result.add(value);
		else
			result.add(tmp);
	}
	
	private void addDynaValue(String property, String locale, Properties bundle, DynaPrompts dynamic, List<String> result)
	{
		if(!property.toUpperCase().equals(dynamic.getProperty().toString()))
			return ;
		for(String tmp : getPromptPath(locale, dynamic))
			if(tmp != null && tmp.length() > 0)
				addStaticValue(tmp, bundle, result);
	}

	public String[] getPromptPath(String locale, DynaPrompts dynamic)
	{
		switch(dynamic.getType())
		{
			case DATE:
				return getPromptPathTime(locale, dynamic.getValueCalendar());
			case NUMBER:
				return getPromptPath(locale, prompts.getPrompts(locale, Prompts.Type.INTEGER, dynamic.getValue()));
			case SPELLED:
				return getPromptPath(locale, prompts.getPrompts(locale, Prompts.Type.SPELLED, dynamic.getValue()));
		}
		return new String[0];
	}
	
	private String[] getPromptPathTime(String locale, Calendar cal)
	{
		String date = cal.get(Calendar.YEAR) + formatDate(cal.get(Calendar.MONTH) + 1) + formatDate(cal.get(Calendar.DAY_OF_MONTH));
		String time = formatDate(cal.get(Calendar.HOUR_OF_DAY)) + formatDate(cal.get(Calendar.MINUTE));
		
		String[] vDate = prompts.getPrompts(locale, Prompts.Type.DATE, date);
		String[] vHour = prompts.getPrompts(locale, Prompts.Type.TIME, time);
		String[] timePrompts = new String[vDate.length + vHour.length];
		
		System.arraycopy(vDate, 0, timePrompts, 0, vDate.length);
		System.arraycopy(vHour, 0, timePrompts, vDate.length, vHour.length);

		return getPromptPath(locale, timePrompts);
	}
	
	private String formatDate(int value)
	{
		return value < 10 ? "0" + value : String.valueOf(value);		
	}
	
	private String[] getPromptPath(String locale, String... prompts)
	{
		Properties bundle = map.get(locale.toLowerCase());
		if(bundle == null)
			return null;	
		String[] result = new String[prompts.length];
		int i = 0;
		for(String tmp : prompts)
			if(tmp != null && tmp.length() > 0)
				result[i++] = bundle.getProperty(tmp);
		return result;
	}
	
	private Hashtable<String, Properties> createMaps()
	{
		VMLocales[] locales = VMLocales.values();
		Hashtable<String, Properties> map = new Hashtable<String, Properties>();
		for(VMLocales locale : locales)
			try
			{
				map.put(locale.toString().toLowerCase(), createLocale(locale.toString().toLowerCase()));
			}catch(Exception e)
			{
				getLogger().error("Cannot create voicemail bundle with locale: " + locale.toString(), e);
			}
		return map;
	}
	
	private Properties createLocale(String locale) throws IOException
	{
		Properties voicemailRules = new Properties();
		String confPath = System.getProperty("jboss.server.config.url") + "basix" + File.separator + "locale";
		confPath = confPath.substring(5);
		
		File file = new File(confPath + File.separator + "voicemail" + File.separator + "voicemail_" + locale.toLowerCase() + ".properties");
		InputStream is = new FileInputStream(file);
		voicemailRules.load(is);
		
		Properties recordFileBoxPrompts = new Properties(voicemailRules);
		file = new File(confPath + File.separator + "recordfilebox" + File.separator + "recordfilebox_" + locale.toLowerCase() + ".properties");
		is = new FileInputStream(file);
		recordFileBoxPrompts.load(is);
		
		Properties eletroniclockPrompts = new Properties(recordFileBoxPrompts);
		file = new File(confPath + File.separator + "ivrpreprocesscall" + File.separator + "ivrpreprocesscall_" + locale.toLowerCase() + ".properties");
		is = new FileInputStream(file);
		eletroniclockPrompts.load(is);
		
		Properties callcenterPrompts = new Properties(eletroniclockPrompts);
		file = new File(confPath + File.separator + "callcenter" + File.separator + "callcenter_" + locale.toLowerCase() + ".properties");
		is = new FileInputStream(file);
		callcenterPrompts.load(is);
		
		Properties promptsMapping = new Properties(callcenterPrompts);
		file = new File(confPath + File.separator + "voicemail" + File.separator + "promptsmapping_" + locale.toLowerCase() + ".properties");
		is = new FileInputStream(file); 
		promptsMapping.load(is);
		return promptsMapping;
	}
}
