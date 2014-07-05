package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.RecordFileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.RecordFile;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.RecordFileInfo;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class RecordFileManager extends Manager
{
	private RecordFileDAO recordFileDAO;
	private CalllogDAO calllogDAO;
	private DomainDAO domainDAO;
	
	public RecordFileManager(Logger logger) throws DAONotFoundException 
	{
		super(logger);
		recordFileDAO = dao.getDAO(RecordFileDAO.class);
		calllogDAO = dao.getDAO(CalllogDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
	}
	
	public void save(List<RecordFile> list) throws DAOException, ValidateObjectException
	{
		for(RecordFile file : list)
			save(file);
	}
	
	public void save(RecordFile recordFile) throws DAOException, ValidateObjectException
	{
		recordFileDAO.save(recordFile);
	}
	
	public List<RecordFile> getRecordFileByIsReady(int isReady, Integer listLength) throws DAOException
	{
		return recordFileDAO.getRecordFileByIsReady(isReady, listLength);
	}

	public RecordFileInfo updateRecordFile(String connectionID, String path) throws DAOException, ValidateObjectException 
	{			
		RecordFileInfo info = getRecordFileInfo(connectionID);
		
		if(info == null)
			return null;
		
		RecordFile file = info.getRecordFile();
		file.setIsReady(RecordFile.READY);
		file.setRecFileName(file.getLabel());
		if(path != null)
			file.setPath(path);
			
		recordFileDAO.save(file);
		
		return info;		
	}
	
	public RecordFileInfo getRecordFileInfo(String connectionID) throws DAOException, ValidateObjectException
	{
		RecordFile file = recordFileDAO.getRecordFileByConnID(connectionID);
		if(file == null)
			return null;			
		
		Calllog calllog = calllogDAO.getByKey(file.getCallLogKey());
		
		if(calllog == null)
			return null;
		
		Domain domain = domainDAO.getDomainByPbxUserKey(calllog.getPbxuserKey());
		
		if(domain == null)
			return null;
		
		return new RecordFileInfo(file, calllog, domain.getKey());		
	}
	
	public RecordFileInfo getRecordFileInfoByRecFileName(String RecFileName) throws DAOException, ValidateObjectException
	{
		RecordFile file = recordFileDAO.getRecordFileByRecFileName(RecFileName);
		if(file == null)
			return null;			
		
		Calllog calllog = calllogDAO.getByKey(file.getCallLogKey());
		
		if(calllog == null)
			return null;
		
		Domain domain = domainDAO.getDomainByPbxUserKey(calllog.getPbxuserKey());
		
		if(domain == null)
			return null;
		
		return new RecordFileInfo(file, calllog, domain.getKey());		
	}
	
	public Long getDomainKeyByConnID(String connectionID) throws DAOException, ValidateObjectException
	{
		RecordFileInfo info =getRecordFileInfo(connectionID);
		if(info == null)
			return null;
		
		return info.getDomainKey();
	}
	
	public List<RecordFileInfo> getRecordFileListByDomainAndDateAndDidList(String domain, Calendar dateStart, Calendar dateEnd, List<String> didList) throws DAOException, ValidateObjectException
	{
		return recordFileDAO.getRecordFileListByDomainAndDateAndDidList(domain, dateStart, dateEnd, didList);
	}

}
