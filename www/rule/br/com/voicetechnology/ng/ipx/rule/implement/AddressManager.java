package br.com.voicetechnology.ng.ipx.rule.implement;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.QuotaException;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.DialPlanDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVRDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ivr.IVROptionDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVR;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVROption;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.AddressInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.DIDInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.NightmodeGroup;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class AddressManager extends Manager
{
	private AddressDAO addDAO;
	private PbxuserDAO puDAO;
	private GroupDAO groupDAO;	
	private ForwardDAO forwardDAO;
	private PbxDAO pbxDAO;
	private IVRDAO ivrDAO;
	private ConfigDAO cDAO;
	private ActivecallDAO actDAO;
	private DomainDAO domainDAO;
	private TerminalDAO terminalDAO;
	private DialPlanDAO dialPlanDAO;
	private IVROptionDAO ivrOptionDAO;
	
	public AddressManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		this.initDAOs();
	}
	
	public AddressManager(Logger logger) throws DAOException
	{
		super(logger);
		this.initDAOs();
	}
	
	private void initDAOs() throws DAOException
	{
		addDAO = dao.getDAO(AddressDAO.class);
		puDAO = dao.getDAO(PbxuserDAO.class);
		groupDAO = dao.getDAO(GroupDAO.class);
		forwardDAO = dao.getDAO(ForwardDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		ivrDAO = dao.getDAO(IVRDAO.class);
		cDAO = dao.getDAO(ConfigDAO.class);
		actDAO = dao.getDAO(ActivecallDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		terminalDAO = dao.getDAO(TerminalDAO.class);
		dialPlanDAO = dao.getDAO(DialPlanDAO.class);
		ivrOptionDAO = dao.getDAO(IVROptionDAO.class);
	}
	
	public ReportResult<DIDInfo> findDids(Report<DIDInfo> report) throws DAOException
	{
		report.getInfo().setTypes(new Integer[]{Address.TYPE_DID});
		return this.executeFind(report, DIDInfo.class);
	}
	
	public ReportResult<AddressInfo> findAddresses(Report<AddressInfo> report) throws DAOException 
	{
		report.getInfo().setTypes(null);
		return this.executeFind(report, AddressInfo.class);
	}
	
	private <T extends AddressInfo> ReportResult<T> executeFind(Report<T> report, Class<T> infoClass) throws DAOException 
	{
		Constructor<T> constructor = null;
		try 
		{
			constructor = infoClass.getConstructor(new Class[]{Address.class, String.class, String.class});
		} catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		ReportDAO<Address, T> addressReportDAO = dao.getReportDAO(AddressDAO.class);
		Long size = addressReportDAO.getReportCount(report);
		List<Address> addressList = addressReportDAO.getReportList(report);
		List<T> infoList = new ArrayList<T>();
		
		Object[] methodParams = new Object[3];
		for(Address address : addressList)
		{
			String sipID = "";
			if(address.getPbxuserKey() != null)
			{
				//inicio -- >bug #4772 - 3.0.2 RC12 patch 4
				Pbxuser pu  = puDAO.getByKey(address.getPbxuserKey());
				if(pu.getUser().getAgentUser() == User.TYPE_TERMINAL)
				{
					User user  = terminalDAO.getAssociatedPbxuserByTerminalUserKey(pu.getUserKey());
					if(user != null)
						sipID = user.getName();
					else
						sipID = " ";
				}	
				else
					sipID = address.getPbxuser().getUser().getUsername();
				//fim -- >bug #4772 - 3.0.2 RC12 patch 4
			}	
			else if(address.getGroupKey() != null)
				sipID = address.getGroup().getName();
			try 
			{
				methodParams[0] = address;
				methodParams[1] = sipID;
				methodParams[2] = null;
				infoList.add(constructor.newInstance(methodParams));
			} catch (Exception e) 
			{
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
		return new ReportResult<T>(infoList, size);
	}

	public DIDInfo getDIDInfoByKey(Long didKey) throws DAOException
	{
		Address did = addDAO.getByKey(didKey);
		DIDInfo info = new DIDInfo(did, makeSipIDToDID(did), null);
		info.addGroupNameSipIDList(groupDAO.getGroupKeyAndNameByDomain(did.getDomainKey()));
		info.addUsernameSipIDList(puDAO.getPbxuserKeyAndUsernameList(did.getDomainKey()));
		info.addIVRSipIDList(ivrDAO.getIVRKeyAndNameList(did.getDomainKey()));
		info.addSipTrunkNameSipIDList(puDAO.getSipTrunkKeyAndUsernameList(did.getDomainKey()));
		return info;
	}

	private String makeSipIDToDID(Address did) throws DAOException
	{
		String sipID = null;
		if(did != null)
			if(did.getPbxuserKey() != null)
			{
				Pbxuser pu = puDAO.getPbxuserAndUser(did.getPbxuserKey());
				sipID = pu != null && pu.getUser() != null ? pu.getUser().getUsername() : null;
			} else if(did.getGroupKey() != null)
			{
				Group g = groupDAO.getByKey(did.getGroupKey());
				sipID = g != null ? g.getName() : null;
			}
		return sipID;
	}
	
	public ReportResult findDIDCentrex(Report<DIDInfo> info) throws DAOException
	{
		Long size = addDAO.getReportCountCentrex(info);
		List<Address> addressList = addDAO.getReportListCentrex(info);
		List<DIDInfo> addressInfoList = new ArrayList<DIDInfo>(addressList.size());
		Long domainKey = info.getInfo().getDomainKey();
		Domain domain = domainKey != null ? domainDAO.getByKey(domainKey) : domainDAO.getRootDomain();
		List<Duo<Long, String>> domainList = domainDAO.getDomainsByRootDomain(domain.getRootKey() != null ? domain.getRootKey() : domain.getKey());
		for(Address address : addressList)
		{
			domain = domainDAO.getByKey(address.getDomainKey());
			DIDInfo didInfo = new DIDInfo(address, null, domain.getDomain());
			didInfo.addDomainList(domainList);
			addressInfoList.add(didInfo);
		}
		return new ReportResult<DIDInfo>(addressInfoList, size);	
	}
	
	public DIDInfo getDIDInfoCentrexByKey(Long didKey) throws DAOException, ValidateObjectException
	{
		Address did = addDAO.getByKey(didKey);
		if(did.getPbxKey() != null)
			throw new ValidateObjectException("Cannot edit DID because it's Trunk Line of a PBX!!!", Address.class, did, ValidateType.DEPENDENCE);
		Domain domain = domainDAO.getByKey(did.getDomainKey());
		DIDInfo info = new DIDInfo(did, null, domain.getDomain());
		getDIDInfoContext(info, domain.getRootKey() != null ? domain.getRootKey() : domain.getKey());
		return info;
	}
	
	public void getDIDInfoContext(DIDInfo didInfo, Long domainRootKey) throws DAOException
	{
		didInfo.addDomainList(domainDAO.getDomainsByRootDomain(domainRootKey));
	}
	
	public void deleteDID(List<Long> didKeys) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		for(Long didKey : didKeys)
			this.deleteDID(didKey,false);
	}

	public void deleteDID(Long didKey, boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Address did = addDAO.getByKey(didKey);
		if(did.getPbxKey() != null)
		{			
			if(!isForced || !trySetNewTrunkLine(did.getDomainKey(), did.getPbxKey()))
				throw new DeleteDependenceException("Cannot delete " + did.getAddress() + " because it's Trunk Line of a PBX!!!", Address.class, 1L, did);			
		}
			
		
		List<Pbxuser> userList = puDAO.getPbxuserListByDefaultDID(did.getKey());
		for(Pbxuser pbxuser : userList)
		{
			pbxuser.setDefaultDIDKey(null);
			puDAO.save(pbxuser);
		}

		did.setActive(Address.DEFINE_DELETED);
		//tveiga - Ao deletar um did é necessário excluir a sua referencia ao pbxuser 
		did.setPbxuserKey(null);
		did.setGroupKey(null);
		addDAO.save(did);
	}
	
	//Este método tenta setar um novo trunk line para o pbx, mas senão existir did´s disponíveis o pbx fica sem trunk line
	private boolean trySetNewTrunkLine(Long domainKey, Long pbxKey) throws DAOException, ValidateObjectException
	{
		List<Address> addressList =  addDAO.getDIDsInDomain(domainKey);
		for(Address address : addressList)
		{
			if(address.getGroupKey() == null && address.getPbxKey() == null && address.getPbxuserKey() == null)
			{
				address.setPbxKey(pbxKey);
				addDAO.save(address);
				return true;
			}
		}
		return false;
	}
	
	public void changeDIDStatus(List<Long> didKeyList, int status) throws DAOException, ValidateObjectException, DeleteDependenceException 
	{
		for(Long key : didKeyList) 
			changeDIDStatus(key, status);
	}
	
	public void changeDIDStatus(Long didKey, int status) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Address add = addDAO.getByKey(didKey);
		if(add != null)
			if(add.getPbxKey() != null && status == Address.DEFINE_DELETED)
				throw new DeleteDependenceException("Cannot delete " + add.getAddress() + " because its trunk line of Pbx", Address.class, 1, add);
			else
			{
				add.setActive(status);
				
				if(add.getActive() == Address.DEFINE_DELETED)
				{
					List<Pbxuser> pbxuserList = puDAO.getPbxuserListByDefaultDID(add.getKey());
					for(Pbxuser pu : pbxuserList)
					{
						pu.setDefaultDIDKey(null);
						puDAO.save(pu);
					}
					
					add.setPbxuserKey(null);
					add.setGroupKey(null);
				}
				addDAO.save(add);
			}
	}
	
	public void save(DIDInfo didInfo) throws DAOException, ValidateObjectException
	{
		boolean edit = didInfo.getKey() != null;
		Address did = didInfo.getAddress();
		did.setStaticaddress(Address.STATIC_ON);
		did.setType(Address.TYPE_DID);
		did.setActive(Address.DEFINE_ACTIVE);
		validateDID(did);
		if(edit)
		{
			Address didTmp = addDAO.getByKey(didInfo.getKey());
			
			if(didIsTrunkLine(didTmp.getAddress()))				
				throw new ValidateObjectException("DID is trunk line and cannot be associated to a new domain!!", Address.class, did, ValidateType.DEPENDENCE);
				
			if(!didTmp.getDomainKey().equals(did.getDomainKey()))
			{				
				List<Pbxuser> pbxuserList = puDAO.getPbxuserListByDefaultDID(didTmp.getKey());
				if(pbxuserList != null && pbxuserList.size() > 0)
					for(Pbxuser pu : pbxuserList)
					{
						pu.setDefaultDIDKey(null);
						puDAO.save(pu);
					}
				didTmp.setPbxuserKey(null);
				didTmp.setGroupKey(null);
				didTmp.setPbxKey(null);
			} else if(did.getPbxuserKey() != null || did.getGroupKey() != null)
			{
				if(didTmp.getPbxuserKey() != null && did.getPbxuserKey() != null && !didTmp.getPbxuserKey().equals(did.getPbxuserKey()))
					resetDefaultANIUser(didTmp.getPbxuserKey()); //change from user to user
				else if(didTmp.getPbxuserKey() != null && did.getGroupKey() != null)
					resetDefaultANIUser(didTmp.getPbxuserKey()); //change from user to group
				else if(didTmp.getPbxKey() != null && (did.getPbxuserKey() != null || did.getGroupKey() != null))
					throw new ValidateObjectException("DID is trunk line and cannot be associated with user or group!!", Address.class, did, ValidateType.DEPENDENCE);
				
				// jfarah - 3.1.0
				// bug 5888 - usuarios que usavam did do grupo, continuavam com 
				// o did como defaultdid apos grupo perder did.
				if(didTmp.getGroupKey() != null && // did associado a um grupo
						(did.getGroupKey() == null || !did.getGroupKey().equals(didTmp.getGroupKey()))) // eh associado a outro destino que nao seja esse grupo
				{
					resetDefaultANIToGroupUsers(didTmp.getGroupKey(), didTmp.getKey());
				}
				
				didTmp.setPbxuserKey(did.getPbxuserKey());
				didTmp.setGroupKey(did.getGroupKey());
			} else if(did.getPbxuserKey() == null && did.getGroupKey() == null)
			{
				if(didTmp.getPbxuserKey() != null)
					resetDefaultANIUser(didTmp.getPbxuserKey());
				didTmp.setPbxuserKey(null);
				didTmp.setGroupKey(null);
				didTmp.setPbxKey(did.getPbxKey());
			}
			didTmp.setAddress(did.getAddress());
			didTmp.setStaticaddress(Address.STATIC_ON);
			didTmp.setDomainKey(did.getDomainKey());
			didTmp.setType(Address.TYPE_DID);
			didTmp.setDescription(did.getDescription());
			addDAO.save(didTmp);
		} else
			addDAO.save(did);
	}

	private void resetDefaultANIUser(Long pbxuserKey) throws DAOException, ValidateObjectException
	{
		Pbxuser pu = puDAO.getByKey(pbxuserKey);
		if(pu != null && pu.getDefaultDIDKey() != null)
		{
			pu.setDefaultDIDKey(null);
			puDAO.save(pu);
		}
	}
	
	/**
	 * Change the defaultdid, of all users of the group, to null if the user's defaultdid key is same the currentdidKey.   
	 * @param groupKey group whose users will become with defaultdid null
	 * @param currentDidKey
	 * @throws DAOException
	 * @throws ValidateObjectException
	 */
	private void resetDefaultANIToGroupUsers(Long groupKey, Long currentDidKey) throws DAOException, ValidateObjectException
	{
		List<Usergroup> users = groupDAO.getUsersInGroup(groupKey);
		for(Usergroup userGroup: users)
		{
			if(userGroup.getPbxuser().getDefaultDIDKey() != null &&
					userGroup.getPbxuser().getDefaultDIDKey().equals(currentDidKey))
			{
				userGroup.getPbxuser().setDefaultDIDKey(null);
				puDAO.save(userGroup.getPbxuser());
			}
		}
	}
	
	private void validateDID(Address did) throws ValidateObjectException
	{
		if(did.getType() == Address.TYPE_DID)
		{
			if(!did.getAddress().matches("\\d{6,}"))
				throw new ValidateObjectException("DID must be only digits!", Address.class, did, ValidateType.INVALID);
		}
	}

	/////////////////////////////
	public void saveAddressList(Pbxuser pu) throws DAOException, ValidateObjectException
	{
		User u = pu.getUser();
		saveAddressList(pu.getKey(), u.getUsername(), u.getDomainKey(), pu.getAddressList(), false);
	}

	public void saveAddressList(Group g) throws DAOException, ValidateObjectException
	{
		Long domainKey = domainDAO.getDomainByPbx(g.getPbxKey()).getKey();
		saveAddressList(g.getKey(), g.getName(), domainKey, g.getAddressList(), true);
	}
	
	private void saveAddressList(Long key, String name, Long domainKey, List<Address> addList, boolean isGroup) throws DAOException, ValidateObjectException
	{
		Address sipID = isGroup ? addDAO.getSipIDByGroup(key) : addDAO.getSipIDByPbxuser(key);
		if(sipID == null)
			sipID = createSipID(key, name, domainKey, isGroup);
		else
			sipID.setAddress(name);
		addDAO.save(sipID);
		List<Address> oldList = isGroup ? addDAO.getExtensionListByGroup(key) : addDAO.getExtensionListByPbxuser(key);
		AddressSort sort = new AddressSort();
		Collections.sort(addList, sort);
		Collections.sort(oldList, sort);
		int i = 0, j = 0;
		while(i < addList.size() && j < oldList.size())
		{
			Address add = addList.get(i);
			Address old = oldList.get(j);
			int compare = add.getAddress().compareTo(old.getAddress());
			if(compare == 0)
			{
				i++;	j++;
			}else if(compare < 0)
			{
				addDAO.save(fillAddress(add, key, domainKey, isGroup));
				i++;
			}else
			{
				addDAO.remove(old);
				j++;
			}
		}
		if(i < addList.size())
			while(i < addList.size())
				addDAO.save(fillAddress(addList.get(i++), key, domainKey, isGroup));
		else if (j < oldList.size())
			while(j < oldList.size())
				addDAO.remove(oldList.get(j++));
	}

	//retorna Address por falicidade de deixar o loop com uma linha...
	private Address fillAddress(Address add, Long key, Long domainKey, boolean isGroup)
	{
		add.setDomainKey(domainKey);
		add.setType(Address.TYPE_EXTENSION);
		add.setStaticaddress(Address.STATIC_ON);
		add.setActive(Address.DEFINE_ACTIVE);
		if(isGroup)
			add.setGroupKey(key);
		else
			add.setPbxuserKey(key);
		return add;
	}
	
	public Address createSipID(Pbxuser pu)
	{
		User u = pu.getUser();
		return createSipID(pu.getKey(), u.getUsername(), u.getDomainKey(), false);
	}
	
	public Address createSipID(Group g) throws DAOException
	{
		Long domainKey = domainDAO.getDomainByPbx(g.getPbxKey()).getKey();
		return createSipID(g.getKey(), g.getName(), domainKey, true);
	}
	
	private Address createSipID(Long key, String sipID, Long domainKey, boolean isGroup)
	{
		Address add = new Address();
		add.setAddress(sipID);
		add.setDomainKey(domainKey);
		add.setStaticaddress(Address.STATIC_ON);
		add.setType(Address.TYPE_SIPID);
		add.setActive(Address.DEFINE_ACTIVE);
		if(isGroup)
			add.setGroupKey(key);
		else
			add.setPbxuserKey(key);
		return add;
	}
	
	public void removeAllAddress(Long key, boolean isGroup, boolean isForced) throws DAOException, ValidateObjectException, DeleteDependenceException
	{
		Address sipID = isGroup ? addDAO.getSipIDByGroup(key) : addDAO.getSipIDByPbxuser(key);
		Long sipIDKey = sipID.getKey();
		
		//verifica a exist�ncia de forwards setados para o address que est� sendo removido.
		Long forwards = forwardDAO.countForwardsToAddress(sipIDKey);
		if(forwards != null && forwards > 0)
		{
			if(!isForced)
				throw new DeleteDependenceException("Cannot delete " + sipID.getAddress() + ": " + forwards + " forward(s) points to this address", Forward.class, forwards, sipID);
			else			
				disableAllForward(sipIDKey);
		}
		
		//verifica a exist�ncia de nightmode de grupos relacionados com o address que est� sendo removido.
		List<Group> grouplist = groupDAO.getGroupListByNightmodeAddress(sipIDKey);
		
		if(grouplist != null && grouplist.size() > 0)
		{
			forwards = Long.valueOf(grouplist.size());
			if(!isForced)
				throw new DeleteDependenceException("Cannot delete " + sipID.getAddress() + ": " + forwards + " nightmode group(s) points to this address", NightmodeGroup.class, forwards, sipID);
			else
				disableAllGroupNightModeForThisKey(sipIDKey);
		}

		//verifica a exist�ncia de forwards de IVR para o address que est� sendo removido
		List<IVR>ivrForwardsList = ivrDAO.getIVRListByForwards(sipIDKey);  //tveiga delete user forced from web services
		if(ivrForwardsList != null && ivrForwardsList.size() > 0)
		{
			
			forwards = Long.valueOf(ivrForwardsList.size());
			if(!isForced)			
				throw new DeleteDependenceException("Cannot delete " + sipID.getAddress() + ": " + forwards + " IVRs forward(s) points to this address", IVR.class, forwards, sipID);
			else
				disableAllIvrForwardForThisKey(sipIDKey);
		}
		
		Pbx pbx = pbxDAO.getPbxByDomain(sipID.getDomainKey());
		//verifica se o address que est� sendo removido � nightmode do pbx
		if(pbx.getNightmodeaddressKey()!= null && pbx.getNightmodeaddressKey().longValue() == sipIDKey.longValue())
		{
			 if(!isForced)
				 throw new DeleteDependenceException("Cannot delete " + sipID.getAddress() + ": The address is nightmode at pbx!", Pbx.class, sipID);
			 else
			 {
				 pbx.setNightmodeaddressKey(null);
				 pbx.setNightMode(Pbx.NIGHTMODE_OFF);
				 pbxDAO.save(pbx);
			 }
		}

		
		//verifica se o address que est� sendo removido � default operator do pbx		
		if(pbx.getDefaultaddressKey().longValue() == sipIDKey.longValue())
		{		
			throw new DeleteDependenceException("Cannot delete " + sipID.getAddress() + ": The address is default operator at pbx!", Pbx.class, sipID);			
		}

		//verifica se o address que est� sendo removido est� em liga��o no momento(possui active call)
		if(!isGroup)
		{
			forwards = (long) actDAO.getActiveCallListByPbxuser(sipID.getPbxuserKey()).size();
			if(forwards != null && forwards > 0 && !isForced)
				throw new DeleteDependenceException("Cannot delete " + sipID.getAddress() + ": " + forwards + " active call(s) points to this address", Activecall.class, forwards, sipID);
		}
		addDAO.remove(sipID);

		List<Address> addList = isGroup ? addDAO.getExtensionListByGroup(key) : addDAO.getExtensionListByPbxuser(key);
		for(Address add : addList)
			addDAO.remove(add);
		//tveiga corrigindo delete de user 
		addList = isGroup ? addDAO.getDIDListByGroup(key) : addDAO.getDIDListByPbxuser(key);
		for(Address add : addList)
		{
			add.setPbxuserKey(null);
			add.setGroupKey(null);
			addDAO.save(add);
		}
	}

//inicio --> dnakamashi - ezaghi - vmartinez - deleção forçada de grupo - version 3.0.5 RC6.4
	private void disableAllForward(Long addressKey) throws DAOException, ValidateObjectException
	{
		List<Forward> forwardList = forwardDAO.getForwardListByAddress(addressKey);
		for(Forward forward : forwardList)
		{
			forward.setAddressKey(null);
			forward.setTarget(null);
			forward.setConfigKey(null); // tveiga - corrigindo remover user forced qd este é um desvio 
			forward.setStatus(Forward.STATUS_OFF);
			forwardDAO.save(forward);
		}
		
	}
	
	private void disableAllGroupNightModeForThisKey(Long addressKey) throws DAOException, ValidateObjectException
	{
		List<Group> listGroup = groupDAO.getGroupListByNightmodeAddress(addressKey);
		
		for(Group group : listGroup )
		{
			group.setNightmodeaddressKey(null);
			group.setNightmodeStatus(Group.NIGHTMODE_OFF);
			group.setNightmodeaddress(null);
			groupDAO.save(group);
		}
	}
	
	private void disableAllIvrForwardForThisKey(Long addressKey) throws DAOException, ValidateObjectException
	{
		List<IVR> listIvr = ivrDAO.getIVRsByTarget(addressKey);
		
		for(IVR ivr : listIvr)
		{
			//ivr.setAddressList(null);
			if (ivr.getEotForwardAddressKey()==addressKey)
				ivr.setEotForwardAddressKey(null);
			if (ivr.getSalutationForwardAddressKey()==addressKey)
				ivr.setSalutationForwardAddressKey(null);
			
			ivr.setPbxuserKey(null); //tveiga - corrigindo delete user qd este tem desvio de ivr
			
			HashMap<String,IVROption> optList = ivr.getOptions();
			
			for (String key : optList.keySet())
			{
				IVROption opt = optList.get(key);
				if (opt.getForwardAddressKey().equals(addressKey))
				{
					ivrOptionDAO.remove(opt);			
				}
			}			
					
			ivrDAO.save(ivr);
		}
	}
	//fim --> dnakamashi - ezaghi - vmartinez - deleção forçada de grupo - version 3.0.5 RC6.4
	
	public String getNextAvalibleAddress(Long domainKey) throws DAOException, QuotaException
	{
		List<String> addList = addDAO.getExtensionsByDomain(domainKey);
		DialPlan extensionDialPlan = dialPlanDAO.getDialPlanByTypeAndDomain(DialPlan.TYPE_EXTENSION, domainKey);		
		int addressStart = extensionDialPlan.getStart();
		int addressEnd = extensionDialPlan.getEnd();
		
		while(addressStart <= addressEnd)
		{
			if(addList.contains(String.valueOf(addressStart)))
				++addressStart;
			else
				break;
		}
		
		if(addressStart > addressEnd)
			throw new QuotaException("Address Pool is full!", QuotaException.Type.ADDRESS);
		return String.valueOf(addressStart);
	}

	public Address getAddressWithPbxuserAndConfig(String address, String domain) throws DAOException
	{
		Address add = addDAO.getAddress(address, domain);
		return completeAddress(add);		
	}
	
	public Address getAddressWithPbxuserAndConfig(String address) throws DAOException
	{
		Address add = addDAO.getDIDAddress(address);	
		return completeAddress(add);
	}
	
	public Address getDID(String DID) throws DAOException
	{
		return addDAO.getDID(DID);
	}
	
	public Address completeAddress(Address add)throws DAOException
	{
		if(add != null && add.getPbxuserKey() != null)
		{
			Pbxuser pu = puDAO.getByKey(add.getPbxuserKey());				
			add.setPbxuser(pu);
			add.getPbxuser().setConfig(cDAO.getByKey(pu.getConfigKey()));
		}else if(add != null && add.getGroupKey()!= null)
		{
			Group group = groupDAO.getByKey(add.getGroupKey());
			List<Usergroup> usergroupList = groupDAO.getUsersInGroup(add.getGroupKey());
			group.setUsergroupList(usergroupList);
			add.setGroup(group);
		}

		return add;
	}

	//call control ivr
	public Address getAddress(String address, String domain) throws DAOException
	{
		return addDAO.getAddress(address, domain);
	}

	public Address getAddress(String address, Long domainKey) throws DAOException
	{
		return addDAO.getAddress(address, domainKey);
	}

	
	public AddressInfo getVoiceMailInfo(Long domainKey) throws DAOException
	{
		Address add = addDAO.getVoicemailAddress(domainKey);
		return new AddressInfo(add, add.getAddress(), null);
	}
	private boolean didIsTrunkLine(String address)throws DAOException{
	   List<Long> PbxKey = addDAO.getPbxKeyByAddress(address);
	       if(PbxKey.size() > 0)
		     return true;
	       else
	    	 return false;
	}
}
    

class AddressSort implements Comparator<Address>
{
	public int compare(Address add1, Address add2)
	{
		return add1.getAddress().compareTo(add2.getAddress());
	}
}