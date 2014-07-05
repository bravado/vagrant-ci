/**
 * 
 */
package br.com.voicetechnology.ng.ipx.ejb.facade;

import java.io.IOException;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.ejb.CDRException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Cdr;
import br.com.voicetechnology.ng.ipx.rule.implement.CallCDRManager;
import br.com.voicetechnology.ng.ipx.rule.implement.CallLogManager;

/**
 * 
 * <!-- begin-user-doc --> A generated session bean <!-- end-user-doc --> * <!--
 * begin-xdoclet-definition -->
 * 
 * @ejb.bean name="CDRFacade" description="An EJB named CDRFacade"
 *           display-name="CDRFacade"
 *           jndi-name="br/com/voicetechnology/ng/ejb/facade/CDRFacade"
 *           type="Stateless" transaction-type="Container"
 * 
 * <!-- end-xdoclet-definition -->
 * @generated
 */

public abstract class CDRFacadeBean implements javax.ejb.SessionBean
{
 
	private Logger logger;

	private CallCDRManager cdrManager;

	private CallLogManager callLogManager;

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
			logger.info("Creating instance of " + this.getClass().getName());
			cdrManager = new CallCDRManager(logger.getName());
			callLogManager = new CallLogManager(logger.getName());
		} catch (Exception e)
		{
			CDRException ex = new CDRException("Error in CDRFacade construtor!", e);
			logger.error(ex.getLocalizedMessage());
			throw ex;
		}

	}

	/**
	 * Gera o CDR de chamada
	 * 
	 * @ejb.interface-method view-type="remote"
	 * @generated
	 * @ejb.transaction type="Required"
	 * @throws CDRException
	 */
	public void saveCallCDR(Cdr cdr)
	{
		cdrManager.saveCallCDR(cdr);
	}
}