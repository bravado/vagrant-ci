package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.DeleteDependenceException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.rule.QuotaException;
import br.com.voicetechnology.ng.ipx.commons.utils.regex.RegEx;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipTrunkAddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipTrunkDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SipTrunkRouteruleDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.RoleDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunk;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunkAddress;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunkRouterule;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Role;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.facets.pbx.DIDInPBXFacet;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.DIDInPBXInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.DIDInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PbxuserInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SipTrunkInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SipTrunkRouteRuleInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class SipTrunkManager extends Manager{

	private SipTrunkDAO sipTrunkDAO;
	private SipTrunkRouteruleDAO sipTrunkRouteRuleDAO;
	private RoleDAO roleDAO; 
	private ReportDAO<SipTrunk, SipTrunkInfo> reportSipTrunk;
	private ReportDAO<SipTrunkRouterule, SipTrunkRouteRuleInfo> reportSipTrunkRouteRule;
	private SipTrunkAddressDAO staDAO;
	private AddressDAO addDAO;
	
	private PbxuserManager puManager;
	
	public SipTrunkManager(Logger logger) throws DAOException {
		super(logger);
		sipTrunkDAO = dao.getDAO(SipTrunkDAO.class);
		roleDAO = dao.getDAO(RoleDAO.class);
		reportSipTrunk = dao.getDAO(SipTrunkDAO.class);
		puManager = new PbxuserManager(logger.getName());
		sipTrunkRouteRuleDAO = dao.getDAO(SipTrunkRouteruleDAO.class);
		reportSipTrunkRouteRule = dao.getDAO(SipTrunkRouteruleDAO.class);
		staDAO = dao.getDAO(SipTrunkAddressDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
	}
	
	public ReportResult<SipTrunkInfo> find(Report<SipTrunkInfo> report) throws Exception
	{		
		Long size = reportSipTrunk.getReportCount(report);
		List<SipTrunk> sipTrunkList = reportSipTrunk.getReportList(report);
		List<SipTrunkInfo> sipTrunkInfoList = getSipTrunkInfos(sipTrunkList);			
		return new ReportResult<SipTrunkInfo>(sipTrunkInfoList, size);
	}

	public List<SipTrunkInfo> getSipTrunkInfos(List<SipTrunk> sipTrunks) throws DAOException{
		List<SipTrunkInfo> sipTrunkInfos = new ArrayList<SipTrunkInfo>();
		for(SipTrunk sipTrunk: sipTrunks){
			PbxuserInfo info = puManager.getInfo(sipTrunk.getPbxuser());	
			SipTrunkInfo sipTrunkInfo = new SipTrunkInfo(sipTrunk);
			sipTrunkInfo.setPuInfo(info);
			sipTrunkInfos.add(sipTrunkInfo);
		}		
		return sipTrunkInfos;
	} 
	
	public void save(SipTrunkInfo info) throws DAOException, ValidateObjectException 
	{	
		SipTrunk sipTrunk = info.getSipTrunk();
		
		PbxuserInfo puInfo = info.getPuInfo();
		puInfo.getUser().setAgentUser(User.TYPE_SIPTRUNK);
		PbxuserManager puManager = new PbxuserManager(logger.getName());
		Config config = puInfo.getConfig();
		config.setDisableVoicemail(Config.VOICEMAIL_OFF);
		config.setEmailNotify(Config.EMAILNOTIFY_OFF);
		config.setAttachFile(Config.ATTACH_FILE_OFF);
		config.setForwardType(Config.FORWARD_TYPE_FROM); //TODO colocar configuracao na tela.
		config.setAllowedGroupForward(Config.NOT_ALLOWED_GROUPFORWARD);
		config.setAllowedKoushiKubun(Config.NOT_ALLOWED_KOUSHIKUBUN);
		config.setAllowedVoiceMail(Config.ALLOWED_VOICEMAIL);				
		
		Role defaultRole = roleDAO.getDefaultRole();
		
		puInfo.addRoleKey(defaultRole.getKey());
		puManager.save(puInfo);
		sipTrunk.setPbxuser(puInfo.getPbxuser());
		
		sipTrunkDAO.save(sipTrunk);	
		
		if(info.isDIDListChange())
			saveDIDInList(info.getDIDInKeyList(), sipTrunk.getKey(), new Long(1273), 0); //TODO ALTERAR E COLOCAR DOMAINKEY
		
	}
	
	private void saveDIDInList(List<Long> didList, Long siptrunkKey, Long domainKey, Integer lastIndex) throws DAOException, ValidateObjectException
	{
		Map<Long, SipTrunkAddress> mapSta = new HashMap<Long, SipTrunkAddress>();
		List<SipTrunkAddress> staList = staDAO.getSipTrunkAddressListBySipTrunkKey(siptrunkKey);
		
		for(SipTrunkAddress sta : staList)
			mapSta.put(sta.getAddressKey(), sta);
		
		for(Long didKey : didList)
		{
			SipTrunkAddress sta = mapSta.get(didKey);
			if(sta != null)
			{
				sta.setAddressKey(didKey);
				sta.setSipTrunkKey(siptrunkKey);
				staDAO.save(sta);
			}
			else
			{
				sta = new SipTrunkAddress(siptrunkKey, didKey);
				staDAO.save(sta);
			}
			mapSta.remove(didKey);
		}
		
		for(SipTrunkAddress sta : mapSta.values())
			staDAO.remove(sta);
	}
	
	public SipTrunkInfo getSipTrunkInfo(Long siptrunkKey, Long domainKey) throws DAOException, QuotaException
	{
		SipTrunk sipTrunk;
		SipTrunkInfo info = new SipTrunkInfo();
		if(siptrunkKey != null)//edit
		{
			sipTrunk = sipTrunkDAO.getByKey(siptrunkKey);
			info = new SipTrunkInfo(sipTrunk);
			info.setPuInfo(puManager.getPbxuserInfoByKey(sipTrunk.getPbxuserKey(), false));
		}
		else//add
		{
			sipTrunk = new SipTrunk();
			puManager.getPbxuserInfoContext(info.getPuInfo(), domainKey, false);			
		}
		
		makeDIDInAndOutList(domainKey, 0, info);
		
		return info;
	}
	
	private void makeDIDInAndOutList(Long domainKey, Integer lastIndex, SipTrunkInfo info) throws DAOException
	{
		List<Address> didOutList = staDAO.getSipTrunkAddressDidListOut(domainKey, lastIndex, DIDInPBXInfo.RESULTS_PER_CONSULT, false);
		for(Address add : didOutList)
				info.addDidOutList(new DIDInfo(add, add.getAddress(), false));
		
		if(info.getSipTrunk() != null && info.getSipTrunk().getKey() != null)
		{
			List<Address> didInList = staDAO.getSipTrunkAddressDidListIn(info.getSipTrunk().getKey());
			for(Address add : didInList)
				info.addDidInList(new DIDInfo(add, add.getAddress(), false));
		}
	}

	public void deleteSipTrunks(List<Long> keysList) throws DAOException, ValidateObjectException, DeleteDependenceException{
		for(Long key : keysList){
			delete(key);			
		}
	}
	
	public void delete(Long key) throws DAOException, ValidateObjectException, DeleteDependenceException{
		SipTrunk sipTrunk = sipTrunkDAO.getByKey(key);
		puManager.deletePbxuser(sipTrunk.getPbxuserKey(), false);
		sipTrunkDAO.remove(sipTrunk);		
	}
	
	public List<SipTrunk> getRegisterSipTrunks() throws DAOException
	{
		return sipTrunkDAO.getRegisterSipTrunkList();
	}
	
	public List<SipTrunk> getSipTrunkListByDomainKey(Long domainKey) throws DAOException
	{
		return sipTrunkDAO.getSipTrunksByDomain(domainKey);
	}
	
	/*****************************************************/
	/** SipTrunkRouteRule								**/
	/*****************************************************/
	
	public void deleteSipTrunkRouteRuleList(List<Long> keyList) throws DAOException
	{
		for(Long key : keyList){
			deleteSipTrunkRouteRule(key);			
		}
	}
	
	public void deleteSipTrunkRouteRule(Long key) throws DAOException
	{
		sipTrunkRouteRuleDAO.remove(sipTrunkRouteRuleDAO.getByKey(key));
	}
	
	public void getSipTrunkRouteRuleInfoContext(SipTrunkRouteRuleInfo info) throws DAOException
	{
		info.setSipTrunkList(generateSipTrunkDuoList(getSipTrunkListByDomainKey(info.getDomainKey())));
	}
	
	public SipTrunkRouteRuleInfo getSipTrunkRouteRuleInfo(Long key) throws DAOException
	{
		SipTrunkRouteRuleInfo info = new SipTrunkRouteRuleInfo();
		info.setSipTrunkRouteRule(sipTrunkRouteRuleDAO.getByKey(key));
		
		return info;
	}
	
	public void save(SipTrunkRouteRuleInfo info) throws DAOException, ValidateObjectException
	{
		SipTrunkRouterule strr = info.getSipTrunkRouteRule();
		SipTrunk sipTrunk = sipTrunkDAO.getByKey(info.getSipTrunkKey());
		strr.setSipTrunk(sipTrunk);
		strr.setDescription(info.getDescription());
		strr.setPattern(info.getPattern());
		strr.setPriority(info.getPriority());
		strr.setRegex(RegEx.getRegEx(info.getPattern()));
		
		sipTrunkRouteRuleDAO.save(strr);
	}
	
	public ReportResult<SipTrunkRouteRuleInfo> findSipTrunkRouteRuleInfo(Report<SipTrunkRouteRuleInfo> report) throws Exception
	{		
		Long size = reportSipTrunkRouteRule.getReportCount(report);
		List<SipTrunkRouterule> sipTrunkRouteRuleList = reportSipTrunkRouteRule.getReportList(report);
		List<SipTrunkRouteRuleInfo> sipTrunkRouteRuleInfoList = getSipTrunkRouteRuleInfos(sipTrunkRouteRuleList);			
		return new ReportResult<SipTrunkRouteRuleInfo>(sipTrunkRouteRuleInfoList, size);
	}
	
	public List<SipTrunkRouteRuleInfo> getSipTrunkRouteRuleInfos(List<SipTrunkRouterule> sipTrunkRouteRuleList) throws DAOException
	{
		List<SipTrunkRouteRuleInfo> sipTrunkRouteRuleInfoList = new ArrayList<SipTrunkRouteRuleInfo>();
		
		if(sipTrunkRouteRuleList.size() > 0)
		{
			List<SipTrunk> sipTrunkList = sipTrunkDAO.getSipTrunksByDomain(sipTrunkRouteRuleList.get(0).getDomainKey());
						
			for(SipTrunkRouterule sipTrunkRouteRule: sipTrunkRouteRuleList){
				sipTrunkRouteRuleInfoList.add(new SipTrunkRouteRuleInfo(sipTrunkRouteRule, generateSipTrunkDuoList(sipTrunkList)));
			}
		}
		return sipTrunkRouteRuleInfoList;
	} 	
	
	private List<Duo<Long, String>> generateSipTrunkDuoList(List<SipTrunk> list)
	{
		List<Duo<Long,String>> siptrunkList = new ArrayList<Duo<Long,String>>();
		for(SipTrunk st: list)
		{
			siptrunkList.add(new Duo<Long, String>(st.getKey(), st.getPbxuser().getUser().getUsername()));
		}
		
		return siptrunkList;
	}

}
