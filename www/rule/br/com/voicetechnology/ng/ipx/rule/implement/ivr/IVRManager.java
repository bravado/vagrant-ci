package br.com.voicetechnology.ng.ipx.rule.implement.ivr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.NoFilesException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.QuotaException;
import br.com.voicetechnology.ng.ipx.commons.security.crypt.Crypt;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVRDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVROptionDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.FileinfoDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVR;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVROption;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.prompt.PromptFile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Preference;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.IVRInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.implement.PbxuserManager;
import br.com.voicetechnology.ng.ipx.rule.implement.prompt.PromptsFile;

public class IVRManager extends PbxuserManager
{
	private ServiceclassDAO scDAO;
	private IVRDAO ivrDAO;
	private IVROptionDAO ivroptDAO;
	private RoleDAO rDAO;
	private FileinfoDAO fileDAO;
	private ForwardDAO forwardDAO;
	private AddressDAO addDAO;
	private PbxDAO pbxDAO;
	private PbxuserDAO puDAO;
	private DomainDAO domainDAO;

	private PromptsFile prompts;
	
	public IVRManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		ivrDAO = dao.getDAO(IVRDAO.class);
		ivroptDAO = dao.getDAO(IVROptionDAO.class);
		scDAO = dao.getDAO(ServiceclassDAO.class);
		rDAO = dao.getDAO(RoleDAO.class);
		fileDAO = dao.getDAO(FileinfoDAO.class);
		forwardDAO = dao.getDAO(ForwardDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		puDAO  = dao.getDAO(PbxuserDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
	}
	
	public ReportResult<IVRInfo> findIVR(Report<IVRInfo> report) throws Exception
	{
		ReportDAO<IVR, IVRInfo> ivrReport = dao.getReportDAO(IVRDAO.class);
		Long size = ivrReport.getReportCount(report);
		List<IVR> ivrList = ivrReport.getReportList(report);
		List<IVRInfo> ivrInfoList = new ArrayList<IVRInfo>();
		for(IVR ivr : ivrList)
		{			
			ivr.getPbxuser().setAddressList(addDAO.getExtensionListByPbxuser(ivr.getPbxuserKey()));
			ivrInfoList.add(new IVRInfo(ivr));
		}
		return new ReportResult<IVRInfo>(ivrInfoList, size);		
	}
	
	public IVRInfo getIVRInfoByKey(Long ivrKey) throws DAOException, QuotaException
	{
		IVR ivr = ivrDAO.getIVRFull(ivrKey);
		Forward forwardNoAnswer = forwardDAO.getForwardByConfig(ivr.getPbxuser().getConfigKey(), Forward.ALWAYS_MODE);
		IVRInfo info = new IVRInfo(ivr, forwardNoAnswer);
		Pbxuser pu = ivr.getPbxuser();
		pu.setAddressList(addDAO.getExtensionListByPbxuser(pu.getKey()));
		for(Address ext : pu.getAddressList())
			info.addExtension(new Duo<Long, String>(ext.getKey(), ext.getAddress()));
		Long domainKey = pu.getUser().getDomainKey();
		setSipIDList(info, domainKey, false, info.getName());
		info.addFileList(fileDAO.listSimpleFiles(domainKey));
		
		List<Address> didList = addDAO.getDIDListByPbxuser(pu.getKey());
		for(Address did : didList)
			info.addDID(new Duo<Long, String>(did.getKey(), did.getAddress()));

		Long pbxKey = pbxDAO.getPbxByDomain(domainKey).getKey();
		info.addServiceClassList(scDAO.getCentrexServiceclassKeyAndName());
		info.addServiceClassList(scDAO.getServiceclassKeyAndName(pbxKey));
		
		return info;
	}

	public void getIVRInfoContext(IVRInfo info, Long domainKey, boolean validateQuota) throws DAOException, QuotaException, NoFilesException
	{
		List<Duo<Long, String>> ivrFileList = fileDAO.getFilesInDomain(domainKey, Fileinfo.TYPE_SIMPLEFILE);
		if(ivrFileList == null || ivrFileList.size() == 0)
			throw new NoFilesException("No IVR Files in domain", NoFilesException.Type.IVR);
		
		Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		if(validateQuota)
		{
			Long ivrAmount = ivrDAO.countIVRs(domainKey);
			if(ivrAmount >= pbx.getMaxIVRApplication())
				throw new QuotaException("IVR Quota exceeded", QuotaException.Type.IVR);
		}
		setSipIDList(info, domainKey, false);

		info.addServiceClassList(scDAO.getCentrexServiceclassKeyAndName());
		info.addServiceClassList(scDAO.getServiceclassKeyAndName(pbx.getKey()));
		
		info.addFileList(fileDAO.listSimpleFiles(domainKey));
	}
	
	public void deleteIVRs(List<Long> ivrKeyList) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		for(Long ivrKey : ivrKeyList)
			deleteIVR(ivrKey);
	}

