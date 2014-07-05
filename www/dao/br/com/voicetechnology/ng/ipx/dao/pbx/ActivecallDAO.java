package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ActivecallInfo;

public interface ActivecallDAO extends DAO<Activecall>, ReportDAO<Activecall, ActivecallInfo>
{
	public List<Activecall> getActiveCallListByPbxuser(Long pbxuserKey) throws DAOException;
	
	public Long howManyActiveCallByPbxuser(Long pbxuserKey) throws DAOException;

	public Activecall getActiveCallByAddressAndState(String address, String domain, int state) throws DAOException;
	
	public Activecall getActiveCallByAddressAndState(String address, String domain, int state, String callId) throws DAOException;

	public Activecall getActiveCallByAddress(String address, String domain, String callId) throws DAOException;
	public Activecall getActiveCallByAddress(String address, String domain) throws DAOException;
	
	public Activecall getActiveCallInGroupByAddressAndState(String address, String domain, int state) throws DAOException;

	public List<Activecall> getActivecallListByPBX(Long pbxKey) throws DAOException;

	public Activecall getActiveCallByPbxuser(Long pbxuserKey, String callId) throws DAOException;
	public Activecall getActiveCallByPbxuser(Long pbxuserKey, int state) throws DAOException;
	
	public Activecall getActiveCallVoicemailPartyByAddressAndState(String address, Long domainKey, int state) throws DAOException;
	
	/**
	 * Retorna a lista de Activecall que possuam o campo 
	 * <code>callID</code> igual ao <code>callID</code> passado.
	 */	
	public List<Activecall> getActivecallListByCallID(String callID) throws DAOException;
	
	public List<Duo<Activecall, Address>> getActivecallListBySipSessionLog(Long sipSessionLogKey) throws DAOException;

	public Activecall getActivecallByCallIDAddressAndDomain(String callID, String address, String domain) throws DAOException;

	public List<Activecall> getActiveCallListByUserKey(Long userKey) throws DAOException;
	
}
