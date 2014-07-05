package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Gateway;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.GatewayInfo;

public interface GatewayDAO extends DAO<Gateway>, ReportDAO<Gateway, GatewayInfo>
{
	/**
	 * Loads User and Domain.
	 */
	public Gateway getGatewayByUserKey(Long userKey) throws DAOException;
	
	public Gateway getGatewayFull(Long gatewayKey) throws DAOException;
	
	public List<Duo<Long, String>> getGatewayList() throws DAOException;
	
	public List<Gateway> getRegisterGateways() throws DAOException;
	
	public boolean isGatewayContact(Long userKey, String contact) throws DAOException;
	
	public Gateway getGatewayByName(String gatewayName) throws DAOException;
}
