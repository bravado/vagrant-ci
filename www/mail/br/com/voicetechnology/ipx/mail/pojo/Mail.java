package br.com.voicetechnology.ipx.mail.pojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Mail
{
	private List<String> to;
	
	private String subject;
	
	private String content;
	
	private boolean isHTML;

	private File attachment;

	public Mail(String to, String subject, String content, boolean isHTML, File attachment)
	{
		this(to, subject, content, isHTML);
		this.attachment = attachment;
	}
	
	public Mail(String to, String subject, String content, boolean isHTML)
	{
		this(subject, content, isHTML);
		addTo(to);
	}

	public Mail(String subject, String content, boolean isHTML)
	{
		this.subject = subject;
		this.content = content;
		this.isHTML = isHTML;
		this.to = new ArrayList<String>();
	}
	
	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public boolean isHTML()
	{
		return isHTML;
	}

	public void setHTML(boolean isHTML)
	{
		this.isHTML = isHTML;
	}

	public List<String> getTo()
	{
		return to;
	}

	public void addTo(String to)
	{
		this.to.add(to);
	}
	
	public File getAttachment()
	{
		return attachment;
	}
}
