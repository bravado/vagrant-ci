package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Configblock;

public interface ConfigblockDAO extends DAO<Configblock>
{

	public List<Configblock> getConfigblockByConfig(Long configKey) throws DAOException;

}
