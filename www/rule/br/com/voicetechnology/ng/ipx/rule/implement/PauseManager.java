package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.pbx.GroupPauseDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PauseDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.GroupPause;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pause;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PauseInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class PauseManager extends Manager
{
	private PauseDAO pauseDAO;
	private GroupPauseDAO groupPauseDAO;

	public PauseManager(String logger) throws DAOException 
	{
		super(logger);
		pauseDAO = dao.getDAO(PauseDAO.class);
		groupPauseDAO = dao.getDAO(GroupPauseDAO.class); 
	}

	public void save(PauseInfo pauseInfo) throws DAOException, ValidateObjectException
	{
		Pause pause = pauseInfo.getPause();
		if(pause.getKey() == null)
		{
			List<Integer> pauseList = pauseDAO.getPauseCodesByDomainKey(pauseInfo.getDomainKey());
			for(Integer tmp : pauseList)
				if (pause.getPauseCode().equals(tmp))
					throw new ValidateObjectException("Pause code already exist in system, please check and choose other code!", Pause.class, pause, ValidateType.DUPLICATED);
		}
		pauseDAO.save(pause);
	}
	
	public ReportResult<PauseInfo> findPauseInfo(Report<PauseInfo> report) throws Exception
	{		
		Long size = pauseDAO.getReportCount(report);
		List<Pause> PauseList = pauseDAO.getReportList(report);
		List<PauseInfo> PauseInfoList = getPauseInfoInfos(PauseList);			
		return new ReportResult<PauseInfo>(PauseInfoList, size);
	}
	
	private List<PauseInfo> getPauseInfoInfos(List<Pause> PauseList)
	{
		List<PauseInfo> pauseInfoList = new ArrayList<PauseInfo>();
		
		if(PauseList.size() > 0)
		{			
			for(Pause Pause: PauseList)
				pauseInfoList.add(new PauseInfo(Pause));
		}
		return pauseInfoList;
	}
	
	public PauseInfo getPauseInfo(Long PauseKey) throws DAOException
	{
		return new PauseInfo(pauseDAO.getByKey(PauseKey));
	}
	
	public PauseInfo getPauseInfoContext(PauseInfo pauseInfo) throws DAOException
	{
		List<Integer> pauseList = pauseDAO.getPauseCodesByDomainKey(pauseInfo.getDomainKey());
		pauseInfo.setPauseList(pauseList);
		return pauseInfo;
	}
	
	public List <GroupPause> getGroupPauseActiveByGroupKey(Long groupKey) throws DAOException
	{
		return groupPauseDAO.getGroupPauseListActiveByGroupKey(groupKey);
	}
	
	public List <GroupPause> getGroupPauseActiveByPbxuserkey(Long pbxuserKey) throws DAOException
	{
		return groupPauseDAO.getGroupPauseListActiveByPbxuserkey(pbxuserKey);
	}
	
	public GroupPause getGroupPauseByGroupKeyAndPauseCode(Long groupKey, Integer pauseCode) throws DAOException
	{
		return groupPauseDAO.getGroupPauseByGroupKeyAndPauseCode(groupKey, pauseCode);
	}
}
