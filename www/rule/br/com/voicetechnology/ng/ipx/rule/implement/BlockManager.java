package br.com.voicetechnology.ng.ipx.rule.implement;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.exception.validation.ValidateObjectException;
import br.com.voicetechnology.ng.ipx.commons.jms.tools.JMSNotificationTools;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.BlockDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.BlockitemDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ConfigblockDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.ServiceclassDAO;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Leg;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.SipAddressParser;
import br.com.voicetechnology.ng.ipx.pojo.db.PersistentObject;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Block;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Blockitem;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Config;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Configblock;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Pbxuser;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Serviceclass;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Usergroup;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.BlockIncomingInfo;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class BlockManager extends Manager
{
	private BlockDAO bDAO;
	private BlockitemDAO biDAO;
	private ConfigblockDAO cbDAO;
	private AddressDAO addDAO;
	private ConfigDAO confDAO;
	private ServiceclassDAO scDAO;

	public BlockManager(String loggerPath) throws DAOException
	{
		super(loggerPath);
		initDAO();
	}
	
	public BlockManager(Logger logger) throws DAOException
	{
		super(logger);
		initDAO();
	}

	private void initDAO() throws DAOException
	{
		bDAO = dao.getDAO(BlockDAO.class);
		biDAO = dao.getDAO(BlockitemDAO.class);
		cbDAO = dao.getDAO(ConfigblockDAO.class);
		addDAO = dao.getDAO(AddressDAO.class);
		confDAO = dao.getDAO(ConfigDAO.class);
		scDAO = dao.getDAO(ServiceclassDAO.class);
	}

	public void saveBlock(Block block) throws DAOException, ValidateObjectException
	{
		boolean remove = block.getKey() != null;
		bDAO.save(block);
		if(remove)
			for(Blockitem bi : biDAO.getBlockitemList(block.getKey()))
				biDAO.remove(bi);
		if(block.getBlockitemList() != null && block.getBlockitemList().size() > 0)
		{	
			for(Blockitem bi : block.getBlockitemList())
			{
				bi.setKey(null);
				bi.setBlockKey(block.getKey());	
				if(bi.getRegexType() == Blockitem.REGEX_ADVANCED_ON)
					bi.setRegex(bi.getPattern());
				else
					bi.setRegex(covertPatternToRegEx(bi.getPattern()));
				biDAO.save(bi);
			}
		}
	}

	public void saveConfigblock(Long blockKey, Long configKey) throws DAOException, ValidateObjectException
	{
		//TODO trocar relacionamento n x n -> 1 x n, fazer block ter configKey
		Configblock cblock = new Configblock();
		cblock.setBlockKey(blockKey);
		cblock.setConfigKey(configKey);
		cbDAO.save(cblock);
	}
	
	public void removeConfigBlocks(Long configKey) throws DAOException
	{
		List<Configblock> configBlockList = cbDAO.getConfigblockByConfig(configKey);
		for (Configblock configblock : configBlockList) 
			cbDAO.remove(configblock);
	}
	
	public void deleteConfigblock(Long configKey) throws DAOException
	{
		for(Configblock cb : cbDAO.getConfigblockByConfig(configKey))
			cbDAO.remove(cb);
	}
	
	public void deleteBlock(Long configKey, int blockType) throws DAOException, ValidateObjectException
	{
		Block block = bDAO.getBlockWithItens(configKey, blockType);
		if(block == null)
			return ;
		block.setActive(PersistentObject.DEFINE_DELETED);
		bDAO.save(block);
		for(Blockitem bi : block.getBlockitemList())
			biDAO.remove(bi);		
	}

	private String covertPatternToRegEx(String pattern)
	{
		pattern = pattern.replaceAll("\\+", "\\.");
		pattern = pattern.replaceAll("\\*", "\\.*");
		return pattern;
	}

	//pbx
	public boolean isBlockedCall(Config config, Leg toLeg, int blockType, Leg fromLeg) throws DAOException
	{
		SipAddressParser possibleBlockedAddress = null;
		if(blockType == Block.TYPE_OUTGOING)
			possibleBlockedAddress = toLeg.getSipAddress();
		else if(blockType == Block.TYPE_INCOMING && fromLeg != null)
			possibleBlockedAddress = fromLeg.getSipAddress();

		Block b = bDAO.getBlockWithItens(config.getKey(), blockType);
		if(b == null)
			return false;
		else if(b.getBlockNOID() == Block.DEFINE_ACTIVE && fromLeg.getDisplay() != null  && fromLeg.getDisplay().equals(Pbxuser.ANONYMOUS))
			return true;
		else if(b.getStatus().intValue() == Block.STATUS_NOT_ACTIVE)
			return false;

		boolean isBlocked = false;

		//dnakamashi - bug #6386 - version 3.0.6
		//agora grupo também tem serviceclass
		if(toLeg.hasServiceclass() || (fromLeg != null && fromLeg.hasServiceclass()))
			for(String address : getSynonymousAddress(possibleBlockedAddress.getExtension(), possibleBlockedAddress.getDomain()))
				if(isBlocked(address, b.getBlockitemList()))
				{
					isBlocked = true;
					break;
				}
				else
					isBlocked = isBlocked(possibleBlockedAddress.getExtension(), b.getBlockitemList());

		return b.isBlackList() ? isBlocked : !isBlocked;
	}	
	
	private boolean isBlocked(String address, List<Blockitem> biList)
	{
		boolean isBlocked = false;
		for(int i = 0; i < biList.size() && !isBlocked; i++)
			if(address.matches(biList.get(i).getRegex()))
				isBlocked = true;
		return isBlocked;
	}
	
	private List<String> getSynonymousAddress(String extension, String domain) throws DAOException
	{
		Address address = addDAO.getAddress(extension, domain);
		List<Address> addressList = null;
		List<String> addressStringList = new ArrayList<String>();
		if(address != null)
		{
			if(address.getGroupKey() != null)
				addressList = addDAO.getAddressListByGroup(address.getGroupKey());
			else if(address.getPbxKey() != null)
			{
				addressList = null;
				addressStringList.add(extension);
			}else if(address.getPbxuserKey() != null)
				addressList = addDAO.getAddressListByPbxuser(address.getPbxuserKey());
			
			if(addressList != null)
				for(Address aux : addressList)
					addressStringList.add(aux.getAddress());
		}else
			addressStringList.add(extension);
		return addressStringList;
	}
	
	public boolean isBlockedCallGroup(Config config, Long gKey, int blockType) throws DAOException
	{
		return isBlockedCall(config, gKey, true, blockType);
	}
	
	public boolean isBlockedCallPbxuser(Config config, Long uKey, int blockType) throws DAOException
	{
		return isBlockedCall(config, uKey, false, blockType);
	}
	
	private boolean isBlockedCall(Config config, Long primaryKey, boolean isGroup, int blockType) throws DAOException
	{
		Block b = bDAO.getBlockWithItens(config.getKey(), blockType);
		if(b == null || b.getStatus().intValue() == Block.STATUS_NOT_ACTIVE)
			return false;

		List<Blockitem> biList = b.getBlockitemList();
		List<Address> addList = new ArrayList<Address>();
		
		//adiciona os address da toLeg numa lista
		if(isGroup)
		{	
			addList.addAll(addDAO.getAddressListByGroup(primaryKey));
		}else
		{
			addList.addAll(addDAO.getAddressListByPbxuser(primaryKey));
			addList.addAll(addDAO.getTerminalExtensionListAssociatedByPbxuser(primaryKey));
		}			
		
		boolean isBlocked = false;
		for(int i = 0; i < biList.size() && !isBlocked; i++)
		{
			String regEx = biList.get(i).getRegex();
			for(int j = 0; j < addList.size() && !isBlocked; j++)
				if(addList.get(j).getAddress().matches(regEx))
					isBlocked = true;
		}
		return b.isBlackList() ? isBlocked: !isBlocked;
	}
	
	public boolean isBlockedCallFromServiceClass(Config config, int blockType, Long serviceclassKey) throws DAOException
	{
		Block b = bDAO.getBlockWithItens(config.getKey(), blockType);
		if(b == null || b.getStatus().intValue() == Block.STATUS_NOT_ACTIVE)
			return false;
		
		Serviceclass sc = scDAO.getServiceclassFull(serviceclassKey);
		if(sc != null)
		{
			Block bServiceClass = bDAO.getBlockWithItens(sc.getConfigKey(), blockType);
		
			if(bServiceClass.getKey().equals(b.getKey()))
				return true;
		}
		return false;
	}
	
	public BlockIncomingInfo getPbxuserIncomingBlock(Long pbxuserKey) throws DAOException 
	{
		Config config = confDAO.getConfigByPbxuser(pbxuserKey);
		Block block = bDAO.getBlockWithItens(config.getKey(), Block.TYPE_INCOMING);
		return new BlockIncomingInfo(block, config.getDndStatus());
	}
	
	public void updatePbxuserIncomingBlock(BlockIncomingInfo blockIncomingInfo) throws DAOException, ValidateObjectException
	{
		Block blockIncomingOld = bDAO.getBlockByPbxuser(blockIncomingInfo.getPbxuserKey());
		blockIncomingOld.setBlockNOID(blockIncomingInfo.getIncomingNOID());
		blockIncomingOld.setStatus(blockIncomingInfo.getIncomingStatus());
		blockIncomingOld.setBlockitemList(blockIncomingInfo.getBlockItemList());
		Config config = confDAO.getConfigByBlock(blockIncomingOld.getKey());
		Integer DNDValue = blockIncomingInfo.getDND();
		config.setDndStatus(DNDValue);
		confDAO.save(config);
		this.saveBlock(blockIncomingOld);
	}
}