package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunk;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SipTrunkInfo;

public interface SipTrunkDAO  extends DAO<SipTrunk>, ReportDAO<SipTrunk, SipTrunkInfo>
{
	
	public List<SipTrunk> getSipTrunksByDomain(Long domainKey) throws DAOException;
	
	public SipTrunk getSipTrunksByPbxuserKey(Long pbxuserKey) throws DAOException;
	public List<SipTrunk> getRegisterSipTrunkList() throws DAOException;

	public SipTrunk getSipTrunkByUserKey(Long key) throws DAOException;
}
