package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserterminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PhoneDialPlanGroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserroleDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.PersistentObject;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Filter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuserterminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.PhoneDialPlanGroup;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userrole;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CallFilterInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.TerminalInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class TerminalManager extends Manager
{
	private TerminalDAO tDAO;
	private AddressDAO addDAO;
	private ServiceclassDAO scDAO;
	private PbxuserDAO puDAO;
	private UserDAO uDAO;
	private ConfigDAO confDAO;
	private PbxuserterminalDAO ptDAO;
	private RoleDAO roleDAO; 
	private UserroleDAO userRoleDAO;
	private ForwardDAO forwardDAO;
	private ConfigManager configManager;
	private PhoneDialPlanGroupDAO phoneDialPlanGroupDAO;
	private AddressManager addManager;
	private final char[] charList = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	
	public TerminalManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		tDAO = dao.getDAO(TerminalDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		scDAO = dao.getDAO(ServiceclassDAO.class);
		puDAO = dao.getDAO(PbxuserDAO.class);
		uDAO = dao.getDAO(UserDAO.class);
		confDAO = dao.getDAO(ConfigDAO.class);
		ptDAO = dao.getDAO(PbxuserterminalDAO.class);
		roleDAO = dao.getDAO(RoleDAO.class);
		userRoleDAO = dao.getDAO(UserroleDAO.class);
		forwardDAO = dao.getDAO(ForwardDAO.class);
		configManager = new ConfigManager(ConfigManager.class.getName());
		phoneDialPlanGroupDAO = dao.getDAO(PhoneDialPlanGroupDAO.class);
		addManager = new AddressManager(logger);
	}

	public ReportResult find(Report<TerminalInfo> report) throws DAOException
	{
		ReportDAO<Terminal, TerminalInfo> tReport = dao.getReportDAO(TerminalDAO.class);
		Long size = tReport.getReportCount(report);
		List<Terminal> tList = tReport.getReportList(report);
		List<TerminalInfo> tInfoList = new ArrayList<TerminalInfo>();
		for(Terminal t : tList)
		{
			//List<Address> addList = t.getPbxuser().getAddressList();
			List<Address> addList = addDAO.getExtensionListByPbxuser(t.getPbxuser().getKey());
			//tveiga issue 5852 - inicio 
			TerminalInfo terminalInfo = new TerminalInfo(t, addList.size() > 0 ? addList.get(0) : null);
			if(t.getPbxuserterminalList() != null && t.getPbxuserterminalList().size() > 0)
			{
				Pbxuserterminal pbxuserTerminal = t.getPbxuserterminalList().get(0);
				if(pbxuserTerminal != null)
				 terminalInfo.setAssociatedUser(pbxuserTerminal.getPbxuser().getUser().getUsername()); 
			}
			tInfoList.add(terminalInfo);
			//tveiga issue 5852 - inicio 
		}
		return new ReportResult<TerminalInfo>(tInfoList, size);
	}
	
	public TerminalInfo getTerminalInfoByKey(Long terminalKey) throws DAOException
	{
		Terminal t = tDAO.getTerminalFull(terminalKey);
		List<Address> addList = addDAO.getExtensionListByPbxuser(t.getPbxuserKey());
		Address extension = addList.size() > 0 ? addList.get(0) : null;
		TerminalInfo tInfo = new TerminalInfo(t, extension);		
		tInfo.addPbxuserListInTerminal(puDAO.getPbxuserKeyAndUsernameByTerminal(terminalKey, tInfo.getDomainKey(), true));
		getTerminalInfoContext(tInfo, tInfo.getDomainKey());
		return tInfo;
	}
	
	public void getTerminalInfoContext(TerminalInfo tInfo, Long domainKey) throws DAOException
	{
		Serviceclass sc = (Serviceclass) scDAO.getDefaultServiceclass(domainKey);
		tInfo.setServiceclassKey(sc.getKey());
		List<PhoneDialPlanGroup> groups = phoneDialPlanGroupDAO.getByDomainKey(domainKey);
		List<Duo<Long, String>> phonedialplangroups = new ArrayList<Duo<Long,String>>();
		for(PhoneDialPlanGroup group : groups)
			phonedialplangroups.add(new Duo<Long, String>(group.getKey(), group.getName()));
		tInfo.setPhoneDialPlanGroups(phonedialplangroups);
		
		//tInfo.addPbxuserListOutTerminal(puDAO.getPbxuserKeyAndUsernameByTerminal(tInfo.getKey(), domainKey, false));
	}	

	public TerminalInfo getPbxuserListOutTerminal(Long terminalKey, Long domainKey, Integer lastIndex) throws DAOException
	{
		TerminalInfo info = new TerminalInfo();
		List<Duo<Long, String>> pbxuserListOutTerminal = new ArrayList<Duo<Long,String>>();		
		if(!info.isPbxuserListOut())
		{
			pbxuserListOutTerminal = puDAO.getPbxuserKeyAndUsernameByTerminal(terminalKey, domainKey, false, lastIndex, TerminalInfo.LIST_MAX_LENGTH);
			info.addPbxuserListOutTerminal(pbxuserListOutTerminal);
			if(pbxuserListOutTerminal.size() < TerminalInfo.LIST_MAX_LENGTH)
			{
				info.setPbxuserListOut(true);
				info.setListLastIndex(0);
			}else
			{
				info.setListLastIndex(lastIndex);
				info.setListLastIndex(info.getListLastIndex() + pbxuserListOutTerminal.size());
			}	
		}			
		
		return info;
	}
	
	public void deleteTerminals(List<Long> terminalKeyList) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		for(Long terminalKey : terminalKeyList)
			deleteTerminal(terminalKey);
	}
	
	public void deleteTerminal(Long terminalKey) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Terminal t = tDAO.getTerminalFull(terminalKey);
		Pbxuser pu = t.getPbxuser();
		pu.setActive(PersistentObject.DEFINE_DELETED);
		puDAO.save(pu);
		User u = pu.getUser();
		u.setActive(PersistentObject.DEFINE_DELETED);
		uDAO.save(u);
		t.setActive(PersistentObject.DEFINE_DELETED);
		tDAO.save(t);
		pu.getConfig().setActive(PersistentObject.DEFINE_DELETED);
		confDAO.save(pu.getConfig());
		
		addManager.removeAllAddress(pu.getKey(), false, false);
		List<Pbxuserterminal> ptList = ptDAO.getPbxuserterminalByTerminal(t.getKey());
		for(Pbxuserterminal pt : ptList)
			ptDAO.remove(pt);
	}

	public void save(TerminalInfo terminalInfo) throws DAOException, ValidateObjectException
	{
		Terminal t = terminalInfo.getTerminal();
		boolean edit = t.getKey() != null;		
		Pbxuser pu = t.getPbxuser();
		User u = pu.getUser();
		
		if(u.getName() == null)
			u.setName(u.getUsername());
		uDAO.save(u);

		if(!edit)
		{
			addRole(u);
			createTerminalConfig(pu);
		} else
			pu.setConfigKey(confDAO.getConfigByPbxuser(pu.getKey()).getKey());

		pu.setUserKey(u.getKey());
		pu.setIsAnonymous(Pbxuser.ANONYMOUS_OFF);
		pu.setDefaultDIDKey(null);
		puDAO.save(pu);
		Config conf = confDAO.getConfigByPbxuser(pu.getKey());
		conf.setEletronicLockStatus(terminalInfo.getEletronicLockStatus());
		conf.setCostCenterStatus(terminalInfo.getCostCenterStatus());
		confDAO.save(conf);
		t.setPbxuserKey(pu.getKey());
		tDAO.save(t);

		if(terminalInfo.getAddress() != null && terminalInfo.getAddress().getAddress() != null)
			pu.getAddressList().add(terminalInfo.getAddress());
		addManager.saveAddressList(pu);

		if(edit)
		{
			for(Pbxuserterminal pt : ptDAO.getPbxuserterminalByTerminal(t.getKey()))
				ptDAO.remove(pt);

			if(terminalInfo.getPbxuserterminalList() != null && terminalInfo.getPbxuserterminalList().size() > 0)
			{
				verifyForwardsAssociatedUser(terminalInfo.getPbxuserterminalList(), pu.getKey());
				//jluchetta - Início - Eliminar filtro com target terminal que está associado a ele mesmo (Bug ID 5275)
				verifyCallFilteringsAssociatedUser(terminalInfo.getPbxuserterminalList(), pu.getKey());
				//jluchetta - Fim - Eliminar filtro com target terminal que está associado a ele mesmo (Bug ID 5275)
			}
			
		}

		List<Pbxuserterminal> ptList = terminalInfo.getPbxuserterminalList();
		if(ptList != null)
			for(Pbxuserterminal pt : ptList)
				addPbxuserInTerminal(t, pt);
	}
	
	private void verifyForwardsAssociatedUser(List<Pbxuserterminal> pbxuserTerminalList, Long terminalPbxuserKey) throws DAOException, ValidateObjectException
	{
		for(Pbxuserterminal pt : pbxuserTerminalList)
		{
			Pbxuser pbxuser = puDAO.getByKey(pt.getPbxuserKey());
			List<Forward> forwardList = forwardDAO.getForwardListByConfig(pbxuser.getConfigKey());
			Address sipIDTerminal = addDAO.getSipIDByPbxuser(terminalPbxuserKey);
			for(Forward f : forwardList)
				if(f.getAddressKey() != null && f.getAddressKey().equals(sipIDTerminal.getKey()))
				{
					f.setAddressKey(null);
					f.setStatus(Forward.STATUS_OFF);
					f.setTarget(null);
					forwardDAO.save(f);
				}
		}
	}
	
	//jluchetta - Método para verificar filtros e deletar caso o target seja um terminal associado ao usuário dono do filtro (Bug ID: 5275)
	private void verifyCallFilteringsAssociatedUser(List<Pbxuserterminal> pbxuserTerminalList, Long terminalPbxuserKey) throws DAOException, ValidateObjectException
	{
		for(Pbxuserterminal pt : pbxuserTerminalList)
		{
			Pbxuser pbxuser = puDAO.getByKey(pt.getPbxuserKey());
			CallFilterInfo callfilter = configManager.getPbxuserCallFilter(pbxuser.getKey());
			Address sipIDTerminal = addDAO.getSipIDByPbxuser(terminalPbxuserKey);
			List<Filter> filterList = callfilter.getFilterList();
			Address addressTarget = null;
			List<Filter> newFilterList = new ArrayList<Filter>();
			for(Filter filter : filterList)
			{
				if(filter.getTarget() != null)
				{
					addressTarget = addDAO.getAddress(filter.getTarget(), pbxuser.getUser().getDomainKey());
					if(addressTarget == null || ( addressTarget!= null && !addressTarget.getKey().equals(sipIDTerminal.getKey())))
						newFilterList.add(filter);
											
				}else
					newFilterList.add(filter);
			}
			callfilter.setFilterList(newFilterList);
			
			callfilter.setPbxuserKey(pbxuser.getKey());
			callfilter.setConfigKey(pbxuser.getConfigKey());
			configManager.updatePbxuserCallFilter(callfilter);
		}
	}

	private void createTerminalConfig(Pbxuser pu) throws DAOException, ValidateObjectException 
	{
		Config conf = createDefaultConfig();
		confDAO.save(conf);
		pu.setConfigKey(conf.getKey());
	}

	private void addRole(User u) throws DAOException, ValidateObjectException
	{
		Role defaultRole = roleDAO.getDefaultRole();
		Userrole userRole = new Userrole();
		userRole.setRoleKey(defaultRole.getKey());
		userRole.setUserKey(u.getKey());
		userRoleDAO.save(userRole);
	}
	
	private void addPbxuserInTerminal(Terminal t, Pbxuserterminal pt) throws DAOException, ValidateObjectException
	{
		pt.setKey(null);
		pt.setTerminalKey(t.getKey());
		ptDAO.save(pt);		
	}
	
	private Config createDefaultConfig()
	{
		Config conf = new Config();
		conf.setActive(PersistentObject.DEFINE_ACTIVE);
		conf.setDisableVoicemail(Config.VOICEMAIL_OFF);
		conf.setDndStatus(Config.DND_OFF);
		conf.setEmailNotify(Config.EMAILNOTIFY_OFF);
		conf.setAttachFile(Config.ATTACH_FILE_OFF);
		conf.setForwardType(Config.FORWARD_TYPE_DEFAULT);
		conf.setAllowedGroupForward(Config.NOT_ALLOWED_GROUPFORWARD);
		conf.setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN);
		conf.setAllowedVoiceMail(Config.NOT_ALLOWED_VOICEMAIL);
		return conf;
	}

	public TerminalInfo createTerminalInfo(String macAddress)
	{
		macAddress = normalizeMACAddress(macAddress);
		TerminalInfo tInfo = new TerminalInfo();
		byte[] macArray = normalizeMACAddress(macAddress).getBytes();
		tInfo.setUsername(createUsername(macArray));
		tInfo.setPassword(createPassword(macArray));	
		tInfo.setMACAddress(macAddress);
		return tInfo;
	}
	
	public String createUsername(byte[] address)
	{
		int[] intArray = new int[address.length * 2];
		for(int i = 0; i < address.length; i++)
		{
			int tmp = address[i];
			tmp = tmp + 13 + (i % 2 == 0 ? 9 : 7);
			int first = tmp / 17;
			intArray[i * 2] = first - 7;
			int second = (tmp - first) % 10;
			intArray[i * 2 + 1] = second;
		}
		return getStringValue(intArray);
	}

	public String createPassword(byte[] address)
	{
		int[] intArray = new int[address.length * 2];
		for(int i = 0; i < address.length; i++)
		{
			int tmp = address[i];
			tmp = tmp + 7 + (i % 2 == 0 ? -3 : 5);
			int first = tmp / 15;
			intArray[i * 2] = first - 5;
			int second = (tmp - first) % 10;
			intArray[i * 2 + 1] = second;
		}
		return getStringValue(intArray);
	}
	
	private String getStringValue(int[] intArray)
	{
		char[] value = new char[intArray.length / 2];
		for(int i = 0; i < intArray.length; )
		{
			int tmp = (intArray[i++] * 10);
			tmp += intArray[i++];
			if(tmp < 0)		tmp = tmp * -1;
			tmp %= 122;
			value[i / 2 - 1] = charList[tmp];
		}
		
		char[] result = new char[value.length];
		int[] transfer = new int[]{10, 7, 8, 2, 5, 0, 3, 1, 4, 11, 9, 6};
		System.arraycopy(value, 0, result, 0, value.length);
		for(int i = 0; i < result.length; i++)
			result[i] = value[transfer[i]];
		return new String(result);
	}
	
	private String normalizeMACAddress(String macAddress)
	{		
		macAddress = macAddress.replaceAll("-|\\.|\\:", "");
		return macAddress.toUpperCase();
	}
	
    public Pbxuser getAssociatedPbxUserByTerminalPbxuserKey(Long terminalPbxuserkey) throws DAOException
    {
        return tDAO.getAssociatedPbxuserByTerminalPbxuserKey(terminalPbxuserkey);
    }
}
