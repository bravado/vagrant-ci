package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pause;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PauseInfo;

public interface PauseDAO extends DAO<Pause>, ReportDAO<Pause, PauseInfo>
{
	public List<Pause> getPauseByDomainKey(Long domainKey) throws DAOException;
	
	public List<Integer> getPauseCodesByDomainKey(Long domainKey) throws DAOException;
}
