package br.com.voicetechnology.ng.ipx.rule.implement.voicemail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.voicetechnology.ipx.mail.formatter.VMMailFormatter;
import br.com.voicetechnology.ipx.mail.server.MailFormatter;
import br.com.voicetechnology.ipx.mail.server.MailServer;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.voicemail.InvalidLoginVoicemailException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.voicemail.VoicemailDisabledException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.QuotaException;
import br.com.voicetechnology.ng.ipx.commons.file.FileUtils;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PreferenceDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.UsergroupDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.FileinfoDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.GroupfileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserfileDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallType;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.prompt.PromptFile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Groupfile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Preference;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userfile;
import br.com.voicetechnology.ng.ipx.pojo.db.voicemail.MessageInfo;
import br.com.voicetechnology.ng.ipx.pojo.db.voicemail.VoiceMailInfo;
import br.com.voicetechnology.ng.ipx.rule.implement.FileinfoManager;
import br.com.voicetechnology.ng.ipx.rule.implement.prompt.PromptsFile;
import br.com.voicetechnology.ng.ipx.rule.implement.voicemail.DynaPrompts.Variable;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class VoicemailManager extends Manager
{
	private PbxuserDAO puDAO;
	private PbxDAO pbxDAO;
	private AddressDAO addDAO;
	private FileinfoDAO fileDAO;
	private GroupDAO gDAO;
	private UsergroupDAO ugDAO;
	private PreferenceDAO prefDAO;
	private UserfileDAO ufDAO;
	private GroupfileDAO gfDAO;
	private CalllogDAO clDAO;
	private DomainDAO dmDAO;
	private ConfigDAO confDAO;
	private TerminalDAO tDAO;
	private UserDAO uDAO;
	private FileinfoDAO fileinfoDAO;

	private PromptsFile prompts;
	private FileinfoManager fileManager;
	
	public VoicemailManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		puDAO = dao.getDAO(PbxuserDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		fileDAO = dao.getDAO(FileinfoDAO.class);
		gDAO = dao.getDAO(GroupDAO.class);
		ugDAO = dao.getDAO(UsergroupDAO.class);
		prefDAO = dao.getDAO(PreferenceDAO.class);
		ufDAO = dao.getDAO(UserfileDAO.class);
		gfDAO = dao.getDAO(GroupfileDAO.class);
		clDAO = dao.getDAO(CalllogDAO.class);
		dmDAO = dao.getDAO(DomainDAO.class);
		confDAO = dao.getDAO(ConfigDAO.class);
		tDAO = dao.getDAO(TerminalDAO.class);
		uDAO = dao.getDAO(UserDAO.class);
		fileinfoDAO = dao.getDAO(FileinfoDAO.class);
		fileManager = new FileinfoManager(logger);
	}

	public List<Pbxuser> getVoicemailListByFarmIP(String farmIP) throws DAOException
	{
		return puDAO.getVoicemailListByFarmIP(farmIP);
	}

	public List<Pbxuser> getVoicemailListByDomain(String domain) throws DAOException
	{
		return puDAO.getVoicemailListByDomain(domain);
	}

	public VoiceMailInfo getVoiceMailInfo(SipAddressParser sipFrom, SipAddressParser sipTo, Long domainKey) throws DAOException, InvalidLoginVoicemailException
	{
		//TODO gambi salvar calllog message
        String[] tmp = sipTo.getExtension().split("##");
        String callID = null;
        String calledAddress = null;
        
        if(tmp.length == 2)
        {
        	callID = tmp[0];
        	sipTo.setExtension(tmp[1]);
        } else if(tmp.length == 3)
        {
        	callID = tmp[0];
        	sipTo.setExtension(tmp[1]);
        	calledAddress = tmp[2];
        }

		boolean isToVoiceMail = isToVoiceMail(sipTo.getDomain(), sipTo);
		boolean isDIDCall = isDIDCall(sipFrom);

		SipAddressParser correctAddresTo = verifyCorrectSipAddress(sipTo, domainKey);
		SipAddressParser originalAddressTo = null;
		if(!correctAddresTo.getExtension().equals(sipTo.getExtension()))		
		{
			originalAddressTo = new SipAddressParser(sipTo.getExtension(), sipTo.getDomain());
			sipTo = correctAddresTo;
		}
		
		// jfarah - 3.1.4 bugid 7202
		// voicemail do nightmode user: sipTo eh o destino discado 
		if(calledAddress != null)
		{
			if(originalAddressTo == null)
				originalAddressTo = new SipAddressParser(calledAddress, sipTo.getDomain());
			else
				originalAddressTo.setExtension(calledAddress);
		}

        Pbxuser pu = null;
        if(!isDIDCall || callID != null)
	        if(isToVoiceMail)
	        	pu = getPbxuserByAddress(sipFrom, domainKey);
	        else	        
	        	pu = getPbxuserByAddress(sipTo, domainKey);
        
        VoiceMailInfo vmInfo;
        if(isDIDCall && callID == null)
        	vmInfo = makeVoiceMailInfoDIDCall(sipFrom, sipTo, isToVoiceMail, domainKey, originalAddressTo);
        else if(pu != null)
        	vmInfo = makeVoiceMailInfoByPbxuser(sipFrom, sipTo, isToVoiceMail, pu, domainKey, originalAddressTo);
        else 
        	vmInfo = makeVoiceMailInfoByGroup(sipFrom, sipTo, isToVoiceMail, domainKey, originalAddressTo);
        
        vmInfo.setCallID(callID);
        vmInfo.setHasMoreSpace(hasMoreSpace(domainKey));
        return vmInfo;
	}

	public boolean isToVoiceMail(String domain, SipAddressParser sipTo) throws DAOException
	{
		List<Address> addList = addDAO.getVoicemailAddressList(domain);
		for(Address add : addList)
			if(sipTo.getExtension().equals(add.getAddress()))
				return true;
		return false;
	}
	
	public boolean isDIDCall(SipAddressParser sipFrom) throws DAOException
	{
		return sipFrom.getExtension().matches("\\d{6,}");
	}
	
	public boolean isIdentified(SipAddressParser sipFrom) throws DAOException
	{
		Address add = addDAO.getAddress(sipFrom.getExtension(), sipFrom.getDomain());
		return add != null;
	}
	
	public boolean hasMoreSpace(Long domainKey) throws DAOException
	{
		Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		return hasMoreSpace(pbx, domainKey);
	}
	
	public boolean hasMoreSpace(Pbx pbx, Long domainKey) throws DAOException
	{
		Float used = fileDAO.countUsedQuota(domainKey).floatValue();
		boolean hasMoreSpace = used.floatValue() < pbx.getQuota().floatValue();
		if(!hasMoreSpace)
		{
			Domain domain = dmDAO.getByKey(domainKey);		
			logger.error("The Voicemail Quota ("+pbx.getQuota().floatValue()+"MB) from "+domain.getDomain()+" was exceeded");
		}
		return hasMoreSpace;
	}
	
	private SipAddressParser verifyCorrectSipAddress(SipAddressParser originalAddress, Long domainKey)throws DAOException
	{
		Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		if(pbx.getOperator().equals(originalAddress.getExtension()))
		{
			Address defaultAdd = addDAO.getByKey(pbx.getDefaultaddressKey());
			if(defaultAdd != null)
				originalAddress.setExtension(defaultAdd.getAddress());
		}
		
		Address address = addDAO.getAddress(originalAddress.getExtension(), originalAddress.getDomain());
		
		if(address != null)
		{
			if(address.getGroupKey() != null)
				return originalAddress;
			else if(address.getPbxuserKey() == null)
			{
				address = addDAO.getByKey(pbx.getDefaultaddressKey());
	
				if(address != null)
					return new SipAddressParser(address.getAddress(), originalAddress.getDomain());
			}
		}
		return originalAddress;
	}
	private Pbxuser getPbxuserByAddress(SipAddressParser sipAddress, Long domainKey) throws DAOException, InvalidLoginVoicemailException
	{
		Address address = addDAO.getAddress(sipAddress.getExtension(), domainKey);
		
		if(address.getPbxKey() != null)
			address = addDAO.getByKey(pbxDAO.getByKey(address.getPbxKey()).getDefaultaddressKey());
				
		Pbxuser pu = puDAO.getPbxuserByAddressKey(address.getKey());		

		if(pu == null || pu.getUser() == null)
			return null;
		else if(pu.getUser().getAgentUser().intValue() == User.TYPE_PBXUSER || 
				pu.getUser().getAgentUser().intValue() == User.TYPE_IVR ||
				pu.getUser().getAgentUser().intValue() == User.TYPE_SIPTRUNK)
			return pu;
		if(pu.getUser().getAgentUser().intValue() == User.TYPE_TERMINAL)
		{
			Pbxuser puAssociated = puDAO.getPbxuserByTerminal(sipAddress.getExtension(), sipAddress.getDomain());
			if(puAssociated == null)
				throw new InvalidLoginVoicemailException("Invalid terminal. To access voicemail by terminal, it must be associated by a Pbxuser!!!");
			return puAssociated;	
		}
		return null;
	}
	
	private VoiceMailInfo makeVoiceMailInfoDIDCall(SipAddressParser sipFrom, SipAddressParser sipTo, boolean isToVoiceMail, Long domainKey, SipAddressParser originalAddressTo) throws DAOException
	{
        Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
        String locale = pbx.getPbxPreferences().getVmLocale();
        String extension = isToVoiceMail ? sipFrom.getExtension() : sipTo.getExtension();
        Address add = addDAO.getAddress(extension, domainKey);
        String[] salutation = null;
        if(add != null)
        {
        	if(add.getGroupKey() != null)
        	{
        		Group g = gDAO.getGroupFull(add.getGroupKey());
        		salutation = getSalutation(g.getKey(), locale, sipTo, true, null, originalAddressTo, g.getNightmodeStatus());
        	}
        	else
        	{
        		Pbxuser pu = puDAO.getByKey(add.getPbxuserKey());
        		if(pu != null)
        			salutation = getSalutation(pu.getUserKey(), locale, sipTo, false, pu.getKey(), originalAddressTo,Groupfile.DEFAULT_FILE);
        	}
        }
        VoiceMailInfo vmInfo = new VoiceMailInfo(sipFrom, sipTo, locale, domainKey, salutation); 
        Domain domain = dmDAO.getByKey(vmInfo.getDomainKey());
        vmInfo.setDomain(domain.getDomain());
		return vmInfo;
	}
	
	private VoiceMailInfo makeVoiceMailInfoByPbxuser(SipAddressParser sipFrom, SipAddressParser sipTo, boolean isToVoiceMail, Pbxuser pu, Long domainKey, SipAddressParser originalAddressTo) throws DAOException
	{
		//jluchetta - Início- correção do problema para acessar voicemail de grupo caso o usuário não tenha voicemail, porm pertença à um grupo que tenha
		 Long countGroupAdmin = ugDAO.getCountAdminGroupWithVoicemailByPbxuser(pu.getKey());
		 boolean isGroupMember = countGroupAdmin.longValue() > 0L;
		 boolean isUserVoicemailEnable = isVoicemailEnabled(pu);
		 
		 //jluchetta - Fim- correção do problema para acessar voicemail de grupo caso o usuário não tenha voicemail, porm pertença à um grupo que tenha
		if(!isUserVoicemailEnable && !isGroupMember)
            throw new VoicemailDisabledException();

		VoiceMailInfo vmInfo = null;
        Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		boolean isIdentified = isIdentified(sipFrom);
        int maxMessageTime = pbx.getMaxMessageTime();
        boolean hasMoreSpace = hasMoreSpace(pbx, domainKey);

		
		Preference pref = prefDAO.getPreferenceByUser(pu.getUserKey());
		String locale = pref.getLocale();
		
		String[] salutation = getSalutation(pu.getUserKey(), locale, sipTo, false, pu.getKey(), originalAddressTo, Groupfile.DEFAULT_FILE);
		
		//jluchetta- mudança para passar um parâmetro para dizer se o voicemail é de pbxuser
        vmInfo = new VoiceMailInfo(pu, sipFrom, sipTo, isIdentified, isToVoiceMail, maxMessageTime, hasMoreSpace, domainKey, isGroupMember, isUserVoicemailEnable, getVoiceMailPrompts().getPromptPath(locale, PromptFile.DEFAULT_SALUTATION));
        vmInfo.setLocale(locale);
        vmInfo.setSalutation(salutation);
        Domain domain = dmDAO.getByKey(vmInfo.getDomainKey());
        vmInfo.setDomain(domain.getDomain());
        
		return vmInfo;
	}

	private VoiceMailInfo makeVoiceMailInfoByGroup(SipAddressParser sipFrom, SipAddressParser sipTo, boolean isToVoiceMail, Long domainKey, SipAddressParser originalAddressTo) throws DAOException, InvalidLoginVoicemailException
	{
		VoiceMailInfo vmInfo = null;
        Pbx pbx = pbxDAO.getPbxByDomain(domainKey);
		boolean isIdentified = isIdentified(sipFrom);
        int maxMessageTime = pbx.getMaxMessageTime();
        boolean hasMoreSpace = hasMoreSpace(pbx, domainKey);

        Group g = gDAO.getGroupByAddressAndDomain(sipTo.getExtension(), sipTo.getDomain());
        if(g == null)
        	throw new InvalidLoginVoicemailException("Invalid group, please check extension typed!!!");
        else if(isToVoiceMail)
    	{
        	Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(sipFrom.getExtension(), sipFrom.getDomain());
        	if(pu.getUser().getAgentUser().intValue() == User.TYPE_TERMINAL)
        		pu = tDAO.getAssociatedPbxuserByTerminalPbxuserKey(pu.getKey());
        	if(pu == null)
        		throw new InvalidLoginVoicemailException("Invalid login group, please check pbxuser or terminal roles!!");
        	boolean isGroupAdmin = ugDAO.verifyIsGroupAdmin(pu.getUser().getUsername(), sipTo.getExtension());
        	if(!isGroupAdmin)
        		throw new InvalidLoginVoicemailException("Invalid login group, you aren't admin for this group!!!");
    	}
		String locale = g.getLocale();
		
		String[] salutation = getSalutation(g.getKey(), locale, sipTo, true, null, originalAddressTo, g.getNightmodeStatus());
        
        vmInfo = new VoiceMailInfo(g, sipFrom, sipTo, isIdentified, isToVoiceMail, maxMessageTime, hasMoreSpace, domainKey, getVoiceMailPrompts().getPromptPath(locale, PromptFile.DEFAULT_SALUTATION));
        vmInfo.setLocale(locale);
        vmInfo.setSalutation(salutation);
        Domain domain = dmDAO.getByKey(vmInfo.getDomainKey());
        vmInfo.setDomain(domain.getDomain());
		return vmInfo;
	}

	private String[] getSalutation(Long key, String locale, SipAddressParser sipTo, boolean isGroup, Long puKey, SipAddressParser originalAddressTo, Integer nightmodeStatus) throws DAOException
	{
		String[] salutation = null;
		Fileinfo fileSalutation = isGroup ? fileDAO.getGroupVoiceMailSalutationPath(key, nightmodeStatus.equals(Group.NIGHTMODE_OFF) ? Groupfile.DEFAULT_FILE : Groupfile.NIGHTMODE_FILE): fileDAO.getUserVoiceMailSalutationPath(key);
        if(fileSalutation != null)
        	salutation = new String[]{fileSalutation.getAbsoluteName()};
        else
        {    
    		String target = sipTo.getExtension();
  			List<Address> addList = isGroup ? addDAO.getExtensionListByGroup(key) : addDAO.getExtensionListByPbxuser(puKey);
			if(addList.size() > 0)
				target = addList.get(0).getAddress();

        	salutation = getVoiceMailPrompts().getPromptPath(locale, PromptFile.DEFAULT_SALUTATION, new DynaPrompts(Variable.TO, target, DynaPrompts.Type.SPELLED));
        }
		return salutation;
	}
	
	private boolean isVoicemailEnabled(Pbxuser pu) throws DAOException
    {
        Config conf = confDAO.getByKey(pu.getConfigKey());
        //jluchetta - mudança para retorna true qd o voicemail está ativo, antes estava ao contrário
        return conf != null && conf.getDisableVoicemail().intValue() == Config.VOICEMAIL_ON && conf.getAllowedVoiceMail() == Config.ALLOWED_VOICEMAIL;
    }
	
	public String[] getPrompt(String locale, PromptFile vmPrompt)
	{
		return getVoiceMailPrompts().getPromptPath(locale, vmPrompt);
	}

	private PromptsFile getVoiceMailPrompts()
	{
		if(this.prompts == null)
			prompts = new PromptsFile();
		return prompts;
	}
	
	public VoiceMailInfo loginVoicemail(SipAddressParser from, SipAddressParser sipTo, String pin) throws InvalidLoginVoicemailException, DAOException, QuotaException
	{
    	Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(from.getExtension(), from.getDomain());
    	if(pu == null)
    		throw new InvalidLoginVoicemailException("Invalid extension@domain, please check values!");
   		if(pu.getUser().getAgentUser().intValue() == User.TYPE_TERMINAL)
   			pu = tDAO.getAssociatedPbxuserByTerminalPbxuserKey(pu.getKey());
    	if(!pu.getPin().equals(pin))
   			throw new InvalidLoginVoicemailException("Invalid extension@domain, please check values!");

    	if(pu.getUser() == null)
    		pu.setUser(uDAO.getByKey(pu.getUserKey()));
    	return makeVoiceMailInfoByPbxuser(from, sipTo, true, pu, pu.getUser().getDomainKey(), null);
	}

	public VoiceMailInfo loginGroupVoiceMail(SipAddressParser from, SipAddressParser to, Long domainKey) throws InvalidLoginVoicemailException, DAOException, QuotaException
	{
		Address address = addDAO.getAddress(to.getExtension(), domainKey);
		if(address == null)
			throw new InvalidLoginVoicemailException("Invalid extension@domain, please check values!");
		Long groupKey = address.getGroupKey();
		if(groupKey == null)
			throw new InvalidLoginVoicemailException("Invalid extension@domain, please check values!");

		//boolean true pois quando o fluxo acessa este m�todo o usu�rio est� realmente ligando para voicemail de um grupo.
		return makeVoiceMailInfoByGroup(from, to, true, domainKey, null);
	}
	
	public void checkMessages(VoiceMailInfo vmInfo) throws DAOException
	{
    	vmInfo.clearNewMessageInfo();
		vmInfo.clearOldMessageInfo();
		if(vmInfo.isPbxuser())
    	{
	    	vmInfo.addMessageInfo(getNewMessages(vmInfo));
	    	vmInfo.addMessageInfo(getOldMessages(vmInfo));
    	}else
    	{
    		vmInfo.addMessageInfo(getNewMessagesGroup(vmInfo));
    		vmInfo.addMessageInfo(getOldMessagesGroup(vmInfo));
    	}    	
    	addPrompts(vmInfo);
	}

	private void addPrompts(VoiceMailInfo vmInfo)
	{
		if(vmInfo.hasNewMessages())
		{
			DynaPrompts dynamic = new DynaPrompts(Variable.MSG_NUM, String.valueOf(vmInfo.getNewMessages().size()), DynaPrompts.Type.NUMBER);
			if(vmInfo.getNewMessages().size() == 1)
				vmInfo.setPrompts(getVoiceMailPrompts().getPromptPath(vmInfo.getLocale(), PromptFile.NEW_MSG, dynamic));
			else
				vmInfo.setPrompts(getVoiceMailPrompts().getPromptPath(vmInfo.getLocale(), PromptFile.NEW_MSGS, dynamic));
		}else
			vmInfo.setPrompts(getVoiceMailPrompts().getPromptPath(vmInfo.getLocale(), PromptFile.NO_MSG));
	}
	
	private List<MessageInfo> getNewMessages(VoiceMailInfo vmInfo) throws DAOException
	{
		List<Userfile> newList = ufDAO.getNewMessagesByUser(vmInfo.getKey());
		return createMessageInfoList(vmInfo, newList);
	}
	
	private List<MessageInfo> getOldMessages(VoiceMailInfo vmInfo) throws DAOException
	{
		List<Userfile> newList = ufDAO.getOldMessagesByUser(vmInfo.getKey());
		return createMessageInfoList(vmInfo, newList);
	}

	private List<MessageInfo> createMessageInfoList(VoiceMailInfo vmInfo, List<Userfile> ufList)
	{
		List<MessageInfo> messageList = new ArrayList<MessageInfo>();
		for(Userfile uf : ufList)
		{
			MessageInfo info;
			if(uf.getCalllog() != null)
			{
				if(uf.getIsRead().intValue() == Userfile.MSG_NOT_READ)
				{
					SipAddressParser sipAddress = new SipAddressParser(uf.getCalllog().getAddress());
					info = new MessageInfo(uf.getFileinfoKey(), uf.getFileinfo().getAbsoluteName(), sipAddress.getExtension(), uf.getCalllog().getFromdate(), false);
				} else
				{
					SipAddressParser sipAddress = new SipAddressParser(uf.getCalllog().getAddress());
					info = new MessageInfo(uf.getFileinfoKey(), uf.getFileinfo().getAbsoluteName(), uf.getLastAccess(), sipAddress.getExtension(), uf.getCalllog().getFromdate(), true);
				}
				info.setAniPrompts(getVoiceMailPrompts().getPromptPath(vmInfo.getLocale(), new DynaPrompts(info.getAni(), DynaPrompts.Type.SPELLED)));
				info.setTimePrompts(getVoiceMailPrompts().getPromptPath(vmInfo.getLocale(), new DynaPrompts(info.getTime(), DynaPrompts.Type.DATE)));
			} else
				info = new MessageInfo(uf.getFileinfoKey(), uf.getFileinfo().getAbsoluteName(), uf.getLastAccess(), uf.getIsRead() == Userfile.MSG_READ ? true : false);
			messageList.add(info);
		}
		return messageList;
	}
	
	private List<MessageInfo> getNewMessagesGroup(VoiceMailInfo vmInfo) throws DAOException
	{
		List<Groupfile> newList = gfDAO.getNewMessages(vmInfo.getKey());
		return createMessageInfoListGroup(vmInfo, newList);
	}
	
	private List<MessageInfo> getOldMessagesGroup(VoiceMailInfo vmInfo) throws DAOException
	{
		List<Groupfile> newList = gfDAO.getOldMessages(vmInfo.getKey());
		return createMessageInfoListGroup(vmInfo, newList);
	}
	
	private List<MessageInfo> createMessageInfoListGroup(VoiceMailInfo vmInfo, List<Groupfile> ufList)
	{
		List<MessageInfo> messageList = new ArrayList<MessageInfo>();
		for(Groupfile gf : ufList)
		{
			boolean isRead = gf.getIsRead().intValue() == Userfile.MSG_READ;
			MessageInfo info = new MessageInfo(gf.getFileinfoKey(), gf.getFileinfo().getAbsoluteName(), gf.getCallerID(), gf.getFileinfo().getCreateTime(), isRead);
			info.setAniPrompts(getVoiceMailPrompts().getPromptPath(vmInfo.getLocale(), new DynaPrompts(info.getAni(), DynaPrompts.Type.SPELLED)));
			info.setTimePrompts(getVoiceMailPrompts().getPromptPath(vmInfo.getLocale(), new DynaPrompts(info.getTime(), DynaPrompts.Type.DATE)));
			messageList.add(info);
		}
		return messageList;
	}
	
	private void saveMessageInCallLog(VoiceMailInfo vmInfo, Userfile uf) throws DAOException, ValidateObjectException
	{
		Calllog cl = clDAO.getCallLogByCallIDAndUser(vmInfo.getCallID(), vmInfo.getKey(), CallType.CALL_RECEIVED);
		uf.setCalllogKey(cl.getKey());
		ufDAO.save(uf);
	}
	
	private void saveMessageInCallLog(VoiceMailInfo vmInfo, Long pbxuserKey, Userfile uf) throws DAOException, ValidateObjectException
	{
		if(logger.isDebugEnabled())
			logger.debug(new StringBuilder("Saving message to pbxuserkey: ").append(pbxuserKey).append(", callID: ").append(vmInfo.getCallID()).toString());
		
		Calllog cl = clDAO.getCallLogByCallIDAndPbxuser(vmInfo.getCallID(), pbxuserKey, CallType.CALL_RECEIVED);
		
		if(cl != null)
		{
			uf.setCalllogKey(cl.getKey());
			ufDAO.save(uf);
			
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Message saved to pbxuserkey: ").append(pbxuserKey).append(", callID: ").append(vmInfo.getCallID()).toString());
		} else
		{
			if(logger.isDebugEnabled())
				logger.debug(new StringBuilder("Couldn't save message, calllog of received call not found to pbxuserkey: ").append(pbxuserKey).append(", callID: ").append(vmInfo.getCallID()).toString());
		}
			
	}

	public void markMessageAsReaded(Long key, Long fileinfoKey) throws DAOException, ValidateObjectException
	{
		Userfile uf = ufDAO.getUserfileByUserKeyAndFileinfoKey(key, fileinfoKey);
		if(uf != null)
		{
			uf.setIsRead(Userfile.MSG_READ);
			uf.setLastAccess(Calendar.getInstance());
			ufDAO.save(uf);
		}else
		{
			Groupfile gf = gfDAO.getGroupfileByFileinfoKey(fileinfoKey);//dnakamashi - 3.0.2 RC12 patch4- corrigido a query
			gf.setIsRead(Userfile.MSG_READ);
			gf.setLastAccess(Calendar.getInstance());
			gfDAO.save(gf);
		}
	}

	public void saveVoiceMailMessage(VoiceMailInfo vmInfo, Fileinfo fileinfo) throws DAOException, ValidateObjectException, IOException
	{
		boolean fileSaved = fileManager.saveVoiceMailMessage(fileinfo);
		if(fileSaved && fileinfo.getActive() == Fileinfo.DEFINE_ACTIVE)
			if(vmInfo.isGroup())
				saveGroupMessage(vmInfo, fileinfo);
			else
				savePbxuserMessage(vmInfo, fileinfo);
	}
	
	private void saveGroupMessage(VoiceMailInfo vmInfo, Fileinfo fileinfo) throws DAOException, ValidateObjectException
	{
		Group g = gDAO.getByKey(vmInfo.getKey());
		if(g.getVoicemailNotifyMode().intValue() == Group.NOTIFY_ADMIN)
		{
			Groupfile gf = new Groupfile();
			gf.setFileinfoKey(fileinfo.getKey());
			gf.setGroupKey(vmInfo.getKey());	
			gf.setIsRead(Userfile.MSG_NOT_READ);
			gf.setLastAccess(Calendar.getInstance());
			gf.setUseType(Groupfile.DEFAULT_FILE);
			gf.setCallerID(vmInfo.getSipFrom().getExtension());
			
			saveVoiceMailGroupfile(vmInfo, fileinfo, gf);		
		
		} else
		{
			List<Usergroup> ugList = ugDAO.getUsergroupListByGroup(g.getKey());
			for(Usergroup ug : ugList)
			{
				Pbxuser pbxuser = puDAO.getByKey(ug.getPbxuserKey());
				Userfile userfile = saveUsefile(pbxuser.getUserKey(), fileinfo.getKey());
				saveMessageInCallLog(vmInfo, pbxuser.getKey(), userfile);
			}
		}
		
	}
	
	//inicio --> dnakamashi - bug # 5191 - 3.0.2 RC12 patch 4
	//relaciona o GroupFile com o CallLog de VoiceMail
	public void saveVoiceMailGroupfile(VoiceMailInfo vmInfo, Fileinfo fileinfo, Groupfile gf) throws DAOException, ValidateObjectException
	{		
		Calllog cl = clDAO.getCallLogByCallIDAndVoicemail(vmInfo.getCallID(), vmInfo.getDomainKey());
		if(cl != null)
		{
			gf.setCalllogKey(cl.getKey());
			gfDAO.save(gf);
		}	
	}
	//fim --> dnakamashi - bug # 5191 - 3.0.2 RC12 patch 4
	
	private void savePbxuserMessage(VoiceMailInfo vmInfo, Fileinfo fileinfo) throws DAOException, ValidateObjectException
	{
		Userfile uf = saveUsefile(vmInfo.getKey(), fileinfo.getKey());
		saveMessageInCallLog(vmInfo, uf);
	}
	
	private Userfile saveUsefile(Long uKey, Long fiKey) throws DAOException, ValidateObjectException
	{
		Userfile uf = new Userfile();
		uf.setFileinfoKey(fiKey);
		uf.setLastAccess(Calendar.getInstance());
		uf.setUserKey(uKey);
		uf.setIsRead(Userfile.MSG_NOT_READ);
		ufDAO.save(uf);
		return uf;
	}

	public void deleteMessage(Long key, Long fileinfoKey) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		Userfile uf = ufDAO.getUserfileByUserKeyAndFileinfoKey(key, fileinfoKey);
		boolean deleteFile = false;
		if(uf != null)
		{
			deleteFile = ufDAO.howMuchUserfileByFileKey(fileinfoKey).longValue() == 1L;
			ufDAO.remove(uf);
		}else
		{
			Groupfile gf = gfDAO.getGroupfileByGroupKeyAndFileinfoKey(key, fileinfoKey);
			deleteFile = gfDAO.howMuchGroupfileByFileKey(fileinfoKey).longValue() == 1L;
			if(gf != null)
			{
				gfDAO.remove(gf);
			}				
		}
		if(deleteFile)
			fileManager.deleteFileinfo(fileinfoKey);
	}

	public void setDefaultSalutation(VoiceMailInfo vmInfo) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		if(vmInfo.isGroup())		
			deleteSalutationGroup(vmInfo.getKey(), Groupfile.DEFAULT_FILE);
		else
			deleteSalutationPbxuser(vmInfo.getKey());
		vmInfo.setSalutation(getVoiceMailPrompts().getPromptPath(vmInfo.getLocale(), PromptFile.DEFAULT_SALUTATION, new DynaPrompts(Variable.TO, vmInfo.getName(), DynaPrompts.Type.SPELLED)));
	}
	
	private void deleteSalutationPbxuser(Long uKey) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		Userfile uf = ufDAO.getSalutationByUserKey(uKey);
		if(uf != null)
		{
			ufDAO.remove(uf);
			fileManager.deleteFileinfo(uf.getFileinfoKey());
		}
	}
	
	public void deleteSalutationGroup(Long gKey, Integer useType) throws DAOException, DeleteDependenceException, ValidateObjectException
	{
		Groupfile gf = gfDAO.getSalutationByGroupKey(gKey, useType);
		if(gf != null)	
			gfDAO.remove(gf);		
			
	}

	public void cancelSalutation(Long fileinfoKey) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		fileManager.deleteFileinfo(fileinfoKey);
	}

	public void saveVoicemailSalutation(VoiceMailInfo vmInfo, Fileinfo newSalutation, Integer useType) throws DAOException, ValidateObjectException, DeleteDependenceException, QuotaException, IOException
	{
		/*jluchetta -  Versão 3.0.5 - Correção apara garvar arquivo de saudação de record box
		*Agora os arquivso já são previamente criados na apsta do domínio correto e depois ele somente é atualizado com as informacoes e tamanho corretos
		* Isso evita a copia da pasta temporária para a definitiva
		*/
		boolean sucess = fileManager.saveRecordFile(newSalutation);
		
		if(sucess)
		{
			if(vmInfo.isGroup())
				saveGroupfile(vmInfo, newSalutation, useType);
			else
				saveUserfile(vmInfo, newSalutation);
			vmInfo.setSalutation(new String[]{newSalutation.getAbsoluteName()});
		}
		/**********************************************/
	}
	
	public void saveGroupSalutation(Long groupKey, Long fileInfoKey, Integer useType) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		Fileinfo salutation = fileinfoDAO.getByKey(fileInfoKey);
		saveGroupfile(groupKey,salutation, useType);
	}
	
	private void saveGroupfile(VoiceMailInfo vmInfo, Fileinfo newSalutation, Integer useType) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		saveGroupfile(vmInfo.getKey(), newSalutation, useType);
	}
	
	private void saveGroupfile(Long groupKey, Fileinfo newSalutation, Integer useType) throws DAOException, DeleteDependenceException, IOException, ValidateObjectException
	{
		Groupfile gf = gfDAO.getSalutationByGroupKey(groupKey, useType);
		if(gf != null)
		{
			//Long fileKey = gf.getFileinfoKey();
			gf.setFileinfoKey(newSalutation.getKey());
			gfDAO.save(gf);
			//fileManager.deleteFileinfo(fileKey);
		} else
		{
			gf = new Groupfile();
			gf.setFileinfoKey(newSalutation.getKey());
			gf.setGroupKey(groupKey);
			gf.setUseType(useType);
			gf.setLastAccess(Calendar.getInstance());
			gf.setCallerID(Groupfile.DEFAULT_CALLERID);
			gf.setIsRead(Groupfile.MSG_NOT_READ);
			gfDAO.save(gf);
		}
	}

	private void saveUserfile(VoiceMailInfo vmInfo, Fileinfo newSalutation) throws DAOException, ValidateObjectException, DeleteDependenceException, IOException
	{
		Userfile uf = ufDAO.getSalutationByUserKey(vmInfo.getKey());
		if(uf != null)
		{
			Long fileKey = uf.getFileinfoKey();
			uf.setFileinfoKey(newSalutation.getKey());
			ufDAO.save(uf);
			fileManager.deleteFileinfo(fileKey);
		} else
			saveUsefile(vmInfo.getKey(), newSalutation.getKey());
	}
	
	public Long sendVoiceMailNotifications() throws DAOException
	{
		
		long numberNotificationsSent = 0l;
		
		logger.debug("sendVoiceMailNotifications - Find user that want email without attach files");
		List<Userfile> files = ufDAO.getEmailNotificationList(false);
		logger.debug("sendVoiceMailNotifications - Found " + files.size() + " users, start notifications!!!");
		numberNotificationsSent += files.size();
		sendEmailNotification(files, false);
		logger.debug("sendVoiceMailNotifications - sent notifications success, to users that want email without attach files");
		files.clear();
		
		logger.debug("sendVoiceMailNotifications - Find user that want email with attach files");
		files = ufDAO.getEmailNotificationList(true);
		logger.debug("sendVoiceMailNotifications - Found " + files.size() + " users, start notifications!!!");
		sendEmailNotification(files, true);
		logger.debug("sendVoiceMailNotifications - sent notifications success, to users that want email with attach files");
		numberNotificationsSent += files.size();
		
		logger.debug("sendVoiceMailNotifications - Find groups in admin only mode that want email without attach files");
		List<Groupfile> gfList = gfDAO.getEmailNotificationList(false);
		logger.debug("sendVoiceMailNotifications - Found " + gfList.size() + " groups, start notifications!!!");
		numberNotificationsSent += gfList.size();
		sendEmailNotificationGroup(gfList, false);
		logger.debug("sendVoiceMailNotifications - sent notifications success, to users that want email with attach files");
		gfList.clear();
		
		logger.debug("sendVoiceMailNotifications - Find groups in admin only mode that want email without attach files");
		gfList = gfDAO.getEmailNotificationList(true);
		logger.debug("sendVoiceMailNotifications - Found " + gfList.size() + " groups, start notifications!!!");
		sendEmailNotificationGroup(gfList, true);
		logger.debug("sendVoiceMailNotifications - sent notifications success, to users that want email with attach files");
		numberNotificationsSent += gfList.size();

		return numberNotificationsSent;
	}
	
	private void sendEmailNotificationGroup(List<Groupfile> gfList, boolean attachFile) throws DAOException
	{
		Groupfile gf;
		for(int i = 0; i < gfList.size();)
		{
			gf = gfList.get(i);
			Group g = gf.getGroup();
			
			Long unread;
			Long read;
			try
			{
				unread = gfDAO.countNewMessagesByGroup(g.getKey(), false);
				read = gfDAO.countOldMessagesByGroup(g.getKey(), false);
			}catch (Exception e)
			{
				i++;
				logger.error("Error sending email to group: " + g.getName() + " when counting message number.", e);
				continue;
			}
			String locale = g.getLocale();
			String name = g.getName();
			
			Calendar notifyDate = Calendar.getInstance();
			Long key = gf.getGroupKey();
			String domain = dmDAO.getDomainByPbx(g.getPbxKey()).getDomain();
			List<User> uList = gDAO.getAdminUsersInGroup(gf.getGroupKey());
			do
			{
				try
				{
					gf = gfList.get(i);
					String file = attachFile ? makeFilePath(gf.getFileinfo()) : null;
					for(User user : uList)
					{						
						MailFormatter formatter = new VMMailFormatter();						
						formatter.addParam("name", name);
						formatter.addParam("new", unread.toString());
						formatter.addParam("old", read.toString());
						formatter.addParam("locale", locale);
						formatter.addParam("downloadURL", makeDownloadURL(makeLogin(user, domain), gf.getFileinfoKey()));
						formatter.addParam("file", file);
						formatter.addParam("email", user.getEmail());
						formatter.addParam("domain", domain);
						
						if(user.getEmail() != null)
							MailServer.getInstance().send(formatter);
					}
					gf.setNotifyDate(notifyDate);
					gfDAO.save(gf);
				}catch(Exception e)
				{
					logger.error("Error sending email to group:" + g.getName(), e);
				}

			}while(++i < gfList.size() && key.equals(gfList.get(i).getGroupKey()));
		}
	}
	
	private void sendEmailNotification(List<Userfile> files, boolean attachFile)
	{
		Userfile uf;
		for (int i = 0; i < files.size();)
		{
			uf = files.get(i);
			User user = uf.getUser();
			
			Long unread;
			Long read;
			try
			{
				unread = ufDAO.countNewMessagesByUser(user.getKey(), false);
				read = ufDAO.countOldMessagesByUser(user.getKey(), false);
			}catch (Exception e)
			{
				i++;
				logger.error("Error sending email to:" + user.getEmail() + " when counting message number.", e);
				continue;
			}
			String locale = user.getPreference().getLocale();
			String name = user.getName() != null && user.getName().length() > 0 ? user.getName() : user.getKanjiName();

			Calendar notifyDate = Calendar.getInstance();
			Long key = uf.getUserKey();

			do
			{
				try
				{
					uf = files.get(i);
					String file = attachFile ? makeFilePath(uf.getFileinfo()) : null;
					String downloadURL = makeDownloadURL(makeLogin(user), uf.getFileinfoKey());
					
					MailFormatter formatter = new VMMailFormatter();						
					formatter.addParam("name", name);
					formatter.addParam("new", unread.toString());
					formatter.addParam("old", read.toString());
					formatter.addParam("locale", locale);
					formatter.addParam("downloadURL", downloadURL);
					formatter.addParam("file", file);
					formatter.addParam("email", user.getEmail());
					formatter.addParam("domain", user.getDomain().getDomain());
					
					if(user.getEmail() != null)
						MailServer.getInstance().send(formatter);
					
					uf.setNotifyDate(notifyDate);
					ufDAO.save(uf);
				} catch(Exception e)
				{
					logger.error("Error sending email to:" + user.getEmail(), e);
				}
			} while(++i < files.size() && key.equals(files.get(i).getUserKey()));
		}
	}
	
	private String makeFilePath(Fileinfo file)
	{
		return FileUtils.getRootPath() + file.getAbsoluteName(); 
	}
	
	private String makeLogin(User u, String domain)
	{
		return u.getUsername() + "@" + domain;
	}
	
	private String makeLogin(User u)
	{
		return u.getUsername() + "@" + u.getDomain().getDomain();
	}
	
	private String makeDownloadURL(String login, Long fileKey)
	{
		return IPXProperties.getProperty(IPXPropertiesType.MAIL_BASIX_URL) + "?login=" + login + "&file=" + fileKey;
	}
}
