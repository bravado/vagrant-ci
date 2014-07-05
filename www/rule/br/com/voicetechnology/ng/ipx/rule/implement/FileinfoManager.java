package br.com.voicetechnology.ng.ipx.rule.implement;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAONotFoundException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateError;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.QuotaException;
import br.com.voicetechnology.ng.ipx.commons.file.FileUtils;
import br.com.voicetechnology.ng.ipx.commons.file.TempFileManager;
import br.com.voicetechnology.ng.ipx.commons.jms.tools.JMSNotificationTools;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVRDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.FileinfoDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.GroupfileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserfileDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVR;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Groupfile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userfile;
import br.com.voicetechnology.ng.ipx.pojo.db.voicemail.VoiceMailInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.FileInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class FileinfoManager extends Manager
{
	public static final int MAX_SAVE_ATTEMPTS = 3;
	
	private FileinfoDAO fileinfoDAO;
	private PbxDAO pbxDAO;
	private DomainDAO domainDAO;
	private UserfileDAO userfileDAO;
	private GroupfileDAO groupfileDAO;
	private IVRDAO ivrDAO;
	private CalllogDAO clogDAO;
	private GroupDAO groupDAO;
	private ReportDAO<Fileinfo, FileInfo> report;
	
	private static String rootPath = null;
	private static Long rangeStart = null;
	private static Long rangeEnd = null;
	private static SimpleDateFormat dateFormat = null;
	private static final Long indexSemaphore = 1L;

	public FileinfoManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		initDAO();
	}
	
	public FileinfoManager(Logger logger) throws DAOException
	{
		super(logger);
		initDAO();
	}
	
	private void initDAO() throws DAONotFoundException
	{
		fileinfoDAO = dao.getDAO(FileinfoDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		userfileDAO = dao.getDAO(UserfileDAO.class);
		groupfileDAO = dao.getDAO(GroupfileDAO.class);
		ivrDAO = dao.getDAO(IVRDAO.class);
		clogDAO = dao.getDAO(CalllogDAO.class);
		groupDAO = dao.getDAO(GroupDAO.class);
		report = dao.getReportDAO(FileinfoDAO.class);
	}

	
	public static String getPhysRootPath()
	{
		if(rootPath == null) 
			rootPath = FileUtils.getAbsoluteBasePath();
		return rootPath;
	}
	
	public static Long getIndexRangeStart()
	{
		if (FileinfoManager.rangeStart == null)
		{
			String start = IPXProperties.getProperty(IPXPropertiesType.FILES_ID_RANGE_START);
			if(start != null)
				FileinfoManager.rangeStart = Long.parseLong(start);
		}
		return FileinfoManager.rangeStart;
	}
	
	public static Long getIndexRangeEnd()
	{
		if (FileinfoManager.rangeEnd == null)
		{
			String end = IPXProperties.getProperty(IPXPropertiesType.FILES_ID_RANGE_END);
			if(end != null)
				FileinfoManager.rangeEnd = Long.parseLong(end);
		}
		return FileinfoManager.rangeEnd;
	}
	
	protected static String getFormattedCurrentTime()
	{
		if(dateFormat == null) 
			dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(Calendar.getInstance().getTime());
	}
	
	public ReportResult<FileInfo> find(Report<FileInfo> info) throws DAOException
	{
		Long size = report.getReportCount(info);
		List<Fileinfo> fileList = report.getReportList(info);
		List<FileInfo> fileInfoList = new ArrayList<FileInfo>(fileList.size());
		for (Fileinfo file : fileList)
			fileInfoList.add(new FileInfo(file, null, null));
		return new ReportResult<FileInfo>(fileInfoList, size);
	}

	public FileInfo getFileInfoByKey(Long fileInfoKey) throws DAOException
	{
		logger.debug("Loading fileInfo with fileinfokey " + fileInfoKey);
		Fileinfo fileinfo = fileinfoDAO.getByKey(fileInfoKey);
		if(fileinfo == null)
			throw new NullPointerException("There is no fileinfo with key " + fileInfoKey);
		logger.debug("loaded fileinfo " + fileinfo);
		Float usedQuota = fileinfoDAO.countUsedQuota(fileinfo.getDomainKey()).floatValue();
		logger.debug("used quota for this domain: " + usedQuota);
		Pbx pbx = pbxDAO.getPbxByDomain(fileinfo.getDomainKey());
		Float maxQuota = pbx.getQuota();
		logger.debug("max quota for this domain: " + maxQuota);
		FileInfo fileInfo = new FileInfo(fileinfo, usedQuota, maxQuota);
		logger.debug("Sucessfully loaded fileInfo");
		return fileInfo;
	}
	
	public void getInfoContext(FileInfo info, Long domainKey, boolean validateQuota) throws DAOException, QuotaException
	{
		if(validateQuota)
			validateQuota(domainKey, info.getFileinfo());
		info.setMaxQuota(pbxDAO.getPbxByDomain(domainKey).getQuota());
		info.setUsedQuota(fileinfoDAO.countUsedQuota(domainKey).floatValue());
	}
	
	public void save(FileInfo fileInfo) throws DAOException, ValidateObjectException, QuotaException, IOException
	{
		Fileinfo fileinfo = fileInfo.getFileinfo();
		boolean update = fileinfo.getKey() != null;
		logger.info("Saving fileinfo " + fileinfo + ", update operation: " + update);
		if(fileinfo.getType() == Fileinfo.TYPE_VM_MESSAGE)
			throw new IllegalArgumentException("Cannot save voicemail messages using FileinfoManager.save(), use methods FileinfoManager.prepareVoiceMailMessage and FileinfoManager.saveVoiceMailMessage instead");
		
		Domain domain = domainDAO.getByKey(fileinfo.getDomainKey());
		File oldFile = null;
		File tmpFile = getPhysFile(fileinfo);
		logger.info("temporary file location: " + tmpFile.getPath());
		
		if(fileinfo.getIndex() == null)
		{
			logger.info("index not found on object, generating index");
			fileinfo.setIndex(this.getNextAvailableIndex(null));
		}
		logger.debug("using index " + fileinfo.getIndex());
		String absoluteName = this.buildPhysName(fileinfo.getName(), fileinfo.getType(), fileinfo.getIndex(), domain.getDomain());
		logger.debug("absolute name generated for the file: " + absoluteName);
		
		if(fileInfo.getCreateTime() == null)
			fileInfo.setCreateTime(Calendar.getInstance());
		
		if(update)
		{
			logger.debug("loading old file from datasource");
			Fileinfo fileFromDS = fileinfoDAO.getByKey(fileinfo.getKey());
			logger.debug("loaded file " + fileFromDS);
			oldFile = getPhysFile(fileFromDS);
			logger.debug("old file physical location: " + oldFile.getPath());
			fileFromDS.setName(fileinfo.getName());
			fileinfo = fileFromDS;
		}
		fileinfo.setAbsoluteName(absoluteName);
		fileinfo.setSize(convertToMegaBytes(tmpFile.length()));

		logger.debug("size of the new file " + fileinfo.getSize() + "mb");
		logger.debug("validating fileinfo ");
		validateSave(fileinfo);
		File physFile = this.getPhysFile(fileinfo);
		try
		{
			fileinfo.setActive(Fileinfo.DEFINE_ACTIVE);
			fileinfoDAO.save(fileinfo);
			logger.debug("Fileinfo sucessfully saved!");
			if(update)
			{
				logger.debug("removing old file");
				FileUtils.deleteFile(oldFile);
			}
			logger.debug("moving " + tmpFile.getPath() + " to " + physFile.getPath());
			FileUtils.moveAndRenameFile(tmpFile, physFile);
			logger.debug("file sucessfully moved");
		} catch (DAOException e)
		{
			logger.error("Couldnt save file! Removing physical file");
			FileUtils.deleteFile(physFile);
			throw e;
		}
		//Notificação para o Call Center de lista de files atualizada - rribeiro
		JMSNotificationTools.getInstance().sendFileListModifiedMessage(domain.getKey());
	}
	
	public void deleteFiles(List<Long> fileinfoKeyList) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		for (Long fileKey : fileinfoKeyList)
			this.deleteFileinfo(fileKey);
	}
	
	public void deleteFileinfo(Long fileKey) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		logger.info("Loading fileinfo with key " + fileKey + " for deletion"); 
		Fileinfo fileinfo = fileinfoDAO.getByKey(fileKey);
		logger.info("loaded fileinfo " + fileinfo);
		logger.info("removing relationals for " + fileinfo);
		this.removeRelationals(fileinfo);
		fileinfo.setActive(Fileinfo.DEFINE_DELETED);
		fileinfoDAO.save(fileinfo);
	}

    public void permanentlyRemoveFileinfo(Long fileKey) throws DAOException, ValidateObjectException
    {
        logger.debug("Removing file info permanently"); 
        Fileinfo fileinfo = fileinfoDAO.getByKey(fileKey);
        fileinfoDAO.remove(fileinfo);
        logger.debug("File info removed from database");
    }
    
	private void removeRelationals(Fileinfo file) throws DAOException, DeleteDependenceException, ValidateObjectException
	{
		//Conta ivrs usando este file
		Long ivrAmount = ivrDAO.countIVRsUsingFile(file.getDomainKey(), file.getKey());
	    Long groupAmount = groupDAO.countGroupsUsingFile(file.getDomainKey(), file.getKey());
		if(ivrAmount > 0 && groupAmount > 0)
			throw new DeleteDependenceException("File is used by " + ivrAmount + " IVRs and "+groupAmount+" Groups", null, ivrAmount+groupAmount, file);
		else if(ivrAmount > 0)
			throw new DeleteDependenceException("File is used by " + ivrAmount + " IVRs", IVR.class, ivrAmount, file);
		else if(groupAmount > 0)
			throw new DeleteDependenceException("File is used by " + groupAmount + " Groups", Group.class, groupAmount, file);
		
		//resgata o file do musicServer e verifica se n�o � o arquivo que est� sendo removido 
		Fileinfo musicServerFile = fileinfoDAO.getMusicServerFileByDomain(file.getDomainKey());
		if(musicServerFile != null && musicServerFile.getKey().equals(file.getKey()))
			throw new DeleteDependenceException("Cannot delete " + file.getName() + ": The file is musicServer file at pbx!", Pbx.class, 1L, file);
		
		List<Userfile> userFileList = userfileDAO.getUserFileListByFile(file.getKey());
		for(Userfile userfile : userFileList)
			userfileDAO.remove(userfile);
		
		List<Groupfile> groupFileList = groupfileDAO.getGroupFileListByFile(file.getKey());
		for(Groupfile groupFile : groupFileList)
		{
			groupfileDAO.remove(groupFile);
		}
	}

	protected void validateSave(Fileinfo fileinfo) throws DAOException, ValidateObjectException, QuotaException
	{
		List<ValidateError> errorList = new ArrayList<ValidateError>();
		if(fileinfo == null)
		{
			errorList.add(new ValidateError("Fileinfo is null!", Fileinfo.class, null, ValidateType.BLANK));
		} else
		{
			Long domainKey = fileinfo.getDomainKey();
			if(domainKey == null)
				errorList.add(new ValidateError("Fileinfo domainKey is null!", Fileinfo.Fields.DOMAIN_KEY.toString(), Fileinfo.class, fileinfo, ValidateType.BLANK));
			
			String name = fileinfo.getName();
			if(name == null)
				errorList.add(new ValidateError("Fileinfo name is null!", Fileinfo.Fields.NAME.toString(), Fileinfo.class, fileinfo, ValidateType.BLANK));
			else if(name.length() == 0)
				errorList.add(new ValidateError("Fileinfo name is blank!", Fileinfo.Fields.NAME.toString(), Fileinfo.class, fileinfo, ValidateType.BLANK));
			
			String absoluteName = fileinfo.getAbsoluteName();
			if(absoluteName == null)
				errorList.add(new ValidateError("Fileinfo absoluteName is null!", Fileinfo.Fields.ABSOLUTENAME.toString(), Fileinfo.class, fileinfo, ValidateType.BLANK));
			else if(absoluteName.length() == 0)
				errorList.add(new ValidateError("Fileinfo absoluteName is blank!", Fileinfo.Fields.ABSOLUTENAME.toString(), Fileinfo.class, fileinfo, ValidateType.BLANK));
			
			Long index = fileinfo.getIndex();
			if(index == null)
				errorList.add(new ValidateError("Fileinfo index is null!", Fileinfo.Fields.INDEX.toString(), Fileinfo.class, fileinfo, ValidateType.BLANK));
			
			Integer type = fileinfo.getType();
			if(type == null)
				errorList.add(new ValidateError("Fileinfo type is null!", Fileinfo.Fields.TYPE.toString(), Fileinfo.class, fileinfo, ValidateType.BLANK));
			else if(type != Fileinfo.TYPE_VM && type != Fileinfo.TYPE_VM_MESSAGE && type != Fileinfo.TYPE_SIMPLEFILE)
				errorList.add(new ValidateError("Fileinfo type is invalid (" + type + ")!", Fileinfo.Fields.TYPE.toString(), Fileinfo.class, fileinfo, ValidateType.INVALID));
			
			Float size = fileinfo.getSize();
			if(size == null)
				errorList.add(new ValidateError("Fileinfo size is null!", Fileinfo.Fields.SIZE.toString(), Fileinfo.class, fileinfo, ValidateType.BLANK));
			else if(size < 0)
				errorList.add(new ValidateError("Fileinfo size is negative(" + size + ")!", Fileinfo.Fields.SIZE.toString(), Fileinfo.class, fileinfo, ValidateType.NUMBER));
		}
		
		if(errorList.size() > 0)
			throw new ValidateObjectException("DAO validate errors! Please check data.", errorList);
		else
			/* Caso nao possua erros de validacao entao valide se existe    *
			 * espaco para salvar nova mensagem                             */
			this.validateQuota(fileinfo.getDomainKey(), fileinfo);
	}
	
	protected void validateQuota(Long domainKey, Fileinfo fileinfo) throws DAOException, QuotaException
	{
		Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		Float maxQuota = pbx.getQuota();
		Float usedQuota = fileinfoDAO.countUsedQuota(domainKey).floatValue() + (fileinfo.getSize() != null ? fileinfo.getSize() : 0);
		if(usedQuota > maxQuota)
			throw new QuotaException("File Quota exceeded", maxQuota, usedQuota, QuotaException.Type.FILE);
	}
	
	protected Long getNextAvailableIndex(Long previousIndex) throws DAOException, QuotaException
	{
		
		return Calendar.getInstance().getTimeInMillis();
		
//		synchronized(indexSemaphore)
//		{
//			List<Long> indexList = fileinfoDAO.getIndexList();
//			Long start = getIndexRangeStart();
//			Long end = getIndexRangeEnd();
//			Long index = null;
//			boolean foundIndex = false;
//			if (indexList.size() < end - start)
//			{
//				Iterator<Long> it = indexList.iterator(); 
//				for(index = start; index <= end && !foundIndex; index++)
//				{
//					if(it.hasNext())
//					{
//						Long nextIndex = it.next();
//						boolean likePrevious = nextIndex != null && nextIndex.equals(previousIndex);
//						if(previousIndex != null && it.hasNext() && likePrevious)
//						{
//							nextIndex = it.next();			
//							index++;
//						}
//						foundIndex = nextIndex == null || nextIndex > index;
//													
//					} else 
//						foundIndex = true;
//				} 
//			}
//			
//			if(foundIndex)
//				return index - 1;
//			else
//				throw new QuotaException("No more Excel indexes available!", QuotaException.Type.EXCEL_INDEX);
//		}
	}
	
	
	
	private String buildPhysName(String name, Integer type, Long index, String domainName)
	{
		String path = this.buildPhysPath(type, domainName);
		return path + File.separator + index + "." + cleanFileName(name) + "." + Fileinfo.Constants.AUDIO_EXTENSION;
	}
	
	private String cleanFileName(String name)
	{
		return name.replaceAll("\\.", "_");
	}
	
	private String buildPhysPath(Integer type, String domainName)
	{
		String basePath = FileUtils.getRelativeBasePath() + convertDomainName(domainName);
		String path = basePath + File.separator;
		switch(type)
		{
			case Fileinfo.TYPE_VM: 
				path += Fileinfo.Constants.VM_DIR.toString();	break;
			case Fileinfo.TYPE_SIMPLEFILE: 
				path += Fileinfo.Constants.FILE_DIR.toString();	break;
			case Fileinfo.TYPE_VM_MESSAGE: 
				path += Fileinfo.Constants.VM_MESSAGE_DIR.toString();	break;
			default:
				path = null;
		}
		
		return path;
	}
	
	public File getPhysFile(Fileinfo fileinfo)
	{
		return new File(FileUtils.getRootPath() + fileinfo.getAbsoluteName());
	}
	
	protected String convertDomainName(String domain)
	{
		return domain.replaceAll("\\.", "_");
	}
	
	protected Float convertToMegaBytes(long bytes)
	{
		double result = (double)bytes / 1024.0 / 1024.0;
		return (float)result;
	}
	
	//voicemail
	public Fileinfo createVoiceMailMessage(VoiceMailInfo vmInfo) throws DAOException, QuotaException, ValidateObjectException
	{
		Fileinfo fileinfo = createBaseMediaFileinfo(vmInfo);
		Domain dm = domainDAO.getByKey(vmInfo.getDomainKey());
		fileinfo.setAbsoluteName(buildPhysPath(Fileinfo.TYPE_VM_MESSAGE, dm.getDomain()) + File.separator + fileinfo.getName());
		fileinfo.setType(Fileinfo.TYPE_VM_MESSAGE);
		fileinfo.setActive(Fileinfo.DEFINE_ACTIVE);
		
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Validating Voicemail file name: ").append(fileinfo.getAbsoluteName()));
		validateVoiceMailFileInfo(fileinfo, vmInfo, dm);
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Voicemail file name validated: ").append(fileinfo.getAbsoluteName()));
		
		return fileinfo;
	}
	
	private boolean trySaveFileInfo(Fileinfo fileinfo)
	{
		try {
			fileinfoDAO.save(fileinfo);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void validateVoiceMailFileInfo(Fileinfo fileinfo, VoiceMailInfo vmInfo, Domain dm) throws DAOException, QuotaException, ValidateObjectException
	{
		boolean saveSucess = false;
		int attempts = 0;
	
		while(!saveSucess && attempts<MAX_SAVE_ATTEMPTS)
		{
			saveSucess = trySaveFileInfo(fileinfo);
			if(!saveSucess)
			{
				if(dm!=null)
					changeFileInfoName(fileinfo, vmInfo.getName(), dm);
				else
					changeFileInfoSalutationName(fileinfo, vmInfo.getName());
			}
			attempts++;
		}
	
		if(!saveSucess)
			throw new QuotaException("Can't save Message because don't have a available index to file");
	}
	
	private void validateFileInfo(Fileinfo fileinfo, String name, Domain dm) throws DAOException, QuotaException, ValidateObjectException
	{
		boolean saveSucess = false;
		int attempts = 0;
	
		while(!saveSucess && attempts<MAX_SAVE_ATTEMPTS)
		{
			saveSucess = trySaveFileInfo(fileinfo);
			if(!saveSucess)
			{
				if(dm!=null)
					changeFileInfoName(fileinfo, name, dm);
				else
					changeFileInfoSalutationName(fileinfo, name);
			}
			attempts++;
		}
	
		if(!saveSucess)
			throw new QuotaException("Can't save Message because don't have a available index to file");
	}
	
	public Fileinfo createVoicemailSalutation(VoiceMailInfo vmInfo) throws DAOException, QuotaException, ValidateObjectException
	{
		Fileinfo fileinfo = createBaseMediaFileinfo(vmInfo);
		/*jluchetta -  Versão 3.0.5 - Correção apara garvar arquivo de saudação de record box
		*Agora os arquivso já são previamente criados na apsta do domínio correto e depois ele somente é atualizado com as informacoes e tamanho corretos
		* Isso evita a copia da pasta temporária para a definitiva
		*/
		Domain dm = domainDAO.getByKey(vmInfo.getDomainKey());
		fileinfo.setAbsoluteName(buildPhysName(vmInfo.getName(), Fileinfo.TYPE_SIMPLEFILE, fileinfo.getIndex(), dm.getDomain()));
		fileinfo.setType(Fileinfo.TYPE_SIMPLEFILE);
		/*******************************************************************/
		fileinfo.setActive(Fileinfo.DEFINE_ACTIVE);		
		validateVoiceMailFileInfo(fileinfo, vmInfo, null);
		return fileinfo;
	}
	
	public Fileinfo createNewFile(Long domainKey, String name) throws DAOException, QuotaException, ValidateObjectException
	{
		Fileinfo fileinfo = createBaseMediaFileinfo(domainKey, name);
		/*jluchetta -  Versão 3.0.5 - Correção apara garvar arquivo de saudação de record box
		*Agora os arquivso já são previamente criados na apsta do domínio correto e depois ele somente é atualizado com as informacoes e tamanho corretos
		* Isso evita a copia da pasta temporária para a definitiva
		*/
		Domain dm = domainDAO.getByKey(domainKey);
		fileinfo.setAbsoluteName(buildPhysPath(Fileinfo.TYPE_SIMPLEFILE, dm.getDomain()) + File.separator + fileinfo.getName());
		fileinfo.setType(Fileinfo.TYPE_SIMPLEFILE);
		/*********************************************************/
		fileinfo.setActive(Fileinfo.DEFINE_ACTIVE);		
		validateFileInfo(fileinfo, name, null);
		return fileinfo;
	}
	
	private Fileinfo createBaseMediaFileinfo(Long domainKey, String name) throws DAOException, QuotaException, ValidateObjectException
	{
		Fileinfo file = new Fileinfo();
		file.setCreateTime(Calendar.getInstance());
		file.setDomainKey(domainKey);
		file.setIndex(getNextAvailableIndex(null));
		String fileName = file.getIndex() + "." + name + "." + Fileinfo.Constants.AUDIO_EXTENSION;
		file.setName(fileName);
		file.setSize(0f);
		return file;
	}
	
	private void changeFileInfoSalutationName(Fileinfo fileInfo, String name) throws QuotaException, DAOException
	{
		changeFileInfoIndex(fileInfo, name);
		fileInfo.setAbsoluteName(FileUtils.getRelativeTempPath() + fileInfo.getName());
	}
	
	private void changeFileInfoIndex(Fileinfo fileInfo, VoiceMailInfo vmInfo) throws DAOException, QuotaException
	{
		Long nextIndex = getNextAvailableIndex(fileInfo.getIndex());
		fileInfo.setIndex(nextIndex);
		String fileName = fileInfo.getIndex() + "." + vmInfo.getName() + "." + Fileinfo.Constants.AUDIO_EXTENSION;
		fileInfo.setName(fileName);
	}
	
	private void changeFileInfoIndex(Fileinfo fileInfo, String name) throws DAOException, QuotaException
	{
		Long nextIndex = getNextAvailableIndex(fileInfo.getIndex());
		fileInfo.setIndex(nextIndex);
		String fileName = fileInfo.getIndex() + "." + name + "." + Fileinfo.Constants.AUDIO_EXTENSION;
		fileInfo.setName(fileName);
	}
	
	private void changeFileInfoName(Fileinfo fileInfo, String name, Domain dm) throws QuotaException, DAOException
	{
		changeFileInfoIndex(fileInfo, name);
		fileInfo.setAbsoluteName(buildPhysPath(Fileinfo.TYPE_VM_MESSAGE, dm.getDomain()) + File.separator + fileInfo.getName());
	}
	
	private Fileinfo createBaseMediaFileinfo(VoiceMailInfo vmInfo) throws DAOException, QuotaException, ValidateObjectException
	{
		Fileinfo file = new Fileinfo();
		file.setCreateTime(Calendar.getInstance());
		file.setDomainKey(vmInfo.getDomainKey());
		
		logger.debug("Creating voicemail file name...");
		
		file.setIndex(getNextAvailableIndex(null));
		String fileName = file.getIndex() + "." + vmInfo.getName() + "." + Fileinfo.Constants.AUDIO_EXTENSION;
		
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Voicemail file name created: ").append(fileName));
		
		file.setName(fileName);
		file.setSize(0f);
		return file;
	}
	
	public boolean saveVoiceMailMessage(Fileinfo fileinfo) throws DAOException, IOException, ValidateObjectException
	{
		File physFile = getPhysFile(fileinfo);
		logger.info("Saving voicemail message " + fileinfo.getName() + " on " + physFile.getPath());
		if(!physFile.exists() || physFile.length() == 0)
		{
			/*jluchetta -  Versão 3.0.5 - Correção apara garvar arquivo de saudação de record box
			*Agora os arquivso já são previamente criados na apsta do domínio correto e depois ele somente é atualizado com as informacoes e tamanho corretos
			* Isso evita a copia da pasta temporária para a definitiva
			*/
			if(physFile.exists())
				FileUtils.deleteFile(physFile);
			cancelRecordFile(fileinfo);
			/********************************************************/
			logger.warn("Message was not saved because physical file does not exist!");
			return false;
			//throw new FileNotFoundException("Couldnt save voicemail message: physical message doesnt exists");
		} else
		{
			Fileinfo fileDB = fileinfoDAO.getByKey(fileinfo.getKey());
			if(fileDB == null)
			{
				logger.error("message not present on Datasource: call createVoiceMailMessage before creating the physical message and use the returned instance");
				logger.info("deleting file " + physFile.getPath());
				FileUtils.deleteFile(physFile);
				throw new NullPointerException("Cannot save message: message not present on datasource!");
			}
			fileDB.setSize(convertToMegaBytes(physFile.length()));
			fileDB.setCreateTime(fileinfo.getCreateTime());
			fileDB.setType(Fileinfo.TYPE_VM_MESSAGE);
			fileinfo.setActive(Fileinfo.DEFINE_ACTIVE);
			fileinfoDAO.save(fileDB);
		}
		logger.info("Message sucessfully saved");
		return true;
	}
	
	/*jluchetta -  Versão 3.0.5 - Correção apara garvar arquivo de saudação de record box
		*Agora os arquivso já são previamente criados na apsta do domínio correto e depois ele somente é atualizado com as informacoes e tamanho corretos
		* Isso evita a copia da pasta temporária para a definitiva
		*/
	public boolean saveRecordFile(Fileinfo fileinfo) throws DAOException, IOException, ValidateObjectException, QuotaException
	{
		
		File physFile = getPhysFile(fileinfo);
		try
		{			
			logger.info("Saving record box file " + fileinfo.getName() + " on " + physFile.getPath());
			if(!physFile.exists() || physFile.length() == 0)
			{
				if(physFile.exists())
					FileUtils.deleteFile(physFile);
				cancelRecordFile(fileinfo);
				logger.warn("Record file Salutation was not saved because physical file does not exist!");
				return false;
			} else
			{
				Fileinfo fileDB = fileinfoDAO.getByKey(fileinfo.getKey());
				if(fileDB == null)
				{
					logger.error("file not present on Datasource: call createNewFile before creating the physical message and use the returned instance");
					logger.info("deleting file " + physFile.getPath());
					FileUtils.deleteFile(physFile);
					throw new NullPointerException("Cannot save file: file not present on datasource!");
				}
				fileDB.setSize(convertToMegaBytes(physFile.length()));
				if(fileinfo.getCreateTime() == null)
					fileinfo.setCreateTime(Calendar.getInstance());
	
				validateSave(fileinfo);
				fileDB.setType(Fileinfo.TYPE_SIMPLEFILE);
				fileinfo.setActive(Fileinfo.DEFINE_ACTIVE);
				fileinfoDAO.save(fileDB);
			}
			logger.info("Record Box file sucessfully saved");
			return true;
		} catch(DAOException e)
		{
			logger.error("Couldnt save file! Removing physical file");
			/*jluchetta -  Versão 3.0.5 - Correção apara garvar arquivo de saudação de record box
			*Agora os arquivso já são previamente criados na apsta do domínio correto e depois ele somente é atualizado com as informacoes e tamanho corretos
			* Isso evita a copia da pasta temporária para a definitiva
			*/
			if(physFile.exists())
				FileUtils.deleteFile(physFile);
			cancelRecordFile(fileinfo);
			/*********************************************/
			throw e;
		}		
	}
	/****************************************************/
	
	private void cancelRecordFile(Fileinfo fileinfoOld) throws DAOException, ValidateObjectException
	{
		Fileinfo fileinfo = fileinfoDAO.getByKey(fileinfoOld.getKey());
		if(fileinfo != null)
			fileinfoDAO.remove(fileinfo);
	}
	
	public List<Fileinfo> getDisabledFiles() throws DAOException, ValidateObjectException
	{
		return fileinfoDAO.getDisabledFiles();
	}

	public String getDownloadableFile(Long fileInfoKey) throws Exception
	{
		Fileinfo fileInfo = fileinfoDAO.getByKey(fileInfoKey);
		if(fileInfo == null)
			throw new Exception("File (key:" + fileInfoKey + ") doesnt exists" );
		return this.copyToTmp(fileInfo);
	}

	public String getDownloadableFile(Long fileInfoKey, Long userKey) throws Exception
	{
		Fileinfo fileInfo = fileinfoDAO.getFileByKeyAndUser(fileInfoKey, userKey);
		if(fileInfo == null)
			throw new Exception("File (key:" + fileInfoKey + ") with owner (key:" + userKey + ") doesnt exists" );
		return this.copyToTmp(fileInfo);
	}

	private String copyToTmp(Fileinfo fileInfo) throws IOException 
	{
		String tmpPath = FileUtils.getAbsoluteTempPath();
		TempFileManager tf = new TempFileManager(tmpPath);
		File copied = tf.copyFile(new File(FileUtils.getRootPath() + fileInfo.getAbsoluteName()));
		return copied.getAbsolutePath();
	}
}