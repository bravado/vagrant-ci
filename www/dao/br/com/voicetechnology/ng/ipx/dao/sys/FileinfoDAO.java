package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.FileInfo;

public interface FileinfoDAO extends DAO<Fileinfo>, ReportDAO<Fileinfo, FileInfo>
{
	public Double countUsedQuota(Long domainKey) throws DAOException;

	public List<Long> getIndexList() throws DAOException;

	public List<Duo<Long, String>> listSimpleFiles(Long domainKey) throws DAOException;

	public Fileinfo getUserVoiceMailSalutationPath(Long userKey) throws DAOException;

	public Fileinfo getGroupVoiceMailSalutationPath(Long gKey, Integer useType) throws DAOException;
	
	public List<Fileinfo> getFilesByUser(Long userKey) throws DAOException;

	public List<Duo<Long, String>> getFilesInDomain(Long domainKey, int fileType) throws DAOException;
	
	public Fileinfo getMusicServerFileByDomain(Long domainKey) throws DAOException;
	
	public String getParkFilePathByDomain(String domain) throws DAOException;
	
	public List<Fileinfo> getDisabledFiles() throws DAOException;
	
	public Fileinfo getFileByKeyAndUser(Long fileinfoKey, Long userKey) throws DAOException;
	
	public List<Fileinfo> getFilesInDomain(Long domainKey) throws DAOException;
	
	public List<Fileinfo> getSimpleFilesInDomain(Long domainKey) throws DAOException;
	
	public List<Duo<Long, String>> getFilesInDomain(Long domainKey, int fileType, Integer lastIndex, Integer maxLength) throws DAOException;
	
	public boolean verifyUniqueIndex(Long index) throws DAOException;
	
	public Fileinfo getFileinfoByNameAndAssociatedUser(String fileName, Long userKey, Long domainKey) throws DAOException;
}