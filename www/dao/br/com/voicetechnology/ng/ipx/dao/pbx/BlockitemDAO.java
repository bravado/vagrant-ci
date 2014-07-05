package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Blockitem;

public interface BlockitemDAO extends DAO<Blockitem>
{

	public List<Blockitem> getBlockitemList(Long key) throws DAOException;

}