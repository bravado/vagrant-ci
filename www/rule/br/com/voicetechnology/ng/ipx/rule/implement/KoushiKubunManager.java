package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateType;
import br.com.voicetechnology.ng.ipx.dao.pbx.ActivecallDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.KoushikubunDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Activecall;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Koushikubun;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbx;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.ActivecallInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.KoushiKubunItemInfo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.KoushikubunInfo;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;
import br.com.voicetechnology.ng.ipx.pojo.report.ReportResult;
import br.com.voicetechnology.ng.ipx.pojo.webreport.wcc.settings.KoushiKubunWebReport;

public class KoushiKubunManager extends Manager
{
	private KoushikubunDAO  koushiKubunDAO;
	private PbxDAO pbxDAO;//dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1

	public KoushiKubunManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		koushiKubunDAO = dao.getDAO(KoushikubunDAO.class);
		pbxDAO = dao.getDAO(PbxDAO.class);//dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1
	}

	public KoushikubunInfo getKoushikubunInfo(Long pbxuserKey) throws DAOException
	{
		//inicio --> dnakamashi - bug #5489 - 3.0.2 RC12 patch 4 
		List<Koushikubun> kkList = koushiKubunDAO.getKoushiKubunListByPbxuserKey(pbxuserKey, Koushikubun.DEFINE_ACTIVE);
		//fim--> dnakamashi - bug #5489 - 3.0.2 RC12 patch 4
		
		KoushikubunInfo kkInfo =  new KoushikubunInfo();
		
		for(Koushikubun kk : kkList)
		{
			kkInfo.addKoushiKubunItemInfo(new KoushiKubunItemInfo(kk));
		}
		
		return kkInfo;
	}
	//inicio issue 5949 tveiga rc12 patch 5 
	public ReportResult<KoushikubunInfo> findKoushiKubun(Report<KoushiKubunItemInfo> info) throws DAOException
	{
		ReportDAO<Koushikubun, KoushiKubunItemInfo> kkReport = dao.getReportDAO(KoushikubunDAO.class);
				
		List<Koushikubun> kkList = kkReport.getReportList(info);
		Long size = kkReport.getReportCount(info);
		
		KoushikubunInfo kkInfo =  new KoushikubunInfo();
		
		for(Koushikubun kk : kkList)
		{			
			if(kk.getPbxuser().getConfig().getAllowedKoushiKubun() == Config.ALLOWED_KOUSHIKUBUN)
				kkInfo.addKoushiKubunItemInfo(new KoushiKubunItemInfo(kk));
		}
		List<KoushikubunInfo> kkInfoList = new ArrayList<KoushikubunInfo>();
		kkInfoList.add(kkInfo);
		
		return new ReportResult<KoushikubunInfo>(kkInfoList, size);
		
	}
    //fim issue 5949 tveiga rc12 patch 5 

	//TODO verificar como resolver o problema do save. Sem criar um novo objeto KushiKubun
	public void saveKoushikubunInfo(KoushikubunInfo kkInfo) throws DAOException, ValidateObjectException
	{
		List<Koushikubun> kkList = koushiKubunDAO.getKoushiKubunListByPbxuserKey(kkInfo.getPbxuserKey());
		
		//inicio --> dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1
		List<Koushikubun> domainKoushikubunList = koushiKubunDAO.getListByDomain(kkInfo.getDomainkey());
		//end --> dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1			
		
		for(Koushikubun kkItem : kkList)
		{
			koushiKubunDAO.remove(kkItem);
		}
		kkList = null;
		
		for(KoushiKubunItemInfo kkItem : kkInfo.getKoushiKubunItemInfoList())
		{
			if (kkItem != null){ // tveiga issue 5293 RC12 patch 5
			Koushikubun kb = new Koushikubun();
			kb.setAddress(kkItem.getAddress());
			kb.setDescription(kkItem.getDescription());
			kb.setPbxuserKey(kkItem.getPbxuserKey());
			
			//	inicio --> dnakamashi - bug #5489 - 3.0.2 RC12 patch 4 
			kb.setActive(Koushikubun.DEFINE_ACTIVE);
			//fim --> dnakamashi - bug #5489 - 3.0.2 RC12 patch 4
			
			koushiKubunDAO.save(kb);
			}
		}
		
		//inicio --> dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1
		List<Koushikubun> newDomainKoushikubunList = koushiKubunDAO.getListByDomain(kkInfo.getDomainkey());
		Pbx pbx = pbxDAO.getPbxByDomain(kkInfo.getDomainkey());
		
		if(!validateKoushikubunQuota(domainKoushikubunList.size(), newDomainKoushikubunList.size(), pbx.getMaxUser()))
			throw new ValidateObjectException("Koushibun quota exceeded for this pbx!", Koushikubun.class, kkInfo, ValidateType.NUMBER);
		//fim --> dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1
	}
	
	//inicio --> dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1
	private boolean validateKoushikubunQuota(Integer oldDomainKKListSize, Integer newDomainKKListSize, Integer pbxMaxUserQuota)
	{		
		if(oldDomainKKListSize > pbxMaxUserQuota*2)
		{
			if(newDomainKKListSize > oldDomainKKListSize)
				return false;
			else
				return true;
		}
		else if(newDomainKKListSize > pbxMaxUserQuota*2)
			return false;
		return true;
	}
	//end --> dnakamashi - correção do bug #6831 - 3.0.5RC6.5.1
}