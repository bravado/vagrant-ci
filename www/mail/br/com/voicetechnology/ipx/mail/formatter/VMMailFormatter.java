package br.com.voicetechnology.ipx.mail.formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ipx.mail.pojo.Mail;
import br.com.voicetechnology.ipx.mail.server.MailFormatter;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;

public class VMMailFormatter implements MailFormatter
{
	public static final String IMAGE_BASE_PATH = "pages/resources/images/mail/";
	public static final String BASIX_LOGO_IMAGE = "basixlogo_mail.gif";
	public static final String DATE_IMAGE = "date.gif";
	public static final String TIME_IMAGE = "time.gif";
	public static final String DOWNLOAD_IMAGE = "download.gif";
	
	private String templatePath;
	private Logger logger;
	private Map<String, Object> values = new HashMap<String, Object>();

	public VMMailFormatter() 
	{		
		templatePath = System.getProperty("jboss.server.config.url") + "basix" + File.separator + "locale" + File.separator + "voicemail" + File.separator + "template" + File.separator;
		templatePath = templatePath.substring(5);
		logger = Logger.getLogger(this.getClass().getName());
	}
	
	public void makeMail(Mail mail) throws Exception
	{
		String locale = (String) values.get("locale");
		File file = new File(templatePath + "template_" + locale.toLowerCase() + ".html");
		String content = readFile(file); 
		
		String trimContent = content.trim().replace("\n", "");
		String dateForm = trimContent.replaceAll(".*\\#date:(\\w+)\\/(\\w+)\\/(\\w+)\\#.*", "$1/$2/$3"); 
		String timeForm = trimContent.replaceAll(".*\\#time:(\\w+):(\\w+)\\#.*", "$1:$2");
		String subject = trimContent.replaceAll(".*\\<title\\>(.*?)\\<\\/title\\>.*", "$1");
		
		mail.setSubject(subject);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateForm);
		
		values.put("date:.*", dateFormat.format(new Date()));
		dateFormat.applyPattern(timeForm);
		values.put("time:.*", dateFormat.format(new Date()));
		values.put("basixlogoImage", getImagePath(BASIX_LOGO_IMAGE));
		values.put("dateImage", getImagePath(DATE_IMAGE));
		values.put("timeImage", getImagePath(TIME_IMAGE));
		values.put("downloadImage", getImagePath(DOWNLOAD_IMAGE));
		
		for(Entry<String, Object> tmp  : values.entrySet())
		{
			if(tmp.getValue() != null)
				content = content.replaceAll("#" + tmp.getKey() + "#", tmp.getValue().toString());
		}
		mail.setHTML(true);
		mail.setContent(content);		
	}
	
	private String getImagePath(String image)
	{
		String baseURL = IPXProperties.getProperty(IPXPropertiesType.MAIL_BASIX_URL);
		String urlSeparator = "";
		if(!baseURL.endsWith("/"))
			urlSeparator = "/";
		return baseURL.concat(urlSeparator).concat(IMAGE_BASE_PATH).concat(image);
	}
	
	private String readFile(File f)
	{
		InputStream fileOpenStream = null;
		InputStreamReader inputReader = null;
		StringBuilder sb = new StringBuilder();
		BufferedReader in = null;
		try {
			fileOpenStream = f.toURL().openStream();
			inputReader = new InputStreamReader(fileOpenStream);
			in = new BufferedReader(inputReader);
			
			for(String line = in.readLine(); line != null; line = in.readLine())
				sb.append(line + "\n");
			
		} catch (Exception e){
			logger.error("Error to open template file to send email");
		}
		
		finally
		{
			try {
				if(fileOpenStream != null)
					fileOpenStream.close();
				if(inputReader != null)
					inputReader.close();
				if(in != null)
					in.close();
			} catch (IOException e) {
				logger.error("Error to close tmeplate file to send email");
			}
		}
		
		return sb.toString();
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


