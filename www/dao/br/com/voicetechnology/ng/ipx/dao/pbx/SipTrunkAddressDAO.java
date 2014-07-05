package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.SipTrunkAddress;

public interface SipTrunkAddressDAO  extends DAO<SipTrunkAddress>
{
	public List<Address> getSipTrunkAddressDidListIn(Long sipTrunkKey) throws DAOException;

	public List<Address> getSipTrunkAddressDidListOut(Long domainKey, Integer lastIndex, Integer maxResultsPerConsult, boolean isAll) throws DAOException;
	
	public SipTrunkAddress getSipTrunkAddressByAddressKey(Long addressKey) throws DAOException;
	
	public List<SipTrunkAddress> getSipTrunkAddressListBySipTrunkKey(Long sipTrunkKey) throws DAOException;
}
