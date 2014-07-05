package br.com.voicetechnology.ng.ipx.rule.call.validationcall;

import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.MaxConcurrentCallsException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.OriginationCallBlockedException;
import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.pbx.SpyCallDeniedException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallStateEvent;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.RouteType;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Forward;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunk;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Sipsessionlog;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.rule.implement.AddressManager;
import br.com.voicetechnology.ng.ipx.rule.implement.BlockManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CallManager;
import br.com.voicetechnology.ng.ipx.rule.implement.ConfigManager;
import br.com.voicetechnology.ng.ipx.rule.implement.GroupManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PbxuserManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PermissionManager;
import br.com.voicetechnology.ng.ipx.rule.implement.ServiceClassManager;
import br.com.voicetechnology.ng.ipx.rule.implement.SessionManager;

public abstract class ValidatorBase
{
	protected ConfigManager configManager;
	private BlockManager blockManager;
	private SessionManager sessionManager;
	private CallManager callManager;
	private PermissionManager permissionManager;
	protected PbxuserManager puManager;
	private ServiceClassManager scManager;
	private GroupManager groupManager;
	private AddressManager addressManager;
	
	private Logger logger;
	
	public ValidatorBase() throws DAOException
	{
		configManager = new ConfigManager("EJB");	//TODO avaliar hierarquia...
		blockManager = new BlockManager("EJB");
		sessionManager = new SessionManager("EJB");
		callManager = new CallManager("EJB");
		permissionManager = new PermissionManager("EJB");
		puManager = new PbxuserManager("EJB");
		scManager = new ServiceClassManager("EJB");
		groupManager = new GroupManager("EJB");
		addressManager = new AddressManager("EJB");
		
		logger = Logger.getLogger(ValidatorBase.class);
	}
	
    protected boolean checkBlock(Config config, Leg toLeg, int blockType, Leg fromLeg) throws DAOException
    {
    	return blockManager.isBlockedCall(config, toLeg, blockType, fromLeg);
    }

    protected void checkBlock(Leg fromLeg, Leg toLeg) throws OriginationCallBlockedException, DAOException
    {
    	//início --> dnakamashi - bug #6386 - version 3.0.6
		Config calledConfig = null;
		Serviceclass calledServiceClass = null;
		
		if(toLeg.isPbxuser())
		{
			calledConfig = toLeg.getPbxuser().getConfig();
			calledServiceClass = toLeg.getPbxuser().getServiceclass();
		}
		else if(toLeg.isGroup())
		{
			calledConfig = toLeg.getGroup().getConfig();
			calledServiceClass = toLeg.getGroup().getServiceclass();
		}
		//fim --> dnakamashi - bug #6386 - version 3.0.6
		
		try
		{
			checkBlock(fromLeg, toLeg, calledConfig, Block.TYPE_INCOMING);
		}catch (OriginationCallBlockedException e)
		{
			throw new OriginationCallBlockedException(e.getMessage(), CallStateEvent.CALL_BLOCKED, e.isCauseByServiceClass());
		}
		if(calledServiceClass != null)
			checkBlock(fromLeg, toLeg, calledServiceClass.getConfig(), Block.TYPE_INCOMING);
    }
    
    private void checkBlock(Leg fromLeg, Leg toLeg, Config config, int blockType) throws OriginationCallBlockedException, DAOException
    {
    	//início --> dnakamashi - bug #6386 - version 3.0.6
    	if(fromLeg.hasServiceclass() && isCallBlocked(fromLeg, config, blockType))
    	{
    		Serviceclass sc = fromLeg.getServiceclass();
    		
    		boolean isCausedByServiceClass = false;
    		if(sc!= null)
    			isCausedByServiceClass = checkBlockCauseByServiceClass(config, blockType, sc.getKey());
			throw new OriginationCallBlockedException("Origination address is blocked to this destination.", CallStateEvent.CALL_BLOCKED, isCausedByServiceClass);    			
    	}//fim --> dnakamashi - bug #6386 - version 3.0.6
		else if(checkBlock(config, toLeg, CallType.CALL_RECEIVED, fromLeg))
			throw new OriginationCallBlockedException("Origination address is blocked to this destination.", CallStateEvent.CALL_BLOCKED);
    }
    
    protected boolean checkBlockGroup(Config config, Long gKey, int blockType) throws DAOException
    {
    	return blockManager.isBlockedCallGroup(config, gKey, blockType);
    }
    
    protected boolean checkBlockPbxuser(Config config, Long puKey, int blockType) throws DAOException
    {
    	return blockManager.isBlockedCallPbxuser(config, puKey, blockType);
    }
    
