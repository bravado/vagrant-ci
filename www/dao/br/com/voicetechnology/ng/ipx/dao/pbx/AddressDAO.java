package br.com.voicetechnology.ng.ipx.dao.pbx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.dao.DAO;
import br.com.voicetechnology.ng.ipx.dao.reporting.ReportDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.db.sys.Domain;
import br.com.voicetechnology.ng.ipx.pojo.info.Duo;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.DIDInfo;
import br.com.voicetechnology.ng.ipx.pojo.report.Report;

public interface AddressDAO extends DAO<Address>, ReportDAO<Address, DIDInfo>
{
	public List<Address> getExtensionListByPbxuser(Long pbxuserKey) throws DAOException;
	
	public List<String> getExtensionListByUser(Long userKey) throws DAOException;

	public Address getSipIDByPbxuser(Long key) throws DAOException;
	
	public List<Long> getDIDKeyExceptDefaultListByPbxuser(Long pbxuserKey) throws DAOException;

	public List<Address> getDIDListByPbxuser(Long pbxuserKey) throws DAOException;
	
	public List<Address> getIvrDIDListByPbxuser(Long pbxuserKey) throws DAOException;
	
	public List<Address> getIvrDIDListByDomain(Long domainKey) throws DAOException;
	
	public List<Address> getDIDListByPbxuser(Long pbxuserKey, Integer status) throws DAOException;
	
	public List<Address> getDIDListByPbxuserWithoutActive(Long pbxuserKey) throws DAOException;

	public List<Address> getExtensionListByGroup(Long groupKey) throws DAOException;
	
	public List<Address> getDIDListByGroupWithoutActive(Long groupKey) throws DAOException;

	public List<Address> getDIDListByGroup(Long groupKey) throws DAOException;

	public List<Duo<Long, String>> getAvailableDIDList(Long domainKey) throws DAOException;
	
	public List<Duo<Long, String>> getAvailableDIDList(Long domainKey, Integer lastIndex, Integer maxLength) throws DAOException;
	
	public List<Address> getDIDsInDomain(Long domainKey) throws DAOException;
	
	public Long getCountDIDsInDomain(Long domainKey) throws DAOException;
	
	public List<Address> getPartialDIDsInDomain(Long domainKey, Integer lastIndex, Integer maxResultsPerConsult) throws DAOException;
	
	public List<Address> getDIDsInDomainWithoutActive(Long domainKey) throws DAOException;
	
	public Address getSipIDByGroup(Long key) throws DAOException;

	//TODO metodo usado pra carregar todas as extensions de um domain para usar na funcionalidade de adicionar uma extension 
	//automatica pra algum usuario. Se um dia comecar a ter ativacao/desativacao de extensions, tem que mudar na query essa opcao
	public List<Address> getExtensionListByDomain(Long domainKey) throws DAOException;
	
	public List<String> getExtensionsByDomain(Long domainKey) throws DAOException;
	
	public List<Duo<Long, String>> getPbxuserKeyAndSipIDByDomain(Long domainKey, boolean voicemail, String... excludeSipID) throws DAOException;
	
	public List<Duo<Long, String>> getUsernameSIPIDList(Long domainKey, boolean voicemail, Integer lastIndex, Integer maxResult, String... excludeSipID) throws DAOException;
	
	public List<Duo<Long, String>> getGroupNameSipIDList(Long domainKey, boolean voicemail, Integer lastIndex, Integer maxResult) throws DAOException;
	
	public List<Duo<Long, String>> getTerminalNameSipIDList(Long domainKey, Integer lastIndex, Integer maxResult, String... excludeSipID) throws DAOException;
	
	public List<Duo<Long, String>> getIVRSipIDList(Long domainKey, Integer lastIndex, Integer maxResult) throws DAOException;
	
	public List<Duo<Long, String>> getGroupKeyAndSipIDByDomain(Long domainKey, boolean voicemail, String... excludeSipID) throws DAOException;
	
	public List<Duo<Long, String>> getTerminalPbxuserKeyAndTerminalNAmeByDomain(Long domainKey, String... excludeSipID) throws DAOException;
	
	public List<Duo<Long, String>> getIVRPbxuserKeyAndIVRNameByDomain(Long domainKey, String... excludeSipID) throws DAOException;
	
	//public List<Duo<Long, String>> getPbxuserKeyAndSipIDByPBX(Long pbxKey, String... excludeSipID) throws DAOException;

	//public List<Duo<Long, String>> getGroupKeyAndSipIDByPBX(Long pbxKey, String... excludeSipID) throws DAOException;

	public List<Address> getAddressListByPbxuser(Long puKey) throws DAOException;
	
	public List<Address> getAddressFullListByPbxuser(Long puKey) throws DAOException;

	public List<Address> getAddressListByGroup(Long groupKey) throws DAOException;
	
	public List<Address> getAddressFullListByGroup(Long groupKey) throws DAOException;

	/**
	 * First try to get extension and domain, but if results null, try to get only by address,
	 * this case will result something with the address is DID of another domain.
	 * Load Address and Domain.
	 */
	public Address getAddress(String extension, Long domainKey) throws DAOException;
	
	public Address getDIDAddress(String didNumber) throws DAOException;
	
	public Address getDID(String didNumber) throws DAOException;
	
	public Address getDIDAddress(String didNumber, Integer status) throws DAOException;

	public Address getAddress(String extension, String domain) throws DAOException;
	
	public Address getVoicemailAddress(Long domainKey) throws DAOException;
	
	public Address getVoicemailInternalAddress(String domain) throws DAOException;
	
	public List<Address> getVoicemailAddressList(String domain) throws DAOException;

	public Long countDIDs(Long domainKey, boolean isBlocked) throws DAOException;
	
	public Address getDefaultAddress(Long pbxKey) throws DAOException;
	
	public Address getDefaultAddressByDomain(String domain) throws DAOException;
	
	public Address getDefaultAddressByDomainKey(Long domainKey) throws DAOException;

	public List<Duo<Long, String>> getDIDListByRootDomain(Long rootKey) throws DAOException;
	
	public List<Address> getReportListCentrex(Report<DIDInfo> report) throws DAOException;
	
	public Long getReportCountCentrex(Report<DIDInfo> report) throws DAOException;
	
	public List<Address> getTerminalExtensionListAssociatedByPbxuser(Long pbxuserKey) throws DAOException;

	public List<Long> getPbxKeyByAddress(String address)throws DAOException;
	
	public Address getRecordFileBoxAddress(Long domainKey) throws DAOException;

	public List<Duo<Long, String>> getSipTrunkSIPIDList(Long domainKey, Integer lastIndex, Integer maxResult, String... excludeSipID) throws DAOException;
}