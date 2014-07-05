package br.com.voicetechnology.ng.ipx.rule.implement.voicemail;

import java.util.Calendar;

public class DynaPrompts
{
	private Variable property;
	private Calendar calendar;
	private String value;
	private Type type;
	
	public enum Type
	{
		DATE, SPELLED, NUMBER;
	}
	
	public enum Variable
	{
		MSG_NUM, ANI, TO;
	}
	
	public DynaPrompts(String value, Type type)
	{
		this.value = value;
		this.type = type;
	}
	
	public DynaPrompts(Calendar calendar, Type type)
	{
		this.calendar = calendar;
		this.type = type;
	}
	
	public DynaPrompts(Variable property, String value, Type type)
	{
		this.property = property;
		this.value = value;
		this.type = type;
	}
	
	public Variable getProperty()
	{
		return property;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public Calendar getValueCalendar()
	{
		return calendar;
	}
	
	public Type getType()
	{
		return type;
	}
}