    protected boolean checkBlockCauseByServiceClass(Config config, int blockType, Long serviceclassKey) throws DAOException
    {
    	return blockManager.isBlockedCallFromServiceClass(config, blockType, serviceclassKey);
    }
    
    protected boolean checkPermission(Pbxuser pbxuser, String permission) throws DAOException
    {
    	return permissionManager.checkPermission(pbxuser.getUserKey(), permission);
    }
    
    protected boolean checkForward(Long configKey, int forwardMode) throws DAOException
    {
    	Forward forward = configManager.getForwardByUserAndMode(configKey, forwardMode);
        return forward != null && forward.getStatus() == Forward.STATUS_ON;
    }

    protected boolean checkSipSessionlog(Long puKey) throws DAOException
    {
    	return sessionManager.hasSipsessionlogByPbxuser(puKey);
    }

    protected boolean checkMaxConcurrentCalls(Pbxuser pu) throws DAOException
    {
    	return pu.getUser().getAgentUser().intValue() == User.TYPE_IVR
    				|| pu.getUser().getAgentUser().intValue() == User.TYPE_SIPTRUNK
    				|| configManager.isMaxConcurrentCall(pu.getKey(), pu.getConfig());
    }
    
    protected void checkFilters(Leg fromLeg, Leg toLeg, ValidationModes mode) throws DAOException, ValidationException
    {
		callManager.checkFilters(fromLeg, toLeg, mode);
    }
    
    /**
     * This validation is used to check if originalFrom has same address of from, is Koushi-Kubun call this retuns false.
     */
    protected boolean needsSipSessionValidation(Leg leg)
    {
    	return leg.getSipAddress().equals(leg.getOriginalSipAddress());
    }
    
    protected void loadReferences(Leg leg) throws DAOException
    {
    	RouteType routeType = leg.getRouteType();
    	if(routeType.equals(RouteType.STATION) || routeType.equals(RouteType.UNDEFINED) || leg.isSipTrunk())
    		if(leg.isGroup())
    			loadGroupReferences(leg);
    		else if(leg.isPbxuser())
    			loadPbxuserReferences(leg);
    }
    
	/**
     * Load Config, User (with Preference), Presence and Serviceclass of Pbxuser. 
     */
    private void loadPbxuserReferences(Leg leg) throws DAOException
    {
    	Pbxuser pu = leg.getPbxuser();
    	Pbxuser pbxuser = puManager.getPbxuserFullByKey(pu.getKey());
        pu.setConfig(pbxuser.getConfig());
        pu.setPresence(pbxuser.getPresence());
        pu.setUser(pbxuser.getUser());
        pu.getUser().setPreference(pbxuser.getUser().getPreference());
        Serviceclass sc = scManager.getServiceclassFullByKey(pu.getServiceclassKey());
        pu.setServiceclass(sc);
    }

	/**
     * Load Config of Group. 
     */
    private void loadGroupReferences(Leg leg) throws DAOException
    {
    	Group group = leg.getGroup();
    	group.setConfig(configManager.getConfigByKey(group.getConfigKey()));
    	
    	//inicio --> dnakamashi - bug #6386 - version 3.0.6
    	//Grupo tambem tem ServiceClass
    	if(group.getServiceclassKey() != null)
    	{
    		Serviceclass sc = scManager.getServiceclassFullByKey(group.getServiceclassKey());
    		group.setServiceclass(sc);
    	}
    	//fim --> dnakamashi - bug #6386 - version 3.0.6
    }
    
    //início --> dnakamashi - bug #6386 - version 3.0.6    
    /**
     * Verifica se o address da leg esta entre aqueles que devem ser bloqueados
     */
    protected boolean isCallBlocked(Leg leg, Config config, int blockType) throws DAOException    
    {	
    	if(leg.isPbxuser())    	
    		return checkBlockPbxuser(config, leg.getPbxuser().getKey(), blockType);
    	else if(leg.isGroup())
    		return checkBlockGroup(config, leg.getGroup().getKey(), blockType);
    	
    	return false;
    }
    
    /**
     * Se o Pbxuser estiver usando como DID de saída um DID que pertence a algum grupo
     * a ServiceClass do grupo é retornada.
     */
    protected Serviceclass getPbxuserServiceClass(Leg fromLeg) throws DAOException
    {
    	String display = fromLeg.getDisplay();
    	Group group = null;
    	
    	if(display != null)
    		group = groupManager.getGroupAndServiceClassByDid(display);
    	
    	if(group != null)
    		return group.getServiceclass();

    	return fromLeg.getPbxuser().getServiceclass();
    }
  //fim --> dnakamashi - bug #6386 - version 3.0.6
    
