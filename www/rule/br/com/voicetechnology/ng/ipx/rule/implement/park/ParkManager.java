package br.com.voicetechnology.ng.ipx.rule.implement.park;


import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.file.FileUtils;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.ParkDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.FileinfoDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.db.park.ParkInfo;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Park;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.info.CallParkInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class ParkManager extends Manager
{
	private final int CALL_ID = 0;
	private final int OWNER = 1;
	private final int POSITION = 2;
	
	private UserDAO uDAO;
	private ParkDAO parkDAO;
	private ReportDAO<Park, CallParkInfo> reportCallPark;
	private FileinfoDAO fileDAO;
	
	public ParkManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		uDAO = dao.getDAO(UserDAO.class);
		parkDAO = dao.getDAO(ParkDAO.class);
		reportCallPark = dao.getDAO(ParkDAO.class);
		fileDAO = dao.getDAO(FileinfoDAO.class);
	}

	public List<User> getParkServerListByDomain(String domain) throws DAOException
	{
		return uDAO.getParkServerListByDomain(domain);
	}	
	
	public User getParkServerByDomain(String domain) throws DAOException
	{
		return uDAO.getParkServerByDomain(domain);
	}
	
	public Park getByPositionAndDomainKey(String position, Long domainKey) throws DAOException
	{
		return parkDAO.getByPositionAndDomainKey(position, domainKey);
	}
	
	//TODO remover retorn booleano.
	public boolean isPositionBusy(String position, Long domainKey) throws DAOException
	{
		return parkDAO.isPositionBusy(position, domainKey);
	}
	
	public void parkPosition(Park park) throws DAOException, ValidateObjectException
	{
		parkDAO.parkPosition(park);
	}
	
	public void pickUpPosition(String position, Long domainKey) throws DAOException
	{
		Park park = parkDAO.getByPositionAndDomainKey(position, domainKey);
		parkDAO.pickUpPosition(park);
	}
	
	public void removeAllParkedConnections(Long domainKey) throws DAOException
	{
		List<Park> list = parkDAO.getParkListByDomainKey(domainKey);
		
		for(Park park: list)
		{
			parkDAO.remove(park);
		}
	}
	
	public ParkInfo getParkInfo(String from, String to) throws DAOException
	{
		SipAddressParser parked = new SipAddressParser(from);
		int begin = to.indexOf(":");
		int end = to.lastIndexOf("@");
		String extension = to.substring(begin+1, end);
		SipAddressParser parker = new SipAddressParser(extension, parked.getDomain());

		String position = null;
		String callID = null;
		// campo to está no formato callid##parkUserName#owner#position@domain, onde owner é a extension de quem 
		// estacionou a chamada e position é a posição de park a ser utilizada e referedSipcallId é a sipCallId da perna em park.
		if(parker.getExtension().matches(".*[^#]#[^#].*"))
		{
			String[] tmp = parker.getExtension().split("##");
			callID = tmp[CALL_ID];
			tmp = tmp[1].split("#");
			parker.setExtension(tmp[OWNER]);
			position = tmp[POSITION];
			
//			String[] tmp = parker.getExtension().split("#");
//			callID = tmp[CALL_ID].split("##")[CALL_ID];
//			parker.setExtension(tmp[OWNER]);
//			position = tmp[POSITION];				
		}

		String parkMusicPath = fileDAO.getParkFilePathByDomain(parked.getDomain());
		if(parkMusicPath == null)
			parkMusicPath = FileUtils.getRelativeBasePath() + IPXProperties.getProperty(IPXPropertiesType.FILES_DEFAULT_MUSIC_ON_HOLD);
		
		return new ParkInfo(position, parked, parker, parked.getDomain(), callID, parkMusicPath);
	}
	
	/*
	 * ###############################################################
	 * 			In�cio dos M�todos utilizados pela WEB
	 * ###############################################################
	 * 
	 */
	
	public ReportResult findCallPark(Report<CallParkInfo> info) throws DAOException
	{
		Long size = reportCallPark.getReportCount(info);
		List<Park> callParkList = reportCallPark.getReportList(info);
		List<CallParkInfo> callParkInfoList = new ArrayList<CallParkInfo>(callParkList.size());
		for(Park park : callParkList)
			callParkInfoList.add(new CallParkInfo(park));
		return new ReportResult<CallParkInfo>(callParkInfoList, size);
	}
	
	public void deleteCallParks(List<Long> callParkKeyList) throws DAOException
	{
		for(Long key : callParkKeyList)
			this.deleteCallPark(key);
	}
	
	private void deleteCallPark(Long callParkKey) throws DAOException
	{
		Park park = parkDAO.getByKey(callParkKey);
		if(park != null)
			parkDAO.remove(park);
	}

	/*
	 * ###############################################################
	 * 			Fim dos M�todos utilizados pela WEB
	 * ###############################################################
	 * 
	 */
}