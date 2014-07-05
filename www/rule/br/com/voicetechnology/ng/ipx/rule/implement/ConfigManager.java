package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.command.CommandConfigException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.config.ConfigException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.commons.utils.regex.RegEx;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CallfilterDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.FilterDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ForwardDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserterminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.UsergroupDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.Permissions;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Callfilter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Filter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuserterminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Terminal;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.facets.config.SipIDListFacet;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CallFilterInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ConfigInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class ConfigManager extends Manager
{
	private final int PSTN_LENGTH = 6;
	
	private ConfigDAO confDAO;
	private ActivecallDAO acDAO;
	private PbxuserDAO puDAO;
	private ForwardDAO fDAO;
	private AddressDAO addDAO;
	private PbxDAO pbxDAO;
	private GroupDAO gDAO;
	private UsergroupDAO ugDAO;
	private TerminalDAO tDAO;
	private PbxuserterminalDAO ptDAO;
	private CallfilterDAO cfDAO;
	private FilterDAO filterDAO;
	private PermissionManager permManager;
	private DialPlanManager dPlanManager;
	
	public ConfigManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		confDAO = dao.getDAO(ConfigDAO.class);
		acDAO = dao.getDAO(ActivecallDAO.class);
		puDAO = dao.getDAO(PbxuserDAO.class);
		fDAO = dao.getDAO(ForwardDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);
		gDAO = dao.getDAO(GroupDAO.class);
		ugDAO = dao.getDAO(UsergroupDAO.class);
		tDAO = dao.getDAO(TerminalDAO.class);
		ptDAO = dao.getDAO(PbxuserterminalDAO.class);
		permManager = new PermissionManager(logger);
		cfDAO = dao.getDAO(CallfilterDAO.class);
		filterDAO = dao.getDAO(FilterDAO.class);
		dPlanManager = new DialPlanManager(loggerPath);
	}

	public Config getConfigByKey(Long confKey) throws DAOException
	{
		return confDAO.getByKey(confKey);
	}
	
	public boolean isMaxConcurrentCall(Long puKey, Config conf) throws DAOException
	{
		Long actual = acDAO.howManyActiveCallByPbxuser(puKey);
		Config tmp = confDAO.getConfigServiceclassByPbxuser(puKey);
		if(tmp.getMaxConcurrentCalls() != null)
			return actual.longValue() < tmp.getMaxConcurrentCalls().intValue();
		return false;
	}
	
	public void validateCommandOwner(SipAddressParser sipAddress) throws DAOException, CommandConfigException
	{
		Terminal t = tDAO.getTerminalByAddressAndDomain(sipAddress.getExtension(), sipAddress.getDomain());
		if(t != null)
		{
			List<Pbxuserterminal> ptList = ptDAO.getPbxuserterminalByTerminal(t.getKey());
			if(ptList.size() == 0)		//no user associated to terminal.
				throw new CommandConfigException("Terminal cannot execute commands!");
			Pbxuser pu = puDAO.getByKey(ptList.get(0).getPbxuserKey());
			sipAddress.setExtension(pu.getUser().getUsername());
		}
	}
	
	public void validateCommandOwner(SipAddressParser sipAddress, String extension, boolean loginState) throws DAOException, CommandConfigException, ValidateObjectException
	{
		Terminal t = tDAO.getTerminalByAddressAndDomain(sipAddress.getExtension(), sipAddress.getDomain());
		if(t != null)
		{
			List<Pbxuserterminal> ptList = ptDAO.getPbxuserterminalByTerminal(t.getKey());
			Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(parseAddress(extension), sipAddress.getDomain());

			if(loginState) // Login CallCenter
			{
				if(ptList.size() == 0)	
				{
					//no user associated to terminal.
					Pbxuserterminal pt = new Pbxuserterminal();
					pt.setKey(null);
					pt.setPbxuserKey(pu.getKey());
					pt.setTerminalKey(t.getKey());
					ptDAO.save(pt);
				}
				else
				{
					//associate new user to terminal
					Pbxuserterminal pt = ptList.get(0);
					pt.setPbxuserKey(pu.getKey());
					ptDAO.save(pt);
				}
			}
			else // Logout CallCenter
			{
				if(ptList.size() != 0 && pu.getKey().equals(ptList.get(0).getPbxuserKey()))
					ptDAO.remove(ptList.get(0));
				else
					throw new CommandConfigException("Terminal cannot execute Logout CallCenter to not associated user!");
			}
			sipAddress.setExtension(pu.getUser().getUsername());
		}
	}
	
	private String parseAddress(String extension)
	{
		if(extension.length() > 4) //address+pin
		{
			//parse extension to Login CallCenter
			String pin = extension.substring(extension.length()-4,extension.length());
			return extension.replace(pin, "").trim();
		}
		else // only address
		{
			//return extension to Logout CallCenter
			return extension;
		}
	}
	
	//pbx
	public void executeDNDConfig(SipAddressParser sipFrom, int dndStatus) throws DAOException, ValidateObjectException, CommandConfigException
	{
		Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(sipFrom.getExtension(), sipFrom.getDomain());
		Config config = confDAO.getByKey(pu.getConfigKey());
		if(dndStatus != Config.DND_OFF && dndStatus != Config.DND_ON)
			throw new IllegalArgumentException("DND valid values are: 0 - OFF, 1 - ON, please check this!");
		config.setDndStatus(dndStatus);
		confDAO.save(config);
	}

	public void executeForward(SipAddressParser sipFrom, String target, int forwardMode, int forwardStatus) throws DAOException, ValidateObjectException, CommandConfigException
	{
		Pbxuser fromPbxuser = puDAO.getPbxuserByAddressAndDomain(sipFrom.getExtension(), sipFrom.getDomain());
        Forward f = fDAO.getForwardByConfig(fromPbxuser.getConfigKey(), forwardMode);
        if(forwardStatus != Forward.STATUS_OFF && forwardStatus != Forward.STATUS_ON)
        	throw new IllegalArgumentException("Forward valid values are: 0 - OFF, 1 - ON, please check this!");
        f.setStatus(forwardStatus);
        if(forwardStatus == Forward.DEFINE_DELETED){
       		return;
        }
        	

        Address add = null;
        if(target != null)
        	add = addDAO.getAddress(target, sipFrom.getDomain());
        
        if(add != null)
        {
        	if(add.getPbxuserKey() != null && add.getPbxuserKey().longValue() == fromPbxuser.getKey().longValue())
        		throw new IllegalArgumentException("Forward invalid, cannot forward calls to yourself!");
        }
        
        Long addressKey = null;
        String forwardTo = null;

        if(target == null && f.getAddressKey() != null)
        {
            addressKey = f.getAddressKey();
            forwardTo = f.getTarget();
        }
        else if(target == null && f.getTarget() != null)
            forwardTo = f.getTarget();
        else if(target != null && add != null)
        {
        	/**
        	 * jfarah - versao 3.0.3 RC1
        	 * valida se usuario adquiriu o voicemail pelo basix store, 
        	 * caso o desvio seja para o voicemail.
        	 */
            if(add.getAddress().matches("\\w{1,30}"))	//is sipID
            {
            	if(add.getPbxuserKey() != null && add.getAddress().equals(User.VOICEMAIL_NAME))
            		addressKey = validateForwardPbxuserTarget(fromPbxuser, add);
            	else
            		addressKey = add.getKey();
            }
            
            if(add.getPbxuserKey() != null && !add.getPbxuserKey().equals(fromPbxuser.getKey()))
            {
                addressKey = validateForwardPbxuserTarget(fromPbxuser, add);
                Pbxuser pu  = puDAO.getPbxuserByAddressKey(addressKey);
                forwardTo = pu != null ? pu.getUser().getUsername() : null;
                if(forwardTo != null && forwardTo.equals(User.RECORDFILEBOX_NAME))
                	throw new IllegalArgumentException("Forward invalid, cannot forward calls to Record FileBox!");
            }
            else if(add.getGroupKey() != null)
            {
                addressKey = addDAO.getSipIDByGroup(add.getGroupKey()).getKey();
                Group group = gDAO.getGroupByAddressKey(addressKey);
                forwardTo = group != null ? group.getName() : null;
            }
            else
            	throw new CommandConfigException("Invalid extension, please check that!!! Target is " + target);
        } else if(target != null && target.length() >= getAddressLength()) //TODO isPstn
        	forwardTo = target;
        else
            throw new CommandConfigException("Invalid target, please check that!!! Target is " + target);

        f.setAddressKey(addressKey);
        f.setTarget(forwardTo);
        fDAO.save(f);
	}
	
	private int getAddressLength()
	{
		return Integer.valueOf(IPXProperties.getProperty(IPXPropertiesType.CENTREX_ADDRESS_LENGTH));
	}
	
    private Long validateForwardPbxuserTarget(Pbxuser fromPbxuser, Address add) throws DAOException
    {
		Pbxuser toPbxuser = puDAO.getByKey(add.getPbxuserKey());
		int agentType = toPbxuser.getUser().getAgentUser().intValue();
		if(agentType == User.TYPE_VOICEMAIL) 
		{
			Config fromConfig = confDAO.getByKey(fromPbxuser.getConfigKey());

			/**
        	 * jfarah - versao 3.0.3 RC1
        	 * valida se usuario adquiriu o voicemail pelo basix store, 
        	 * caso o desvio seja para o voicemail.
        	 */
			validateForwardToVoicemail(fromConfig);
		} else if(agentType == User.TYPE_TERMINAL)
		{
			Pbxuser associatedTerminalPbxuser = tDAO.getAssociatedPbxuserByTerminalPbxuserKey(toPbxuser.getKey());
			if(associatedTerminalPbxuser != null && fromPbxuser.getKey().longValue() == associatedTerminalPbxuser.getKey().longValue())
				throw new CommandConfigException("Invalid target, cannot make forward to your own terminal!!!");
		}
		return addDAO.getSipIDByPbxuser(toPbxuser.getKey()).getKey();
    }
	
  //inicio --> dnakamashi - Star Codes para Forward de Grupo - version 3.0.5
	public void executeGroupForward(SipAddressParser sipFrom, String commandData, int forwardMode, int forwardStatus) throws DAOException, ValidateObjectException, CommandConfigException
	{		
		Group group = null;
		String groupExtension = null;
		String target = null;
		
		DialPlan extensionDialPlan = dPlanManager.getDialPlanByTypeAnddomain(sipFrom.getDomain(), DialPlan.TYPE_EXTENSION);
		int dialPlanStartLength = String.valueOf(extensionDialPlan.getStart()).length();
		int dialPlanEndLength = String.valueOf(extensionDialPlan.getEnd()).length();	
		
		
		do
		{
			groupExtension =commandData.substring(0, dialPlanStartLength);
			target =commandData.substring(dialPlanStartLength);
			
			group = gDAO.getGroupByAddressAndDomain(groupExtension, sipFrom.getDomain());
		
			if(group != null)
			{
				Pbxuser fromPbxuser = puDAO.getPbxuserByAddressAndDomain(sipFrom.getExtension(), sipFrom.getDomain());		
				Usergroup usergroup = ugDAO.getUsergroupByPbxuserAndGroup(fromPbxuser.getKey(), group.getKey());
				if(usergroup != null && usergroup.getGroupAdmin() == Usergroup.ADMIN_ON)					
					break;					
			}
			++dialPlanStartLength;
			
		}while(dialPlanStartLength <= dialPlanEndLength);
		
		if(group == null)
			throw new CommandConfigException("Invalid Group extension, please check that!!! Group Extension is " + groupExtension);		
				
    	Config config = confDAO.getByKey(group.getConfigKey());
    	if(config.getAllowedGroupForward() == Config.NOT_ALLOWED_GROUPFORWARD)
    		throw new CommandConfigException("Group Forward is not allowed.");	
		
		Forward forward = fDAO.getForwardByConfig(config.getKey(), forwardMode);
		
		Address add = null;
		if(target != null)
        	add = addDAO.getAddress(target, sipFrom.getDomain());
		
		validateGroupForward(group, add, forward, config, target, forwardStatus);					
		
		fDAO.save(forward);
	}	
	
	//Valida, seta o target e a key
	//Validações: Verifica se o Voicemail esta habilitado, se ele esta allowed(Basix Store) e se o forward é para o próprio grupo
    private void validateGroupForward(Group group, Address add, Forward forward, Config config, String target, int forwardStatus) throws DAOException    
    {    	
    	Long forwardAddressKey = null;    	
    	if(add == null)
    	{    		
    		if(target.length() == 0)
    		{
    			if(forward.getAddressKey() == null && forward.getTarget() == null && forward.getStatus() == Forward.STATUS_ON)				
    				throw new CommandConfigException("Cannot enable this forward, because the target is null.");
    			if(forward.getAddressKey() != null)
    			{				
    				if(isForwardToVoicemail(forward.getAddressKey()))
    					validateForwardToVoicemail(config);    				    				
    			}    					
    		}
    		else
    		{	
    			if(target.length() < PSTN_LENGTH)
    				throw new CommandConfigException("Invalid external number. Number = " + target);    			
    			
    			forward.setTarget(target);
    			forward.setAddressKey(null);
    		}
    	}
    	else if(add.getPbxuserKey() != null)
    	{    		
    		if(isForwardToVoicemail(add.getPbxuserKey()))
    			validateForwardToVoicemail(config);    		
    		forward.setAddressKey(addDAO.getSipIDByPbxuser(add.getPbxuserKey()).getKey());
    		Pbxuser pu = puDAO.getPbxuserByAddressKey(forward.getAddressKey());
    		forward.setTarget(pu.getUser().getUsername());
    		if(forward.getTarget() != null && forward.getTarget().equals(User.RECORDFILEBOX_NAME))
    			throw new CommandConfigException("Cannot configure forward for RecordFileBox.");
    	}
       	else if(add.getGroupKey() != null)
       	{
       		if(add.getGroupKey().longValue() == group.getKey().longValue())
       			throw new IllegalArgumentException("Forward invalid, cannot forward calls to the same group!");
       		
       		forward.setAddressKey(addDAO.getSipIDByGroup(add.getGroupKey()).getKey());
       		Group destinationGroup = gDAO.getGroupByAddressKey(forward.getAddressKey());
       		forward.setTarget(destinationGroup.getName());
       	}
    	forward.setStatus(forwardStatus);
    }
    
    private boolean isForwardToVoicemail(Long addressKey) throws DAOException
    {
    	Pbxuser toPbxuser = puDAO.getByKey(addressKey);
    	if(toPbxuser != null)
    	{
    		int agentType = toPbxuser.getUser().getAgentUser().intValue();		
    		if(agentType == User.TYPE_VOICEMAIL)
    			return true;
    	}		
		return false;
    }
    
    private void validateForwardToVoicemail(Config config)
    {
    	if(config.getAllowedVoiceMail() != Config.ALLOWED_VOICEMAIL)
			throw new CommandConfigException("Voicemail not Allowed. ");
		if(config.getDisableVoicemail() == Config.VOICEMAIL_OFF)
			throw new CommandConfigException("Voicemail is disabled. ");
    } 
    //fim --> dnakamashi - Star Codes para Forward de Grupo - version 3.0.5
    
	public void executeFollowMe(SipAddressParser sipFrom, String target, int followMeStatus) throws DAOException, ValidateObjectException, CommandConfigException
	{
		Pbxuser from = puDAO.getPbxuserByAddressAndDomain(sipFrom.getExtension(), sipFrom.getDomain());
		Long targetKey = addDAO.getSipIDByPbxuser(from.getKey()).getKey();
		
		Address add = addDAO.getAddress(target, sipFrom.getDomain());
		if(add == null)
			throw new CommandConfigException("Follow me target is wrong!!!Please check target: " + target);
		if(add.getPbxuserKey() == null)
			throw new CommandConfigException("Follow me just be used between pbxusers, please check the target extension!!!");
		Pbxuser to = puDAO.getByKey(add.getPbxuserKey());
		if(to.getUser().getAgentUser().intValue() == User.TYPE_TERMINAL)
			to = tDAO.getAssociatedPbxuserByTerminalPbxuserKey(to.getKey());
		if(from.getKey().longValue() == to.getKey().longValue())
			throw new CommandConfigException("Cannot use follow me to yourself or your terminals!!!");
		if(to.getUser().getAgentUser().intValue() != User.TYPE_PBXUSER)
			throw new CommandConfigException("Follow me can be used just with pbxuser targets!!!");
		Forward f = fDAO.getForwardByConfig(to.getConfigKey(), Forward.ALWAYS_MODE);
		f.setAddressKey(targetKey);
		if(followMeStatus != Forward.STATUS_OFF && followMeStatus != Forward.STATUS_ON)
			throw new IllegalArgumentException("Forward valid values are: 0 - OFF, 1 - ON, please check this!");
		f.setStatus(followMeStatus);
		fDAO.save(f);
	}

	public void executeNightmode(SipAddressParser sipFrom, String groupAddress, int status, int nightmodeType) throws DAOException, ValidateObjectException, CommandConfigException
	{
		//jluchetta - Início - Mudança para separar comando para habilitar ou desabilitar night mode do grupo e do PBX (BUG ID: 5765)
		if(nightmodeType == Config.NIGHT_MODE_PBX)
			executeNightmodePBX(sipFrom, status);
		else
			executeNightmodeGroup(sipFrom, groupAddress, status);
		//jluchetta - FIM - Mudança para separar comando para habilitar ou desabilitar night mode do grupo e do PBX (BUG ID: 5765)
	}

	private void executeNightmodePBX(SipAddressParser sipFrom, int status) throws DAOException, ValidateObjectException, ConfigException
	{
		Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(sipFrom.getExtension(), sipFrom.getDomain());
		if(!permManager.checkPermission(pu.getUserKey(), Permissions.CONFIG_NIGHTMODE_USER))
			throw new ConfigException("User don�t have permission to do this action!");
		Pbx pbx = pbxDAO.getPbxByDomain(sipFrom.getDomain());
		if(status != Pbx.NIGHTMODE_OFF && status != Pbx.NIGHTMODE_ON)
			throw new IllegalArgumentException("Pbx nightmode valid values are: 0 - OFF, 1 - ON, please check this!");
		
		//ffontes 25/10/2006 bug #1866
		if (pbx.getNightmodeaddressKey() == null)
		{
			throw new ConfigException("No night mode destination address, cannot activate night mode");
		}
		//ffontes 25/10/2006 bug #1866
		
		pbx.setNightMode(status);
		pbxDAO.save(pbx);
	}
	
	private void executeNightmodeGroup(SipAddressParser sipFrom, String groupAddress, int status) throws DAOException, ConfigException, ValidateObjectException
	{
		//jluchetta - FIM - Validação para ver se o addres do grupo foi passado no comando (BUG ID: 5765)
		if(groupAddress == null || groupAddress.length()==0)
			throw new ConfigException("Group address is empty, please check the group address!!");
		//jluchetta - FIM - Validação para ver se o addres do grupo foi passado no comando (BUG ID: 5765)
		Address add = addDAO.getAddress(groupAddress, sipFrom.getDomain());
		if(add == null || add.getGroupKey() == null)
			throw new ConfigException("Please check address of group!!");
		Group g = gDAO.getByKey(add.getGroupKey());
		if(status != Group.NIGHTMODE_OFF && status != Group.NIGHTMODE_ON)
			throw new IllegalArgumentException("Group nightmode valid values are: 0 - OFF, 1 - ON, please check this!");

		//ffontes 25/10/2006 bug #1866
		if (g.getNightmodeaddressKey() == null)
		{
			throw new ConfigException("No night mode destination address, cannot activate night mode");
		}
		//ffontes 25/10/2006 bug #1866
		
		Pbxuser pu = puDAO.getPbxuserByAddressAndDomain(sipFrom.getExtension(), sipFrom.getDomain());
		Usergroup ug = ugDAO.getUsergroupByPbxuserAndGroup(pu.getKey(), g.getKey());
		if(ug.getGroupAdmin().equals(Group.ADMIN_OFF))
			throw new ConfigException("User isnt Admin of group!");
		g.setNightmodeStatus(status);
		gDAO.save(g);
	}

	public Forward getForwardByUserAndMode(Long configKey, int mode) throws DAOException
	{
		return fDAO.getForwardByConfig(configKey, mode);
	}

	public CallFilterInfo getPbxuserCallFilter(Long pbxuserKey) throws DAOException
	{
		Pbxuser pbxuser = puDAO.getPbxuserFull(pbxuserKey);
		Callfilter callFilter = cfDAO.getCallFilterByPbxuser(pbxuserKey);
		List<Filter> filterList = cfDAO.getFilterByCallFilter(callFilter.getKey());
		callFilter.setFilterList(filterList);
		CallFilterInfo callFilterInfo = new CallFilterInfo(callFilter);
		setSipIDList(callFilterInfo, pbxuser.getUser().getDomainKey(), true, pbxuser.getUser().getUsername());
		return callFilterInfo;
	}
	
	public void updatePbxuserCallFilter(CallFilterInfo callFilterInfo) throws DAOException, ValidateObjectException 
	{
		List<Filter> filterList = callFilterInfo.getFilterList();
		List<Address> didUserList = addDAO.getDIDListByPbxuser(callFilterInfo.getPbxuserKey());
		for(int i = 0; i < filterList.size(); i++)
			for(int j = 0; j < didUserList.size(); j++)
				if(filterList.get(i).getAction() == Filter.ACTION_FORWARD && filterList.get(i).getTarget().equals(didUserList.get(j).getAddress()))
					throw new ValidateObjectException("Cannot add this filter because its target is a did of pbxuser!!!", Filter.class, filterList.get(i), ValidateType.INVALID);

		Callfilter callFilterOld = cfDAO.getCallFilterByPbxuser(callFilterInfo.getPbxuserKey());
		callFilterOld.setActive(callFilterInfo.getStatus());
		callFilterOld.setFilterList(callFilterInfo.getFilterList());
		boolean edit = callFilterOld.getKey() != null;
		cfDAO.save(callFilterOld);
		if(edit)
			for(Filter fi : cfDAO.getFilterByCallFilter(callFilterOld.getKey()))
				filterDAO.remove(fi);
		for(Filter fi: callFilterOld.getFilterList())
		{
			fi.setKey(null);
			fi.setCallfilterKey(callFilterOld.getKey());
			fi.setRegex(RegEx.getRegEx(fi.getPattern()));
			filterDAO.save(fi);
		}
	}
	
	
	protected void setSipIDList(SipIDListFacet info, Long domainKey, boolean voicemail, String... excludeUserSipID) throws DAOException
	{
		String[] term = null;
		List<String> terminal = null;
		if (excludeUserSipID.length > 0)
		{
			terminal = filterDAO.getTerminalByUser(excludeUserSipID[0], domainKey);
		} else
		{
			terminal = new ArrayList<String>();
		}
		if (terminal.size() > 0)
		{
			term = terminal.toArray(excludeUserSipID);
		} else
		{
			term = new String[0];
		}

		info.addUsernameSipIDList(puDAO.getPbxuserKeyAndUsernameList(domainKey, excludeUserSipID));
		info.addSipTrunkNameSipIDList(puDAO.getSipTrunkKeyAndUsernameList(domainKey, excludeUserSipID));
		info.addGroupNameSipIDList(addDAO.getGroupKeyAndSipIDByDomain(domainKey, false));
		info.addTerminalNameSipIDList(addDAO.getTerminalPbxuserKeyAndTerminalNAmeByDomain(domainKey, term));
		info.addIVRSipIDList(addDAO.getIVRPbxuserKeyAndIVRNameByDomain(domainKey));
	}

	public ReportResult findConfig(Report<ConfigInfo> report) throws DAOException 
	{
		Long size = confDAO.getReportCount(report);
        List<Config> configList = confDAO.getReportList(report);
		List<ConfigInfo> configInfoList = new ArrayList<ConfigInfo>(configList.size());
		
		for(Config config : configList)
			configInfoList.add(new ConfigInfo(config));
		
		return new ReportResult<ConfigInfo>(configInfoList, size);
	}
// tveiga - inicio - basix store
	public void save(Config config) throws DAOException, ValidateObjectException
	{
		validateConfig(config);
		confDAO.save(config);
	}

	private void validateConfig(Config config) throws ValidateObjectException {
		if(config != null)
		{
		 if(config.getKey() == null)	
		   throw new ValidateObjectException("Config without key " , Config.class, config, ValidateType.INVALID);
		}
	}
// tveiga - inicio - basix store

}