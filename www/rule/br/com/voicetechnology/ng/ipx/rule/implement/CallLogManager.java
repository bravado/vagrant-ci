package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.voicetechnology.ipx.mail.formatter.CalllogCSVMailFormatter;
import br.com.voicetechnology.ipx.mail.server.MailServer;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.CalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxuserDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.RecordFileDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.SystemcalllogDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.TerminalDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.UsergroupDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.ContactDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.DomainDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.GroupfileDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserfileDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.CallType;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Calllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Group;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Systemcalllog;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Contact;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Groupfile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.RecordFile;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Userfile;
import br.com.voicetechnology.ng.ipx.pojo.info.CallLogCSVMail;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.CalllogInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.SystemCalllogInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.pojo.webreport.wca.view.DWRSystemCalllogInfo;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class CallLogManager extends Manager
{
	private CalllogDAO clDAO;
	private PbxuserDAO puDAO;
	private UserfileDAO ufDAO;
	private AddressDAO addDAO;
	private DomainDAO domainDAO;
	private ContactDAO contactDAO;
	private TerminalDAO terminalDAO;
	private UsergroupDAO ugDAO;
	private GroupfileDAO gfDAO;
	private UserDAO uDAO;
	private SystemcalllogDAO systemCallLogDAO;
	private ReportDAO<Calllog, CalllogInfo> reportCalllogDAO;
	private ReportDAO<Systemcalllog, SystemCalllogInfo> reportSystemCalllogDAO;
	private RecordFileDAO recordFileDAO;
	
	public CallLogManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		clDAO = dao.getDAO(CalllogDAO.class);
		puDAO = dao.getDAO(PbxuserDAO.class);
		ufDAO = dao.getDAO(UserfileDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		terminalDAO = dao.getDAO(TerminalDAO.class);
		contactDAO = dao.getDAO(ContactDAO.class);
		ugDAO = dao.getDAO(UsergroupDAO.class);
		domainDAO = dao.getDAO(DomainDAO.class);
		gfDAO = dao.getDAO(GroupfileDAO.class);
		systemCallLogDAO = dao.getDAO(SystemcalllogDAO.class);
		reportCalllogDAO = dao.getReportDAO(CalllogDAO.class);
		reportSystemCalllogDAO = dao.getReportDAO(SystemcalllogDAO.class);
		recordFileDAO = dao.getDAO(RecordFileDAO.class);
		uDAO = dao.getDAO(UserDAO.class);
	}

	public ReportResult findCalllogs(Report<CalllogInfo> report) throws DAOException, ValidateObjectException
	{
		Long size = report.exportAsCSV() ? 0 :reportCalllogDAO.getReportCount(report);
		List<Calllog> calllogList = reportCalllogDAO.getReportList(report);
		List<CalllogInfo> calllogInfoList = new ArrayList<CalllogInfo>();
		boolean onlyVoiceMail = report.getInfo().isVoiceMailMessage();
		Long domainKey = report.getInfo().getDomainKey();
		Domain domain = domainKey == null ? domainDAO.getRootDomain() : domainDAO.getByKey(domainKey);
		List<Duo<Long, String>> domainList = !report.getInfo().getTopTenCalls() ? domainDAO.getDomainsByRootDomain(domain.getRootKey() != null ? domain.getRootKey() : domain.getKey()) : null;
						
		for(Calllog calllog : calllogList)
		{
			
			//jluhetta - Substuir somente tags para a query que busca emnsagens de grupo  mudança para buscar group file pelo calllog key (Bug ID: 5191)
			Userfile userFile = ufDAO.getUserFileByCallLog(calllog.getKey());
			if(userFile != null)
				calllog.setUserFile(userFile);
			else
			{
				Groupfile gf = gfDAO.getGroupfileByCalllog(calllog.getKey());
				if(gf != null)
					calllog.setGroupfile(gf);
			}
			//jluhetta - Fim - (Bug ID: 5191)
			
			String domainName = calllog.getPbxuser().getUser().getDomain().getDomain();
			String username = calllog.getPbxuser().getUser().getUsername();
			String anotherLegInfo = null;
			boolean isToVoicemailUser = false;
			boolean isToIVRUser = false;
			if(calllog.getAnotherPbxuserKey() != null)
			{
				Pbxuser anotherpbxuser =  puDAO.getByKey(calllog.getAnotherPbxuserKey());
				if(anotherpbxuser != null)
					anotherLegInfo = anotherpbxuser.getUser().getName() != null && anotherpbxuser.getUser().getName() != "" ? anotherpbxuser.getUser().getName() : anotherpbxuser.getUser().getKanjiName();
				if(anotherpbxuser.getUser().getAgentUser() == User.TYPE_VOICEMAIL)
					isToVoicemailUser = true;
				else if(anotherpbxuser.getUser().getAgentUser() == User.TYPE_IVR)
					isToIVRUser = true;
			} else if(calllog.getContactKey()!= null)
			{
				Contact contact = contactDAO.getByKey(calllog.getContactKey());
				if(contact != null)
					anotherLegInfo = contact.getName() != null && contact.getName() != "" ? contact.getName() : contact.getKanjiName();
			}
			
			RecordFile recFile = recordFileDAO.getRecordFileByCallLogKey(calllog.getKey());
			CalllogInfo info = new CalllogInfo(calllog, username, domainName, anotherLegInfo, domainList, isToVoicemailUser, isToIVRUser, recFile);
			info.setDID(getDID(calllog.getPbxuser(), calllog.getMyAddress()));
			calllogInfoList.add(info);
		}
		return new ReportResult<CalllogInfo>(calllogInfoList, size);
	}	
	
	private String getDID(Pbxuser pu, String myAddress) throws DAOException
	{
		if (myAddress != null)
			return myAddress;
		if(pu == null)
			return null;
		
		List<Address> didList = addDAO.getDIDListByPbxuser(pu.getKey());		
		if(didList.size() > 0)
			return didList.get(0).getAddress();

		User u = pu.getUser();
		Address trunkLine = addDAO.getDefaultAddressByDomainKey(u.getDomainKey());		
		return trunkLine.getAddress();
		
	}
	
	public Calllog createCallLog(Long ownerKey, boolean isOwner, SipAddressParser sipFrom, SipAddressParser sipTo, String display, String callID, String userAgent, int callStatus, Long anotherPbxuserKey, String myAddress) throws Exception
	{
		int callType;
		SipAddressParser sipAddress;
		if(isOwner)
		{
			callType = CallType.CALL_DIALED;
			sipAddress = sipTo;
		}else
		{
			callType = CallType.CALL_RECEIVED;
			sipAddress = sipFrom;
		}
		
		Pbxuser pu = puDAO.getByKey(ownerKey);
		if(pu != null && pu.getUser().getAgentUser().intValue() == User.TYPE_TERMINAL)
			pu = terminalDAO.getAssociatedPbxuserByTerminalPbxuserKey(ownerKey);
		if (pu == null)
			return null;
		
		//se o from e o to são do mesmo domínio o address do call log fica sendo o sipID, senão o address fica como estava
		//jluchetta - mudanca da condição verificando se o não é from da chamada (!isOwner), para que no callog do originador mantenha o numero discado
		if (sipFrom.getDomain().equals(sipTo.getDomain()) && !isOwner)
		{
			Pbxuser puAux = puDAO.getPbxuserByAddressAndDomain(sipAddress.getExtension(), sipAddress.getDomain());
			if (puAux != null)
			{
				Address sipID = addDAO.getSipIDByPbxuser(puAux.getKey());
				if (sipID != null)
					sipAddress.setExtension(sipID.getAddress());
			}
		}
		String address = getPSTNNumber(isOwner ? sipTo.getExtension() : sipFrom.getExtension(), false); 
		Contact contact = contactDAO.getContactByUserAndAddress(pu.getUserKey(), address);
		if(contact == null)
			contact = contactDAO.getPublicContact(pu.getUser().getDomainKey(), address);
		Long contactKey = contact != null ? contact.getKey() : null;
		Calllog calllog = createCalllog(pu, sipAddress.getAddress(), display, callType, callStatus, callID, userAgent, null, anotherPbxuserKey, contactKey, myAddress);
		clDAO.save(calllog);
		return calllog;
	}

	private Calllog createCalllog(Pbxuser pbxuser, String address, String display, int callType, int status, String callID, String userAgent, Group group, Long anotherPbxuserKey, Long contactKey, String myAddress) throws DAOException, ValidateObjectException
    {
        Calllog calllog = new Calllog();
        calllog.setPbxuser(pbxuser);
        calllog.setFromdate(Calendar.getInstance());
        calllog.setPbxuserKey(pbxuser.getKey());
        calllog.setAddress(cleanCallLogAddress(address));
        calllog.setDisplay(display);
        calllog.setCalltype(new Integer(callType));
        calllog.setStatus(new Integer(status));
        calllog.setCallID(callID);
        calllog.setUserAgent(userAgent);
        calllog.setContactKey(contactKey);
        calllog.setGroup(group);
        calllog.setMyAddress(myAddress);
        calllog.setActive(Calllog.DEFINE_ACTIVE);
        if(contactKey == null)
        	calllog.setAnotherPbxuserKey(anotherPbxuserKey);
        return calllog;
    }
	
	public boolean isToVoiceMail(Long domainKey, SipAddressParser sipTo) throws DAOException
	{
		Address add = addDAO.getVoicemailAddress(domainKey);
		return add.getAddress().equals(sipTo.getExtension()) || sipTo.getExtension().equals(User.VOICEMAIL_NAME);
	}
	
	public void saveCallLog(Collection<Calllog> logs) throws DAOException, ValidateObjectException
	{
		for(Calllog log : logs)
			saveCallLog(log);
	}
	
	public void saveCallLog(Calllog log) throws DAOException, ValidateObjectException
	{
		log.setAddress(cleanCallLogAddress(log.getAddress()));
		SipAddressParser sipAddress = new SipAddressParser(log.getAddress());
		log.setDisplay(sipAddress.getExtension());
		log.setActive(Calllog.DEFINE_ACTIVE);
		clDAO.save(log);
		
		Pbxuser pu = terminalDAO.getAssociatedPbxuserByTerminalPbxuserKey(log.getPbxuserKey());
		if(pu != null)
		{
			Calllog logUser = createCalllog(pu, log.getAddress(), log.getDisplay(), log.getCalltype(), log.getStatus(), log.getCallID(), log.getUserAgent(), log.getGroup(), log.getAnotherPbxuserKey(), log.getContactKey(), log.getMyAddress());
			logUser.setUntildate(log.getUntildate());
			clDAO.save(logUser);
			updatePbxuserCalllogKey(logUser);
		}
		
		updatePbxuserCalllogKey(log);
		
		if (log.isGroup())
		{
			Usergroup ug = ugDAO.getUsergroupByPbxuserAndGroup(log.getPbxuserKey(), log.getGroup().getKey());
			if(ug != null)
			{
			   //jluchetta - Início - Correção do problema de round robin - Correção para versão 3.0.3 - (BugID: 5957)
				if(log.isChangeUserGroupStartTime())
					ug.setStartLastCall((Calendar) log.getFromdate().clone());
				///jluchetta - Fim -Correção do problema de round robin - Correção para versão 3.0.3 - (BugID: 5957)
				if (log.getUntildate() != null)
					ug.setLastCall((Calendar) log.getUntildate().clone());
				ugDAO.save(ug);
			}
		}
	}
	
	public Systemcalllog saveSystemCalllog(Systemcalllog systemCalllog) throws DAOException, ValidateObjectException
	{
		systemCallLogDAO.save(systemCalllog);
		return systemCalllog;
	}
	
	
	public void removeCalllog(Calllog log) throws DAOException
	{		
		RecordFile rfile = recordFileDAO.getRecordFileByCallLogKey(log.getKey());
		if(rfile != null)
			recordFileDAO.remove(rfile);
		clDAO.remove(log);
	}
	
	private String getExtension(String address) 
	{
		SipAddressParser sipAddress = new SipAddressParser(address);
		return sipAddress.getExtension();
	}
	
	private String cleanCallLogAddress(String address) 
	{
		SipAddressParser sipAddress = new SipAddressParser(address);
		address = sipAddress.getExtension() + "@" + sipAddress.getDomain();
		return address;
	}

	private void updatePbxuserCalllogKey(Calllog log) throws DAOException, ValidateObjectException
	{
		if (log.getCalltype() == CallType.CALL_DIALED)
		{
			log.getPbxuser().setCalllogKey(log.getKey());
			Pbxuser user = puDAO.getByKey(log.getPbxuserKey());
			user.setCalllogKey(log.getKey());
			puDAO.save(user);
		}
	}
	
	/*
	 * ***********************************************************************
	 * ************************ System Call Log ******************************
	 * ***********************************************************************
	 */
	
	public ReportResult findSystemCalllog(Report<SystemCalllogInfo> info) throws DAOException
	{
		Long size = reportSystemCalllogDAO.getReportCount(info);
		List<Systemcalllog> systemCalllogList = reportSystemCalllogDAO.getReportList(info);
		List<SystemCalllogInfo> systemCalllogInfoList = new ArrayList<SystemCalllogInfo>(systemCalllogList.size());
		for(Systemcalllog systemCalllog : systemCalllogList)
			systemCalllogInfoList.add(new SystemCalllogInfo(systemCalllog));
		return new ReportResult<SystemCalllogInfo>(systemCalllogInfoList, size);
	}
	
	public SystemCalllogInfo viewSystemCalllogDetails(Long systemCalllogKey) throws DAOException
	{
		List<Systemcalllog> relatedCalllogList = new ArrayList<Systemcalllog>();
		Systemcalllog relatedCalllog = null;
		Set<String> callIDList = new HashSet<String>();
				
		Systemcalllog systemcalllog = systemCallLogDAO.getByKey(systemCalllogKey);
		if(systemcalllog == null)
			return null;		
		relatedCalllogList.add(systemcalllog);
		addToMoreDetailsList(relatedCalllogList, getRelatedCalllogs(systemCalllogKey));	
			
		for(int i = 0; i < relatedCalllogList.size(); i++)
		{
			relatedCalllog = relatedCalllogList.get(i);			
			if(relatedCalllog.getMovedFromCalllogKey() != null || relatedCalllog.getMovedToCalllogKey() != null)
			{	
				callIDList.add(relatedCalllog.getCallID());
				addToMoreDetailsList(relatedCalllogList, getRelatedCalllogs(relatedCalllog.getMovedFromCalllogKey()));
				addToMoreDetailsList(relatedCalllogList, getRelatedCalllogs(relatedCalllog.getMovedToCalllogKey()));
			}
			else if((relatedCalllog.getMovedToCalllogKey() == null) && (relatedCalllog.getMovedFromCalllogKey() == null))
				callIDList.add(relatedCalllog.getCallID());				
		}		
		List<Systemcalllog> moreDetailsList = systemCallLogDAO.getRelatedCallLogListByCallIDs(callIDList.toArray(new String[callIDList.size()]));		
		List<DWRSystemCalllogInfo> dwrMoreDetailsList = makeDWRSystemCalllogInfoList(moreDetailsList);

		return new SystemCalllogInfo(systemcalllog, dwrMoreDetailsList);
	}
	
	private List<Systemcalllog> getRelatedCalllogs(Long systemCalllogKey) throws DAOException
	{		
		List<Systemcalllog> relatedCalllogList = new ArrayList<Systemcalllog>();
		if(systemCalllogKey != null)
		{	
			Systemcalllog systemCalllog = systemCallLogDAO.getByKey(systemCalllogKey);
			if(systemCalllog != null)
				relatedCalllogList = systemCallLogDAO.getListByCallSequenceAndCallID(systemCalllog.getCallSequence(), systemCalllog.getCallID());
		}
		return relatedCalllogList;
	}		

	private List<DWRSystemCalllogInfo> makeDWRSystemCalllogInfoList(List<Systemcalllog> systemCalllogList)
	{
		List<DWRSystemCalllogInfo> dwrSystemCalllogList = new ArrayList<DWRSystemCalllogInfo>();
		for(Systemcalllog systemCalllog : systemCalllogList)
		{
			SystemCalllogInfo systemCalllogInfo = new SystemCalllogInfo(systemCalllog);
			dwrSystemCalllogList.add(new DWRSystemCalllogInfo(systemCalllogInfo));
		}
		return dwrSystemCalllogList;
	}
	
	private void addToMoreDetailsList(List<Systemcalllog> moreDetailsList, List<Systemcalllog> relatedCalllogList)
	{
		for(Systemcalllog systemcalllog : relatedCalllogList)
			if(!findExist(systemcalllog.getKey(), moreDetailsList))				
				moreDetailsList.add(systemcalllog);			
	}
	
	private boolean findExist(Long calllogKey, List<Systemcalllog> relatedList)
	{
		for(Systemcalllog sysCalllog : relatedList)
			if(sysCalllog.getKey().equals(calllogKey))
				return true;
		return false;
	}	
	
	public void sendCalllogCSVEmail(CallLogCSVMail mail) throws Exception
	{
		CalllogCSVMailFormatter formatter = new CalllogCSVMailFormatter();
		formatter.addParam("email", mail.getTo());
		formatter.addParam("file", mail.getFile());
		formatter.addParam("content", mail.getContent());
		formatter.addParam("subject", mail.getSubject());
		MailServer.getInstance().send(formatter);
	}
}
