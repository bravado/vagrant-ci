package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sessionlog;

public interface SessionlogDAO extends DAO<Sessionlog>
{

	public List<Sessionlog> getSessionlogListByPbxuser(Long pbxuserKey, boolean sessionEnd, boolean decreasing) throws DAOException;

	/**
	 * Seleciona as sessões web (Sessionlog) abertas 
	 */
	public List<Sessionlog> getOpenedSessionlogList() throws DAOException;
	
	public List<Sessionlog> getOpenedSessionlogListByUser(Long userKey) throws DAOException;
	

	public List<Sessionlog> getSessionlogListByGatewayUser(Long userKey) throws DAOException;
	
	public List<Sessionlog> getSessionlogListByUserCentrex(Long userKey) throws DAOException;
	
	
	public Long getNumberOfClosedSessionlog(int type) throws DAOException;
	
	public long updateClosedSessionlog(String query, Long currentTime) throws DAOException;
	
	public long updateClosedUserSessionlog(long timeout) throws DAOException;
	
	//jluchetta - BUG 6727 - Versão 3.0.5 RC6.6 - Alteracao para trabalhar com PBX somente em uma porta
	public void updateMediaSessionlog(String query) throws DAOException;
	
}
