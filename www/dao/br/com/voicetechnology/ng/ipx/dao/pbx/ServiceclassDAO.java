package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ClassOfServiceInfo;

public interface ServiceclassDAO extends DAO<Serviceclass>, ReportDAO<Serviceclass, ClassOfServiceInfo>
{

	public List<Duo<Long, String>> getServiceclassKeyAndName(Long pbxKey) throws DAOException;

	/**
	 * Load Serviceclass and Config.
	 * @throws DAOException
	 */
	public Serviceclass getServiceclassFull(Long serviceclassKey) throws DAOException;

	public List<Pbxuser> getPbxuserListInServiceclass(Long serviclassKey) throws DAOException;

	public Serviceclass getDefaultServiceclass(Long domainKey) throws DAOException;
	
	public Serviceclass getCentrexDefaultServiceclass() throws DAOException;

	public List<Serviceclass> getServiceClassInPBX(Long pbxKey, boolean allowDefaultServiceClass) throws DAOException;
	
	public Serviceclass getServiceClassByNameAndDomainName(String classOfServiceName, String domainName) throws DAOException;
	
	public List<Duo<Long, String>> getCentrexServiceclassKeyAndName() throws DAOException;
}
