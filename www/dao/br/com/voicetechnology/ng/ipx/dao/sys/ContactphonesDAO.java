package br.com.voicetechnology.ng.ipx.dao.sys;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contactphones;

public interface ContactphonesDAO extends DAO<Contactphones> 
{
	public List<Contactphones> getContactphonesListByContact(Long contactKey) throws DAOException;

}
