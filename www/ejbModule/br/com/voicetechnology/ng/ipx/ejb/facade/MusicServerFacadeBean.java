package br.com.voicetechnology.ng.ipx.ejb.facade;

import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.ejb.PBXServerException;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.rule.implement.music.MusicManager;

/**
 * 
 * <!-- begin-user-doc --> A generated session bean <!-- end-user-doc --> * <!-- begin-xdoclet-definition -->
 * 
 * @ejb.bean name="MusicServerFacade" description="A session bean named MusicServerFacade" display-name="MusicServerFacade" jndi-name="br/com/voicetechnology/ng/ejb/facade/MusicServerFacade" type="Stateless" transaction-type="Container"
 * 
 * <!-- end-xdoclet-definition -->
 * @generated
 */
public abstract class MusicServerFacadeBean implements javax.ejb.SessionBean
{
	private Logger logger;
	private MusicManager musicManager;
	
	public MusicServerFacadeBean()
	{
		try
		{
			logger = Logger.getLogger(this.getClass()); 
			musicManager = new MusicManager(logger.getName());
		}catch(Exception e)
		{
			PBXServerException ex = new PBXServerException("Error in MusicServerFacade construtor!", e);
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
		//do nothing
	}
	
	/**
 	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws PBXServerException
	 */
	@SuppressWarnings("unchecked")
	public List<User> getMusicServerListByFarmIP(String farmIP) throws PBXServerException
	{
		try
		{
			logger.debug("Start getMusicServerListByFarmIP, farmIP: " + farmIP);
			List<User> agentList = musicManager.getMusicServerListByFarmIP(farmIP);
			logger.debug("End getMusicServerListByFarmIP, farmIP: " + farmIP);
			return agentList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getMusicServerListByFarmIP was executed!", t);
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
	@SuppressWarnings("unchecked")
	public List<User> getMusicServerListByDomain(String domain) throws PBXServerException
	{
		try
		{
			logger.debug("Start getMusicServerListByDomain, domain: " + domain);
			List<User> agentList = musicManager.getMusicServerListByDomain(domain);
			logger.debug("End getMusicServerListByDomain, domain: " + domain);
			return agentList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getMusicServerListByDomain was executed!", t);
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
	@SuppressWarnings("unchecked")
	public List<Fileinfo> getMusicFileList(Long userKey) throws PBXServerException
	{
		try
		{
			logger.debug("Start getMusicFileList, userKey: " + userKey);
			List<Fileinfo> fileList = musicManager.getMusicFileList(userKey);
			logger.debug("End getMusicFileList, userKey: " + userKey);
			return fileList;
		}catch(Throwable t)
		{
			PBXServerException ex = new PBXServerException("A error ocurrs when getMusicFileList was executed!", t);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}
	}
}