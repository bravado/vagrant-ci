package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Park;
import br.com.voicetechnology.ng.ipx.pojo.info.CallParkInfo;

public interface ParkDAO extends DAO<Park>, ReportDAO<Park, CallParkInfo>
{
	public boolean isPositionBusy(String position, Long domainKey) throws DAOException;
	public boolean isPositionBusy(String position, String domain) throws DAOException;
	public void parkPosition(Park park) throws DAOException, ValidateObjectException;
	public void pickUpPosition(Park park) throws DAOException;
	public List<Park> getParkListByDomainKey(Long domainKey) throws DAOException;
	public Park getByPositionAndDomainKey(String position, Long domainKey) throws DAOException;
	
}