	public void deleteIVR(Long ivrKey) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		IVR ivr = ivrDAO.getByKey(ivrKey);
		deletePbxuser(ivr.getPbxuserKey(), false);
		ivr.setActive(IVR.DEFINE_DELETED);
		ivr.setEotForwardAddressKey(null);
		ivr.setSalutationForwardAddressKey(null);
		ivr.setEotFileKey(null);
		ivr.setSalutationFileKey(null);
		ivr.setInvalidFileKey(null);
		ivr.setTimeoutFileKey(null);
		ivrDAO.save(ivr);
		removeIVROptions(ivrKey);
	}
	
	private void removeIVROptions(Long ivrKey) throws DAOException
	{
		List<IVROption> ivrOptionList = ivroptDAO.getIVROptionListByIVR(ivrKey);
		if(ivrOptionList != null && ivrOptionList.size() > 0)
			for(IVROption opt : ivrOptionList)
				ivroptDAO.remove(opt);
	}

	public void save(IVRInfo info) throws DAOException, ValidateObjectException
	{
		save(info, false);
	}
	
	public void save(IVRInfo info, boolean isIVRPreProcessCall) throws DAOException, ValidateObjectException
	{
		IVR ivr = info.getIVR();
		Pbxuser pu = ivr.getPbxuser();
		if(pu.getIsAnonymous() == null)
			pu.setIsAnonymous(Pbxuser.ANONYMOUS_OFF);
		User user = pu.getUser();
		Config config = pu.getConfig();
		config.setDisableVoicemail(Config.VOICEMAIL_OFF);
		config.setEmailNotify(Config.EMAILNOTIFY_OFF);
		config.setAttachFile(Config.ATTACH_FILE_OFF);
		config.setForwardType(Config.FORWARD_TYPE_DEFAULT);
		config.setAllowedGroupForward(Config.NOT_ALLOWED_GROUPFORWARD);
		config.setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN);
		config.setAllowedVoiceMail(Config.NOT_ALLOWED_VOICEMAIL);
		config.setAllowedRecordCall(Config.ALLOWEDRECORDCALL_OFF);
		
		if(pu.getServiceclassKey() == null)
		{
			Long serviceclassKey = scDAO.getDefaultServiceclass(info.getDomainKey()).getKey();
			pu.setServiceclassKey(serviceclassKey);			
		}
		
		pu.setActive(Pbxuser.DEFINE_ACTIVE);
		user.setAgentUser(User.TYPE_IVR);
		
		if(user.getUsername().equals(User.IVR_PREPROCESSCALL) && !isIVRPreProcessCall)
			throw new ValidateObjectException("Invalid Name : " + User.IVR_PREPROCESSCALL, User.class, pu.getUser(), ValidateType.INVALID);

		Domain domain = domainDAO.getByKey(user.getDomainKey());
		Domain rootDomain = domainDAO.getRootDomain();
		if(domain != null && rootDomain != null)
		{
			StringBuilder pass = new StringBuilder();
			pass.append(user.getUsername());
			pass.append(domain.getDomain());
			pass.append(rootDomain.getDomain());
			user.setPassword(Crypt.encrypt(user.getUsername(), domain.getDomain(), pass.toString()));
		} else
			user.setPassword(Crypt.encrypt((System.currentTimeMillis() % 100000000) + ""));

		if(user.getName() == null)
			user.setName(user.getUsername());
		user.setActive(User.DEFINE_ACTIVE);
		user.setFarmIP(getFarmIP());
		pu.getUser().getPreference().setClickToCallConfirmation(Preference.CLICKTOCALLCONFIRMATION_DISABLED);
		savePbxuser(pu, false);
		ivr.setPbxuserKey(ivr.getPbxuser().getKey());
		
		//Criacao dos 4 tipos de forward para o IVR para manter o mesmo padrao de Pbxuser jah que ele tambem eh um pbxuser
		if(info.getKey() == null)
			saveForwardList(User.TYPE_IVR, null, null, ivr.getPbxuser().getConfigKey(), info.getForwardAlways(), createForward(Forward.NOANSWER_MODE), createForward(Forward.BUSY_MODE), createForward(Forward.CALL_FAILURE_MODE));
		else
			saveForwardList(User.TYPE_IVR, null, null, ivr.getPbxuser().getConfigKey(), info.getForwardAlways());
		saveAddressList(ivr.getPbxuser());
		Role r = rDAO.getDefaultRole();
		if(r == null)
			throw new NullPointerException("Role default must be set by Admin Centrex!!!");
		List<Long> rList = new ArrayList<Long>();
		rList.add(r.getKey());
		saveUserroleList(ivr.getPbxuser().getUserKey(), rList);
		ivr.setActive(IVR.DEFINE_ACTIVE);
		ivrDAO.save(ivr);
		saveOptions(ivr);
	}
	
	private Forward createForward(Integer type)
	{
		Forward f = new Forward();
		f.setForwardMode(type);
		f.setStatus(Forward.STATUS_OFF);
		return f;
	}
	
	private String getFarmIP() 
	{
		String farmIp = IPXProperties.getProperty(IPXPropertiesType.APP_FARM_IP);
		if(farmIp == null)
			throw new NullPointerException("Property not found: centrex.farm.ip");
		return farmIp;
	}
	
	private void saveOptions(IVR ivr) throws DAOException, ValidateObjectException
	{
		List<IVROption> optList = new ArrayList<IVROption>(ivr.getOptions().values());
		List<IVROption> oldList = ivroptDAO.getIVROptionListByIVR(ivr.getKey());
		for (IVROption option : oldList)
			ivroptDAO.remove(option);
		for (IVROption option : optList) 
			ivroptDAO.save(fillIVROption(option, ivr.getKey()));
	}

	//retorna Address por falicidade de deixar o loop com uma linha...
	private IVROption fillIVROption(IVROption opt, Long ivrKey)
	{
		opt.setIvrKey(ivrKey);
		return opt;
	}

	//call control
	public List<IVR> getIVRListByFarmIP(String farmIP) throws DAOException
	{
		return ivrDAO.getIVRListByFarmIP(farmIP);
	}

	public Block getOutgoingBlockByIVRKey(Long ivrKey) throws DAOException
	{
		return ivrDAO.getOutgoingBlockByIVRKey(ivrKey);
	}
	
	public List<IVR> getIVRListByDomain(String domain) throws DAOException
	{
		return ivrDAO.getIVRListByDomain(domain);
	}
	
	public IVR getIVRApplication(Long ivrKey) throws DAOException
	{
		IVR ivr = ivrDAO.getIVRWithOptions(ivrKey);
		//fill options target
		if(ivr.getOptions().size() > 0)
			for(IVROption option : ivr.getOptions().values())
				if(option.getForwardAddressKey() != null)
					option.setForwardAddress(addDAO.getByKey(option.getForwardAddressKey()));
		//address
		if(ivr.getEotForwardAddressKey() != null)
			ivr.setEotForwardAddress(addDAO.getByKey(ivr.getEotForwardAddressKey()));
		if(ivr.getSalutationForwardAddressKey() != null)
			ivr.setSalutationForwardAddress(addDAO.getByKey(ivr.getSalutationForwardAddressKey()));
		//file
		if(ivr.getEotFileKey() != null)
			ivr.setEotFile(fileDAO.getByKey(ivr.getEotFileKey()));
		if(ivr.getInvalidFileKey() != null)
			ivr.setInvalidFile(fileDAO.getByKey(ivr.getInvalidFileKey()));		
		if(ivr.getSalutationFileKey() != null)
			ivr.setSalutationFile(fileDAO.getByKey(ivr.getSalutationFileKey()));
		if(ivr.getTimeoutFileKey() != null)
			ivr.setTimeoutFile(fileDAO.getByKey(ivr.getTimeoutFileKey()));	
		
		ivr.setPbxuser(puDAO.getPbxuserFull(ivr.getPbxuserKey()));
		
		return ivr;
	}
	
	//inicio --> dnakamashi - Caixa de  Gravação- 3.0.5
	public String[] getPrompt(String locale, PromptFile ivrPrompt)
	{
		return getIVRPrompts().getPromptPath(locale, ivrPrompt);
	}
	
	private PromptsFile getIVRPrompts()
	{
		if(this.prompts == null)
			prompts = new PromptsFile();
		return prompts;
	}
	//fim --> dnakamashi - Caixa de  Gravação- 3.0.5	
}