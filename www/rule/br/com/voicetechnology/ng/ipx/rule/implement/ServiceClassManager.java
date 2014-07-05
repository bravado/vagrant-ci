package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.BlockDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.PersistentObject;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.Info;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ClassOfServiceInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class ServiceClassManager extends Manager
{
	private ServiceclassDAO scDAO;
	private BlockDAO bDAO;
	private ConfigDAO confDAO;
	
	private BlockManager blockManager;
	
	public ServiceClassManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		
		scDAO = dao.getDAO(ServiceclassDAO.class);
		bDAO = dao.getDAO(BlockDAO.class);
		confDAO = dao.getDAO(ConfigDAO.class);
		
		blockManager = new BlockManager(logger);
	}

	public <T extends Info> ReportResult<T> find(Report<ClassOfServiceInfo> report) throws DAOException
	{
		ReportDAO<Serviceclass, ClassOfServiceInfo> gReport = dao.getReportDAO(ServiceclassDAO.class);
		Long size = gReport.getReportCount(report);
		List<Serviceclass> scList = gReport.getReportList(report);
		List<ClassOfServiceInfo> scInfoList = new ArrayList<ClassOfServiceInfo>();
		for(Serviceclass sc : scList)
			scInfoList.add(new ClassOfServiceInfo(sc));
		return (ReportResult<T>) new ReportResult<ClassOfServiceInfo>(scInfoList, size);
	}
	
	public ClassOfServiceInfo getClassOfServiceInfoByKey(Long serviceclassKey) throws DAOException
	{
		Serviceclass sc = scDAO.getServiceclassFull(serviceclassKey);
		Block  bIn = bDAO.getBlockWithItens(sc.getConfigKey(), Block.TYPE_INCOMING);
		Block  bOut = bDAO.getBlockWithItens(sc.getConfigKey(), Block.TYPE_OUTGOING);
//		//Thiago Veiga RC12 Patch 4 issue 5634
		int timeout = Integer.parseInt(IPXProperties.getProperty(IPXPropertiesType.SERVICE_CLASS_TIMEOUT));
		ClassOfServiceInfo classOfServiceInfo = new ClassOfServiceInfo(sc, bIn, bOut);
		classOfServiceInfo.setService_class_timeout(timeout);
		return classOfServiceInfo;
	}

	public void save(ClassOfServiceInfo scInfo) throws DAOException, ValidateObjectException
	{
		Serviceclass sc = scInfo.getServiceclass();
		Config config = sc.getConfig();
		if(config.getForwardType() == null)
			config.setForwardType(Config.FORWARD_TYPE_DEFAULT);		
		config.setAllowedVoiceMail(Config.NOT_ALLOWED_VOICEMAIL);		
		config.setAllowedGroupForward(Config.NOT_ALLOWED_GROUPFORWARD);		
		config.setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN);
		confDAO.save(config);
		sc.setConfigKey(sc.getConfig().getKey());
		if(sc.getScreenPopUpType() == null)
			sc.setScreenPopUpType(Serviceclass.SCREENPOPUP_TYPE_WINDOW);
		scDAO.save(sc);
		blockManager.removeConfigBlocks(sc.getConfigKey());
		blockManager.saveBlock(scInfo.getIncomingBlock());
		blockManager.saveBlock(scInfo.getOutgoingBlock());
		blockManager.saveConfigblock(scInfo.getIncomingBlock().getKey(), sc.getConfigKey());
		blockManager.saveConfigblock(scInfo.getOutgoingBlock().getKey(), sc.getConfigKey());
	}

	public void deleteClassesOfService(List<Long> serviceclassKeyList) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		for(Long key :  serviceclassKeyList)
			deleteServiceClass(key);
	}

	private void deleteServiceClass(Long serviceclassKey) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		//Serviceclass, Config
		Serviceclass sc = deleteServiceclassAndDependences(serviceclassKey);
		
		//Block incoming, Block outgoing
		blockManager.deleteBlock(sc.getConfigKey(), Block.TYPE_INCOMING);
		blockManager.deleteBlock(sc.getConfigKey(), Block.TYPE_OUTGOING);
		blockManager.deleteConfigblock(sc.getConfigKey());
		
	}

	private Serviceclass deleteServiceclassAndDependences(Long serviceclassKey) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Serviceclass sc = scDAO.getServiceclassFull(serviceclassKey);
		Integer users = scDAO.getPbxuserListInServiceclass(serviceclassKey).size();
		if(users > 0)
			throw new DeleteDependenceException("Cannot delete " + sc.getName() + ": " + users + " users are bound to it", User.class, users, sc);
		
		sc.setActive(PersistentObject.DEFINE_DELETED);
		scDAO.save(sc);
		sc.getConfig().setActive(PersistentObject.DEFINE_DELETED);
		confDAO.save(sc.getConfig());
		
		return sc;
	}

	public Serviceclass getServiceclassFullByKey(Long serviceclassKey) throws DAOException
	{
		return scDAO.getServiceclassFull(serviceclassKey);
	}
	
	public String getScreenPopUpPath(Long serviceClassKey) throws DAOException
	{
		Serviceclass sc = scDAO.getByKey(serviceClassKey);
		if(sc == null)
			return null;
		return sc.getScreenPopUpPath();
	}
}