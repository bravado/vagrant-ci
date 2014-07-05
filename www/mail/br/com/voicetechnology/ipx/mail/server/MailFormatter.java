package br.com.voicetechnology.ipx.mail.server;

import br.com.voicetechnology.ipx.mail.pojo.Mail;


public interface MailFormatter
{
	public void makeMail(Mail mail)throws Exception;
	public Object getParam(String key);
	public void addParam(String key, Object obj);
}
