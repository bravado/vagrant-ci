package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Eventsubscription;

public interface EventsubscriptionDAO extends DAO<Eventsubscription> 
{

	public List<Eventsubscription> getBySipsessionlog(Long sipsessionlogKey) throws DAOException;
	public long removeBySipsessionlog(String query,Long currentTime) throws DAOException;
	public Eventsubscription getBySipsessionlogAndEvent(Long sipsessionlogKey, String event) throws DAOException;
	public List<Eventsubscription> getEventSubscriptionByDomain(String eventType, String domain) throws DAOException;
	public long getNumberOfSusbcription() throws DAOException;
}