    protected void checkSipTrunkCalls(SipTrunk siptrunk, boolean isFrom) throws DAOException, MaxConcurrentCallsException
    {
    	List<Activecall> activeCallList = callManager.getActiveCallListByPbxuser(siptrunk.getPbxuserKey());
    	if(activeCallList.size() >= siptrunk.getCalls())
    		throw new MaxConcurrentCallsException("SipTrunk Exceded number of concurrent calls", CallStateEvent.DESTINATION_BUSY, isFrom); 	
    	
    }
    
    protected boolean isTerminalWithoutPbxuser(Leg leg)
    {
    	if(leg.isTerminal())
		{
			Pbxuser pu = leg.getPbxuser();
			if(pu == null || pu.getUser().getAgentUser() == User.TYPE_TERMINAL)
				return true;						
		}
    	return false;
    }
    
    protected void checkSpyCall(Pbxuser caller, String address, Long domainKey) throws DAOException, SpyCallDeniedException
    {
    	String tmp = address.split(":|@|,|\\s")[1].trim(); //Retirar o address destino do orginal address da ToLeg
    	List<Long> fromGroupKeys = puManager.getGroupkeyListOfGroupAdministrator(caller.getKey());
    	List<Long> toGroupKeys = groupManager.getGroupkeyListByAddressAndDomainKey(tmp, domainKey);
    	
    	if(fromGroupKeys == null)
    		throw new SpyCallDeniedException("Caller is not a group administrator, don't have permission to make spy call", CallStateEvent.SPYCALL_DENIED);
    	if(toGroupKeys == null)
    		throw new SpyCallDeniedException("Destination is not a group member, caller cannot make spy call", CallStateEvent.SPYCALL_DENIED);
    	
    	boolean contains = false; 
    	
    	for (Long l : fromGroupKeys)
    	{
    		if(!toGroupKeys.contains(l))
    			continue;
    		else
    		{
    			contains = true;
    			break;
    		}
    	}
    	
    	if(!contains)
    		throw new SpyCallDeniedException("Cannot make a spy call because caller is not a Group Administrator of target", CallStateEvent.SPYCALL_DENIED);
    }
    
    protected boolean canDIDMake(String DID)
    {
    	if(DID != null)
    	{
	    	try
	    	{
				Address address = addressManager.getDID(DID);
				return address.getActive().equals(Address.DEFINE_ACTIVE);
		    } catch(Throwable t)
			{
				if(logger.isDebugEnabled())
					logger.error(new StringBuilder("Error while validating DID: ").append(DID));
				
				return true;
			}
    	} else
    	{
    		return true;
    	}
    }
    
    protected boolean canDIDReceive(String DID)
    {
    	if(DID != null)
    	{
	    	try
	    	{
		    	Address address = addressManager.getDID(DID);
		    	return address == null || address.getActive().equals(Address.DEFINE_ACTIVE) || address.getActive().equals(Address.DEFINE_RECEIVEVONLY);
	
	    	} catch(Throwable t)
	    	{
	    		if(logger.isDebugEnabled())
	    			logger.error(new StringBuilder("Error while validating DID: ").append(DID));
	    		
	    		return true;
	    	}
    	} else
    	{
    		return true;
    	}
    }
    
    protected boolean isValidOwnerIp(String contact, Leg fromLeg) throws DAOException
    {
    	if(contact == null) return true;
    	SipAddressParser sipAddress = new  SipAddressParser(contact);
    	
    	if(sipAddress.getDomain().equals(IPXProperties.getProperty(IPXPropertiesType.MEDIA_APP_IP)))
    		return true;
    	
    	List <Sipsessionlog> sslList = null;
    	Long pbxUserKey = null;
    	
    	if(fromLeg.isTerminal())
    	{
    		Pbxuser puTerminal = puManager.getPbxuserByAddressAndDomain(fromLeg.getOriginalSipAddress().getExtension(), fromLeg.getUser().getDomain().getDomain());
    		if(puTerminal != null)
    			pbxUserKey = puTerminal.getKey(); 
    	} else
    		pbxUserKey = fromLeg.getPbxuser().getKey();
    		    		
    	if(pbxUserKey != null)
    	{
    		sslList = sessionManager.getOpenSipSessionLogByPbxUser(pbxUserKey);
    	
	    	for (Sipsessionlog ssl: sslList)
	    	{
	    		if (ssl.getContact().contains(sipAddress.getDomain())) return true;
	    	}
    	}
    	
    	return false;
    }
    
}
