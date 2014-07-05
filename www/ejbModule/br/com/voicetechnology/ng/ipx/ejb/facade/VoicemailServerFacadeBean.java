package br.com.voicetechnology.ng.ipx.ejb.facade;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.SessionBean;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.voicemail.InvalidLoginVoicemailException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PBXServerException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.prompt.PromptFile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Groupfile;
import br.com.voicetechnology.ng.ipx.pojo.db.voicemail.VoiceMailInfo;
import br.com.voicetechnology.ng.ipx.rule.implement.FileinfoManager;
import br.com.voicetechnology.ng.ipx.rule.implement.voicemail.VoicemailManager;

/**
 * 
 * <!-- begin-user-doc --> A generated session bean <!-- end-user-doc --> * <!-- begin-xdoclet-definition -->
 * 
 * @ejb.bean name="VoicemailServerFacade" description="A session bean named VoicemailServerFacade" display-name="VoicemailServerFacade" jndi-name="br/com/voicetechnology/ng/ejb/facade/VoicemailServerFacade" type="Stateless" transaction-type="Container"
 * 
 * <!-- end-xdoclet-definition -->
 * @generated
 */
public abstract class VoicemailServerFacadeBean implements SessionBean
{
	private Logger logger;
	private VoicemailManager vmManager;
	private FileinfoManager fileManager;
	
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
		try
		{
			logger = Logger.getLogger(this.getClass());
			logger.debug("Creating instance of " + this.getClass().getName());
			vmManager = new VoicemailManager(logger.getName());
			fileManager = new FileinfoManager(logger.getName());
		} catch(Exception e)
		{
			PBXServerException ex = new PBXServerException("Error in PBXServerFacade construtor!", e);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public List<Pbxuser> getVoicemailListByFarmIP(String farmIP)
	{
		try
		{
			logger.debug("Start getVoicemailListByFarmIP, farmIP: " + farmIP);
			List<Pbxuser> vmList = vmManager.getVoicemailListByFarmIP(farmIP);
			logger.debug("End getVoicemailListByFarmIP, farmIP: " + farmIP);
			return vmList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getVoicemailListByFarmIP was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public List<Pbxuser> getVoicemailListByDomain(String domain)
	{
		try
		{
			logger.debug("Start getVoicemailListByDomain, farmIP: " + domain);
			List<Pbxuser> vmList = vmManager.getVoicemailListByDomain(domain);
			logger.debug("End getVoicemailListByDomain, farmIP: " + domain);
			return vmList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getVoicemailListByFarmIP was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws InvalidLoginVoicemailException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public VoiceMailInfo getVoiceMailInfo(SipAddressParser sipFrom, SipAddressParser sipTo, Long domainKey) throws InvalidLoginVoicemailException
	{
		try
		{
			logger.debug("Start getVoiceMailInfo, from: " + sipFrom.toString() + " to: " + sipTo.toString() + " domainKey: " + domainKey);
			VoiceMailInfo vmInfo = vmManager.getVoiceMailInfo(sipFrom, sipTo, domainKey);
			logger.debug("End getVoiceMailInfo, from: " + sipFrom.toString() + " to: " + sipTo.toString() + " domainKey: " + domainKey);
			return vmInfo;
		}catch(InvalidLoginVoicemailException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getVoiceMailInfo was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public String[] getPrompt(String locale, PromptFile vmPrompt)
	{
		try
		{
			logger.debug("Start getPrompt, locale: " + locale + " prompt: " + vmPrompt.toString());
			String[] path = vmManager.getPrompt(locale, vmPrompt);
			logger.debug("End getPrompt, locale: " + locale + " prompt: " + vmPrompt.toString());
			return path;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getPrompt was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
 	 * @throws InvalidLoginVoicemailException 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public VoiceMailInfo loginVoicemail(SipAddressParser from, SipAddressParser sipTo, String pin) throws InvalidLoginVoicemailException
	{
		try
		{
			logger.debug("Start loginVoicemail, from: " + from.toString());
			VoiceMailInfo vmInfo = vmManager.loginVoicemail(from, sipTo, pin);
			logger.debug("End loginVoicemail, from: " + from.toString());
			return vmInfo;
		}catch(InvalidLoginVoicemailException e)
		{
			throw e;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when loginVoicemail was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public Fileinfo createVoiceMailMessage(VoiceMailInfo vmInfo)
	{
		try
		{
			logger.debug("Start createVoiceMailMessage, vmInfo: " + vmInfo.toString());
			Fileinfo file = fileManager.createVoiceMailMessage(vmInfo);
			logger.debug("End createVoiceMailMessage, vmInfo: " + vmInfo.toString());
			return file;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when checkMessages was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public void saveVoiceMailMessage(VoiceMailInfo vmInfo, Fileinfo fileInfo)
	{
		try
		{
			logger.debug("Start saveVoiceMailMessage, fileinfo: " + fileInfo.toString());
			vmManager.saveVoiceMailMessage(vmInfo, fileInfo);
			logger.debug("End saveVoiceMailMessage, fileinfo: " + fileInfo.toString());
		
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveVoiceMailMessage was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public void checkMessages(VoiceMailInfo vmInfo)
	{
		try
		{
			logger.debug("Start checkMessages, vmInfo: " + vmInfo.toString());
			vmManager.checkMessages(vmInfo);
			logger.debug("End checkMessages, vmInfo: " + vmInfo.toString());
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when checkMessages was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public void markMessageAsReaded(Long uKey, Long fileinfoKey)
	{
		try
		{
			logger.debug("Start markMessageAsReaded, key: " + fileinfoKey);
			vmManager.markMessageAsReaded(uKey, fileinfoKey);
			logger.debug("End markMessageAsReaded, key: " + fileinfoKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when markMessageAsReaded was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}

	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public void deleteMessage(Long fileinfoKey)
	{
		try
		{
			logger.debug("Start deleteMessage, key: " + fileinfoKey);
			fileManager.deleteFileinfo(fileinfoKey);
			logger.debug("End deleteMessage, key: " + fileinfoKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when deleteMessage was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public void deleteMessage(Long key, Long fileinfoKey)
	{
		try
		{
			logger.debug("Start deleteMessage, key: " + fileinfoKey);
			vmManager.deleteMessage(key, fileinfoKey);
			logger.debug("End deleteMessage, key: " + fileinfoKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when deleteMessage was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public void setDefaultSalutation(VoiceMailInfo vmInfo)
	{
		try
		{
			logger.debug("Start setDefaultSalutation, vmInfo: " + vmInfo.toString());
			vmManager.setDefaultSalutation(vmInfo);
			logger.debug("End setDefaultSalutation, vmInfo: " + vmInfo.toString());
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when setDefaultSalutation was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public void cancelSalutation(Long fileinfoKey)
	{
		try
		{
			logger.debug("Start cancelSalutation, fileKey: " + fileinfoKey);
			vmManager.cancelSalutation(fileinfoKey);
			logger.debug("End cancelSalutation, fileKey: " + fileinfoKey);
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when cancelSalutation was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public Fileinfo createVoicemailSalutation(VoiceMailInfo vmInfo)
	{
		try
		{
			logger.debug("Start createVoicemailSalutation, vmInfo: " + vmInfo.toString());
			Fileinfo fileinfo = fileManager.createVoicemailSalutation(vmInfo);
			logger.debug("End createVoicemailSalutation, vmInfo: " + vmInfo.toString());
			return fileinfo;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when createVoicemailSalutation was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public void saveVoicemailSalutation(VoiceMailInfo vmInfo, Fileinfo newSalutation)
	{
		try
		{
			logger.debug("Start saveVoicemailSalutation, vmInfo: " + vmInfo.toString());
			vmManager.saveVoicemailSalutation(vmInfo, newSalutation, Groupfile.DEFAULT_FILE);
			logger.debug("End saveVoicemailSalutation, vmInfo: " + vmInfo.toString());
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when saveVoicemailSalutation was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
	
	/**
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 */
	public VoiceMailInfo loginGroupVoiceMail(SipAddressParser from, SipAddressParser to, Long domainKey) throws InvalidLoginVoicemailException, RemoteException
	{
		try
		{
			logger.debug("Start loginGroupVoiceMail, extension: " + to.getExtension());
			VoiceMailInfo info = vmManager.loginGroupVoiceMail(from, to, domainKey);
			logger.debug("End loginGroupVoiceMail, extension: " + to.getExtension());
			return info;
		} catch(InvalidLoginVoicemailException e)
		{
			throw e;
		} catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when loginGroupVoiceMail was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
}