package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callback;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CallbackInfo;

public interface CallbackDAO extends DAO<Callback>, ReportDAO<Callback, CallbackInfo>
{

	public Callback getCallbackByPbxuserAndTo(Long puKey, String sipAddress) throws DAOException;
	
	public Callback getCallbackByFromAndTo(String from, String to) throws DAOException;
	
	public List<Callback> getCallbackListByPbxuser(String address, String domain) throws DAOException;
	
	public List<Callback> getCallbackListByDomainKey(Long domainKey) throws DAOException;
	
	public Callback getCallbackFullByKey(Long callbackKey) throws DAOException;
	
	public List<Callback> getClickToDialListByDomainKey(Long domainKey) throws DAOException;

	public List<Callback> getCallbackListFromPbxuser(Long pbxuserKey) throws DAOException;
	
}
