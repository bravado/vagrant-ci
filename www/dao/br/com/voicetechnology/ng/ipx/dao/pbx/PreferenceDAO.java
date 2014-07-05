package br.com.voicetechnology.ng.ipx.dao.pbx;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Preference;

public interface PreferenceDAO extends DAO<Preference>
{

	Preference getPreferenceByUser(Long userKey) throws DAOException;

	Preference getPreferenceByPbxuser(Long pbxuserKey) throws DAOException;
	
	Preference getPreferenceByUsernameAndDomain(String username, String domain) throws DAOException;

}
