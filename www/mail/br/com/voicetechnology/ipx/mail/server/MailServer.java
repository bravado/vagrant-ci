package br.com.voicetechnology.ipx.mail.server;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ipx.mail.pojo.Mail;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;


public class MailServer
{
	private static MailServer server;
	
	private String username;
	private String password;
	private String smtpHost;
	private int port;
	private boolean authEnable;
	private boolean sslFactoryEnable;
	
	private Logger logger;
	
	private MailServer()
	{
		logger = Logger.getLogger(MailServer.class);
		java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	}
	
	public synchronized static MailServer getInstance()
	{
		if(server == null)
			server = new MailServer();
		return server;
	}
	
	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getSmtpHost()
	{
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost)
	{
		this.smtpHost = smtpHost;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
	
	public boolean isAuthEnable() 
	{
		return authEnable;
	}

	public void setAuthEnable(boolean authEnable) 
	{
		this.authEnable = authEnable;
	}

	public boolean isSslFactoryEnable()
	{
		return sslFactoryEnable;
	}

	public void setSslFactoryEnable(boolean sslFactoryEnable)
	{
		this.sslFactoryEnable = sslFactoryEnable;
	}

	public void send(MailFormatter formatter) throws Exception
	{
		String to = (formatter.getParam("email") != null) ? (String) formatter.getParam("email") : null;
		String file = (formatter.getParam("file") != null) ? (String) formatter.getParam("file") : null;
		String domain = (formatter.getParam("domain") != null) ? (String) formatter.getParam("domain") : null;
		Mail mail = new Mail(to, "Basix Email", null, true, file != null ? new File(file) : null);	
		formatter.makeMail(mail);
		initSMTPServer(mail, domain);
	}
	
	//inicio --> dnakamashi - bug #6273 - version 3.0.6
	private void initSMTPServer(Mail mail, String domain) throws Exception
	{
		username = null;
		password = null;
		smtpHost = null;
		port = -1;
		configureSMTPServer(mail, domain);
	}
	
	private void sendMail(Mail mail) throws Exception
	{		
		
		Properties prop = new Properties();
		
		prop.put("mail.transport.protocol", "smtp");
		prop.put("mail.smtp.host", getSmtpHost());
		prop.put("mail.smtp.port", getPort());
		prop.put("mail.from", getUsername());
		
		Session session;
		
		if(true)
		{
			prop.put("mail.smtp.socketFactory.port", "465");
			prop.put("mail.smtp.socketFactory.class" ,"javax.net.ssl.SSLSocketFactory");
		}
		
		if(isAuthEnable())
		{
			prop.put("mail.smtp.starttls.enable","true");		
			prop.put("mail.smtp.auth", "true");		
			
			
			
			Authenticator auth = new javax.mail.Authenticator() 
			{
				protected PasswordAuthentication getPasswordAuthentication() 
				{
					return new PasswordAuthentication(getUsername(), getPassword());
				}
			};

			session = Session.getInstance(prop, auth);
		}
		else
		{
			session = Session.getInstance(prop);
		}
		
		MimeMessage msg = new MimeMessage(session);		
		
		msg.setFrom(new InternetAddress(getUsername()));
		
		Address[] addList = new Address[mail.getTo().size()];
		
		
		for(int i = 0; i < mail.getTo().size(); i++)
			addList[i] = InternetAddress.parse(mail.getTo().get(i), false)[0];
		
		msg.setRecipients(Message.RecipientType.TO, addList);

		msg.setSubject(mail.getSubject(), "UTF-8");
		
		Multipart parts = new MimeMultipart();
		MimeBodyPart body = new MimeBodyPart();
		if(mail.isHTML())
			body.setContent(mail.getContent(), "text/html; charset=UTF-8");
		else
			body.setText(mail.getContent(), "UTF-8");
		parts.addBodyPart(body);
		
		if(mail.getAttachment() != null)
			parts.addBodyPart(makeAttachment(mail.getAttachment()));
	
		msg.setContent(parts);
		msg.setHeader("Centrex-Mailer", "CetrexMail");
		msg.setSentDate(new Date());		
		
		Transport.send(msg, msg.getAllRecipients());		
		
		if(logger.isDebugEnabled())
		{
			StringBuilder sb = new StringBuilder("email sent to ");
			for(String tmp : mail.getTo())
				sb.append(tmp).append(", ");
			sb.append(".");
			logger.debug(sb);
		}		
		
	}
	
	private MimeBodyPart makeAttachment(File file) throws MessagingException
	{
		MimeBodyPart attach = new MimeBodyPart();
		FileDataSource souce = new FileDataSource(file);
		attach.setDataHandler(new DataHandler(souce));
		attach.setDisposition(Part.ATTACHMENT);
		attach.setFileName(souce.getName());
		return attach;
	}
	

	private void configureSMTPServer(Mail mail, String domain) throws Exception
	{			
		if(isSMTPServerReady())
			sendMail(mail);
		else		
			tryConfigure(mail, domain);
	}		
	
	private void tryConfigure(Mail mail, String domain) throws Exception
	{
		String smtpProperty = IPXPropertiesType.SMTP_HOST.getKey();		
		String smtpPortProperty = IPXPropertiesType.SMTP_HOST_PORT.getKey();
		String mailUsernameProperty = IPXPropertiesType.MAIL_USER.getKey();
		String mailPasswordProperty = IPXPropertiesType.MAIL_PASSWORD.getKey();	
		String authEnableProperty = IPXPropertiesType.SMTP_AUTH_ENABLE.getKey();
		String sslEnableProperty = IPXPropertiesType.SSL_SOCKETFACTORY_ENABLE.getKey();
		
		if(domain != null && domain.length() > 0)
		{
			smtpProperty = smtpProperty.concat(".").concat(domain);
			smtpPortProperty = smtpPortProperty.concat(".").concat(domain);
			mailUsernameProperty = mailUsernameProperty.concat(".").concat(domain);
			mailPasswordProperty = mailPasswordProperty.concat(".").concat(domain);
			authEnableProperty = authEnableProperty.concat(".").concat(domain);
			sslEnableProperty = sslEnableProperty.concat(".").concat(domain);
		}
		
		String domainSMTP = IPXProperties.getProperty(smtpProperty, null);
		String domainSMTPPort = IPXProperties.getProperty(smtpPortProperty, null);
		String domainMailUsername = IPXProperties.getProperty(mailUsernameProperty, null);
		String domainMailPassword = IPXProperties.getProperty(mailPasswordProperty, null);			
		String domainMailAuthEnable = IPXProperties.getProperty(authEnableProperty, "true");
		String domainMailSSLEnable = IPXProperties.getProperty(sslEnableProperty, "true");
				
		if(domainSMTP != null && domainSMTPPort != null && domainMailUsername != null && domainMailPassword != null)
		{
			setSmtpHost(domainSMTP);
			setPort(Integer.valueOf(domainSMTPPort));
			setUsername(domainMailUsername);
			setPassword(domainMailPassword);			
			setAuthEnable(Boolean.valueOf(domainMailAuthEnable));
			setSslFactoryEnable(Boolean.valueOf(domainMailSSLEnable));
		}
		else
		{
			if(domain == null)
				throw new Exception("SMTP Configurations Not Found!!");
			else  if(domain.contains("."))
				domain = domain.substring(domain.indexOf(".") + 1, domain.length());
			else if(domain.length() > 0)
				domain = null;
		}
		
		configureSMTPServer(mail, domain);
	}
	
	private boolean isSMTPServerReady()
	{
		if(getSmtpHost() != null && getPort() != -1 && getUsername() != null && getPassword() != null)
			return true;
		else
			return false;
	}
	//fim --> dnakamashi - bug #6273 - version 3.0.5
}
