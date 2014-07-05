package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Systemcalllog;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SystemCalllogInfo;

public interface SystemcalllogDAO extends DAO<Systemcalllog>, ReportDAO<Systemcalllog, SystemCalllogInfo>
{
	public Systemcalllog getRelatedCalllogBySystemCalllogKey(Long systemCalllogKey) throws DAOException;
	
	public List<Systemcalllog> getListByCallSequenceAndCallID(Integer callSequence, String callID) throws DAOException;
	
	public List<Systemcalllog> getRelatedCallLogListByCallIDs(String ...callID) throws DAOException;

}