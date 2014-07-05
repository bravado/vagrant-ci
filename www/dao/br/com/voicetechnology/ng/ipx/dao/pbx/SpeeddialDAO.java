package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Speeddial;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SpeeddialInfo;

public interface SpeeddialDAO extends DAO<Speeddial>, ReportDAO<Speeddial, SpeeddialInfo>
{

	public Speeddial getSpeeddial(String position, String domain) throws DAOException;
	
	public List<Speeddial> getByPbx(Long pbxKey)  throws DAOException;	
       
}
