package br.com.voicetechnology.ng.ipx.dao.pbx.ivr;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.ivr.IVROption;

public interface IVROptionDAO extends DAO<IVROption>
{
	public List<IVROption> getIVROptionListByIVR(Long ivrKey) throws DAOException;
	
	public List<IVROption> getIVROptionListByAddress(Long addressKey) throws DAOException;
}