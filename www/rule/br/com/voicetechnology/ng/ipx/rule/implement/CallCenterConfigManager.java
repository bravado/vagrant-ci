package br.com.voicetechnology.ng.ipx.rule.implement;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallCenterConfigDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CallCenterConfig;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class CallCenterConfigManager extends Manager
{
	private CallCenterConfigDAO callCenterConfigDAO;
	
	public CallCenterConfigManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		this.initDAOs();
	}
	
	public CallCenterConfigManager(Logger logger) throws DAOException
	{
		super(logger);
		this.initDAOs();
	}
	
	private void initDAOs() throws DAOException
	{
		callCenterConfigDAO = dao.getDAO(CallCenterConfigDAO.class);
	}
	
	public CallCenterConfig getCallCenterConfigByGroupkey(Long groupkey) throws DAOException
	{
		return callCenterConfigDAO.getCallCenterConfigByGroupkey(groupkey);
	}
	
}
