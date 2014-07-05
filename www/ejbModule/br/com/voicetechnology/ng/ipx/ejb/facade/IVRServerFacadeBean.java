package br.com.voicetechnology.ng.ipx.ejb.facade;

import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.SessionBean;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PBXServerException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.QuotaException;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxpreferenceDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Target;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVR;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.CostCenter;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.DialPlan;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxpreference;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.prompt.PromptFile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Preference;
import br.com.voicetechnology.ng.ipx.rule.implement.AddressManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CallManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CostCenterManager;
import br.com.voicetechnology.ng.ipx.rule.implement.DialPlanManager;
import br.com.voicetechnology.ng.ipx.rule.implement.FileinfoManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PbxManager;
import br.com.voicetechnology.ng.ipx.rule.implement.PbxuserManager;
import br.com.voicetechnology.ng.ipx.rule.implement.SessionManager;
import br.com.voicetechnology.ng.ipx.rule.implement.ivr.IVRManager;

/**
 * 
 * <!-- begin-user-doc --> A generated session bean <!-- end-user-doc --> * <!-- begin-xdoclet-definition -->
 * 
 * @ejb.bean name="IVRServerFacade" description="A session bean named IVRServerFacade" display-name="IVRServerFacade" jndi-name="br/com/voicetechnology/ng/ejb/facade/IVRServerFacade" type="Stateless" transaction-type="Container"
 * 
 * <!-- end-xdoclet-definition -->
 * @generated
 */
