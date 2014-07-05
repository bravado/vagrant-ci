package br.com.voicetechnology.ipx.mail.formatter;

import java.util.HashMap;
import java.util.Map;

import br.com.voicetechnology.ipx.mail.pojo.Mail;
import br.com.voicetechnology.ipx.mail.server.MailFormatter;

public class CalllogCSVMailFormatter implements MailFormatter
{
	private Map<String, Object> values = new HashMap<String, Object>();
	public void makeMail(Mail mail) throws Exception
	{
		mail.setSubject((String) getParam("subject"));
		mail.setContent((String) getParam("content"));		
		mail.setHTML(true);
	}

	public Object getParam(String key)
	{		
		return values.get(key);
	}

	public void addParam(String key, Object obj)
	{
		values.put(key, obj);		
	}
}
