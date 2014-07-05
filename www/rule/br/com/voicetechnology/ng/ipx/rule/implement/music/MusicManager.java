package br.com.voicetechnology.ng.ipx.rule.implement.music;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.file.FileUtils;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.sys.UserDAO;
import br.com.voicetechnology.ng.ipx.dao.sys.UserfileDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Fileinfo;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.User;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class MusicManager extends Manager
{
	private UserDAO uDAO;
	private UserfileDAO ufDAO;
	
	public MusicManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		uDAO = dao.getDAO(UserDAO.class);
		ufDAO = dao.getDAO(UserfileDAO.class);
	}
	
	public List<User> getMusicServerListByFarmIP(String farmIP) throws DAOException
	{
		List<User> uList = uDAO.getMusicServerListByFarmIP(farmIP);
		setDefaultFile(uList);
		return uList;
	}

	public List<User> getMusicServerListByDomain(String domain) throws DAOException
	{
		List<User> uList = uDAO.getMusicServerListByDomain(domain);
		setDefaultFile(uList);
		return uList;
	}
	
	private void setDefaultFile(List<User> uList) throws DAOException
	{
		for(User u : uList)
			u.setFileList(getMusicFileList(u.getKey()));
	}
	public List<Fileinfo> getMusicFileList(Long userKey) throws DAOException
	{
		List<Fileinfo> fileList = ufDAO.getFileListByUserKey(userKey);
		if(fileList.size() == 0)
		{
			Fileinfo defaultMusic = new Fileinfo();
			defaultMusic.setAbsoluteName(FileUtils.getRelativeBasePath() + IPXProperties.getProperty(IPXPropertiesType.FILES_DEFAULT_MUSIC_ON_HOLD));
			fileList.add(defaultMusic);
		}
		return fileList;
	}
}