public abstract class IVRServerFacadeBean implements SessionBean
{
	private Logger logger;
	private IVRManager ivrManager;
	private AddressManager addManager;
	private SessionManager sessionManager;
	private FileinfoManager fileinfoManager;
	private CallManager callManager;
	private PbxuserManager puManager;
	private DialPlanManager dPlanManager;
	private PbxManager pbxManager;
	private CostCenterManager costCenterManager;
	public IVRServerFacadeBean()
	{
		try
		{
			logger = Logger.getLogger(this.getClass());
			logger.info("Creating instance of " + this.getClass().getName());
			ivrManager = new IVRManager(logger.getName());
			addManager = new AddressManager(logger.getName());
			sessionManager = new SessionManager(logger.getName());
			fileinfoManager = new FileinfoManager(logger.getName());
			callManager = new CallManager(logger.getName());
			puManager = new PbxuserManager(logger.getName());
			dPlanManager = new DialPlanManager(logger.getName());
			pbxManager = new PbxManager(logger.getName());
			costCenterManager = new CostCenterManager(logger);
		} catch(Exception e)
		{
			PBXServerException ex = new PBXServerException("Error in PBXServerFacade construtor!", e);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * 
	 * <!-- begin-xdoclet-definition -->
	 * 
	 * @ejb.create-method view-type="remote" <!-- end-xdoclet-definition -->
	 * @generated
	 * 
	 * //TODO: Must provide implementation for bean create stub
	 */
	public void ejbCreate()
	{

	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public IVR getIVRApplication(Long ivrKey)
	{
		try
		{
			logger.debug("Start getIVRApplication, key: " + ivrKey);
			IVR ivr = ivrManager.getIVRApplication(ivrKey);
			logger.debug("End getIVRApplication, key: " + ivrKey);
			return ivr;
		} catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getIVRApplication was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public List<IVR> getIVRListByFarmIP(String farmIP)
	{
		try
		{
			logger.debug("Start getIVRListByFarmIP, farmIP: " + farmIP);
			List<IVR> ivrList = ivrManager.getIVRListByFarmIP(farmIP);
			logger.debug("End getIVRListByFarmIP, farmIP: " + farmIP);
			return ivrList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getIVRListByFarmIP was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public Block getOutgoingBlockByIVRKey(Long ivrKey)
	{
		try
		{
			logger.debug("Start getOutgoingBlockkByIVRKey, IVRKey: " + ivrKey);
			Block block = ivrManager.getOutgoingBlockByIVRKey(ivrKey);
			logger.debug("End getOutgoingBlockByIVRKey, IVRKey: " + ivrKey);
			return block;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getOutgoingBlockByIVRKey was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public List<IVR> getIVRListByDomain(String domain)
	{
		try
		{
			logger.debug("Start getIVRListByDomain, domain: " + domain);
			List<IVR> ivrList = ivrManager.getIVRListByDomain(domain);
			logger.debug("End getIVRListByDomain, domain: " + domain);
			return ivrList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getIVRListByDomain was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public Address getAddress(String address, String domain)
	{
		try
		{
			logger.debug("Start getAddress, address: " + address + "@" + domain);
			Address add = addManager.getAddress(address, domain);
			logger.debug("End getAddress, farmIP: " + address + "@" + domain);
			return add;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getAddress was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public Target lookupSipSession(String address, String domain)
	{
		try
		{
			logger.debug("Start lookupSipSession, address: " + address + "@" + domain);
			Target target = sessionManager.lookupSipSession(address, domain);
			logger.debug("End lookupSipSession, farmIP: " + address + "@" + domain);
			return target;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when lookupSipSession was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */	
	public List<Fileinfo> getDisabledFiles() throws RemoteException
	{
		try
		{
			logger.debug("Start getDisabledFiles");
			List<Fileinfo> fileinfoList = fileinfoManager.getDisabledFiles();
			logger.debug("End getDisabledFiles");
			return fileinfoList;
		} catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when lookupSipSession was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	public Activecall getActivecall(String address, String domain, int state)
	{
		try
		{
			logger.debug("Start getActivecall, from: " + address + "@" + domain);
			Activecall ac = callManager.getActivecall(address, domain, state);
			logger.debug("End getActivecall, from: " + address + "@" + domain);
			return ac;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getActivecall was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
        
        /**
         * @ejb.interface-method view-type="remote"
         * @generated
         * @ejb.transaction type="Required"
         */
        public void permanentlyRemoveFileinfo(long fileinfoKey)
        {
            try 
            {
                fileinfoManager.permanentlyRemoveFileinfo(fileinfoKey);
            } catch (Throwable t)
            {
                StringBuilder sb = new StringBuilder("Fail permanently removing file info: ");
                sb.append(t.getMessage());
                logger.error(sb.toString(), t);
            }
        }
        
        /**
         * @ejb.interface-method view-type="remote"
         * @generated
         * @ejb.transaction type="Required"
         */
        public void saveNewFille(Fileinfo fileinfo)
        {
            try 
            {	
            	/*jluchetta -  Versão 3.0.5 - Correção apara garvar arquivo de saudação de record box
				*Agora os arquivso já são previamente criados na apsta do domínio correto e depois ele somente é atualizado com as informacoes e tamanho corretos
				* Isso evita a copia da pasta temporária para a definitiva
				*/
                fileinfoManager.saveRecordFile(fileinfo);
            } catch (Throwable t)
            {
                StringBuilder sb = new StringBuilder("Fail permanently removing file info: ");
                sb.append(t.getMessage());
                logger.error(sb.toString(), t);
            }
        }
        
        /**
         * @ejb.interface-method view-type="remote"
         * @generated
         * @ejb.transaction type="Required"
         */
        public Fileinfo createNewFile(Long domainKey, String name)
        {
            try 
            {
                return fileinfoManager.createNewFile(domainKey, name);
            } catch (Throwable t)
            {
            	PBXServerException ex = new PBXServerException("A error ocurrs when createNewFile was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
            }
        }  
        
        /**
     	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public String[] getPrompt(String locale, PromptFile ivrPrompt)
    	{
    		try
    		{    			
    			String[] path = ivrManager.getPrompt(locale, ivrPrompt);    		
    			return path;
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when getPrompt was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}
    	
    	/**
    	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public void cancelNewFile(Long fileinfoKey)
    	{
    		try
    		{    			
    			fileinfoManager.deleteFileinfo(fileinfoKey);    		
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when cancelNewFile was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}
    	
    	/**
    	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public Preference getPreferenceByUsernameAndDomain(String username, String domain)
    	{
    		try
    		{    			
    			 return puManager.getPreferenceByUsernameAndDomain(username, domain);
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when getPreferenceByUsernameAndDomain was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}
    	
    	/**
    	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public DialPlan getDialPlanByTypeAndDomain(String domain, int type)
    	{
    		try
    		{    			
    			 return dPlanManager.getDialPlanByTypeAnddomain(domain, type);
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when getDialPlanByTypeAndDomain was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}
    	
    	/**
    	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public Pbxuser getPbxUserWithExtensionList(String username, String domain)
    	{
    		try
    		{    			
    			 return puManager.getPbxUserWithExtensionList(username, domain);
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when getPbxUserWithExtensionList was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}    	
    	
    	/**
    	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public Pbxuser getPbxUserByAddressAndDomain(String extension, String domain)
    	{
    		try
    		{    			
    			 return puManager.getPbxuserByAddressAndDomain(extension, domain);
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when getPbxUserByAddressAndDomain was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}  	
    	
    	/**
    	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public Pbxpreference getPbxPreference(String domain) throws RemoteException
    	{
    		try
    		{    			
    			 return pbxManager.getPbxPreference(domain);
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when getPbxUserByAddressAndDomain was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}   
    	
    	/**
    	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public CostCenter getCostCenter(String domain, String code) throws RemoteException
    	{
    		try
    		{    			
    			 return costCenterManager.getByDomainAndCode(domain, code);
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when getPbxUserByAddressAndDomain was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}
    	
    	/**
    	 * @ejb.interface-method view-type="remote"
    	 * @generated
    	 * @ejb.transaction type="Required"
    	 */
    	public Pbxuser getPbxuserOrTerminalWithConfigByAddressAndDomain(String extension, String domain) throws RemoteException
    	{
    		try
    		{    			
    			return callManager.getPbxuserOrTerminalByAddressAndDomain(extension, domain);    			 
    		}catch(Throwable t)
    		{
    			PBXServerException ex = new PBXServerException("A error ocurrs when getPbxuserWithConfigByAddressAndDomain was executed!", t);
    			logger.error(ex.getLocalizedMessage());
    			throw ex;
    		}
    	}    	
}