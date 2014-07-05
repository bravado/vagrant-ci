package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.RecordFile;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RecordFileInfo;

public interface RecordFileDAO extends DAO<RecordFile>
{
	public RecordFile getRecordFileByCallLogKey(Long callLogKey) throws DAOException;
	
	public List<RecordFile> getRecordFileByIsReady(int isReady, Integer listLength) throws DAOException;
	
	public RecordFile getRecordFileByConnID(String connID) throws DAOException;

	public RecordFileInfo getRecordFileInfobySipCallId(String callId) throws DAOException;

	public RecordFile getRecordFileByRecFileName(String recFileName) throws DAOException;
	
	public List<RecordFileInfo> getRecordFileListByDomainAndDateAndDidList(String domain, Calendar dateStart, Calendar dateEnd, List<String> didList) throws DAOException;
}
