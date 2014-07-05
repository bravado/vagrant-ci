package br.com.voicetechnology.ng.ipx.rule.interfaces;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.DAOFactory;

public abstract class Manager
{
	protected static DAOFactory dao;
	protected Logger logger;

	public Manager(String loggerPath) throws DAOException
	{
		if(dao == null)
			dao = DAOFactory.createFactory(IPXProperties.getProperty(IPXPropertiesType.DAO_FACTORY), IPXProperties.getProperty(IPXPropertiesType.DAO_JNDI_URL));
		logger = Logger.getLogger(this.getClass());
		logger.info("Creating instance of " + this.getClass().getName());
	}

	public Manager(Logger logger)
	{
		this.logger = logger;
	}
	
	protected String getSipIDRegex()
	{
		return "\\w[\\w\\.\\-\\_\\d]*";
	}
	
	protected String getCommandRegex()
	{
		return "\\*\\d+";
	}
	
	protected String getPhoneNumberRegex()
	{
		return "\\d+";
	}
	
	protected String getPSTNNumber(String number, boolean isGetPrefix)
	{
		String length = IPXProperties.getProperty(IPXPropertiesType.CENTREX_ADDRESS_LENGTH);
		if(length == null)
			return number;
		try
		{
			int sizeNumber = Integer.valueOf(length);
			if(number.length() - sizeNumber >= 0)
				if(isGetPrefix)
					return number.substring(0, number.length() - sizeNumber);
				else
					return number.substring(number.length() - sizeNumber, number.length());
			else if(isGetPrefix)
				return null;
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return number;
	}
}