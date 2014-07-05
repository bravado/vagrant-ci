package br.com.voicetechnology.ng.ipx.rule.implement;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.jboss.util.id.GUID;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXProperties;
import br.com.voicetechnology.ng.ipx.commons.utils.property.IPXPropertiesType;
import br.com.voicetechnology.ng.ipx.dao.pbx.AddressDAO;
import br.com.voicetechnology.ng.ipx.dao.pbx.PbxDAO;
import br.com.voicetechnology.ng.ipx.pojo.db.pbx.Address;
import br.com.voicetechnology.ng.ipx.pojo.info.imp.PBXInfo;
import br.com.voicetechnology.ng.ipx.rule.interfaces.Manager;

public class ServiceCDRManager extends Manager
{

    protected static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private static final String DID_SERVICE_ID = "IPXPSTNNO";

    private static final String USER_SERVICE_ID = "IPXUSERNO";

    private static final String IVR_SERVICE_ID = "IPXIVRNO";

    private static final String STORAGE_SERVICE_ID = "IPXDSKSPC";

    private PbxManager pbxManager;

    private PbxDAO pbxDao;

    private Logger log4j;

    public ServiceCDRManager(String loggerPath) throws DAOException
    {
        super(loggerPath);
        pbxManager = new PbxManager(loggerPath);
        pbxDao = dao.getDAO(PbxDAO.class);
        log4j = Logger.getLogger(ServiceCDRManager.class);
    }
    
    

    public static String toHex(byte[] array)
    {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < array.length; i++)
        {
            int n = array[i] & 0xff;
            if (n < 0x10)
                buf.append('0');
            buf.append(Integer.toHexString(n));
        }
        return buf.toString();
    }

   

    protected String getFileName(String prefix, String ipxCallGuid)
    {
		// jfarah: correcao do bugid 5534: o nome do CDR deve ser composto com o IPXCALLGUID
        String fileName = new StringBuilder(prefix).
                append("_").
                append(new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())).
                append("_").
                append(ipxCallGuid).
                append(".xml").toString();
        return fileName;
    }

    private String generateIVRData(PBXInfo pbx, PBXMaxValues pbxOld)
    {
        int eventValue = (int) (pbx.getMaxIVRApplications() - pbxOld
                .getMaxIVRApplications());
        logEventValue(eventValue);

        if (eventValue != 0)
        {
            StringBuilder cdr = new StringBuilder();
            cdr.append("<CurrentValue>");
            cdr.append(pbx.getMaxIVRApplications());
            cdr.append("</CurrentValue>");

            cdr.append("<EventValue>");
            cdr.append(eventValue);
            cdr.append("</EventValue>");
            return cdr.toString();
        } else
        {
            return null;
        }
    }

    private String generateStorageData(PBXInfo pbx, PBXMaxValues pbxOld)
    {
        float eventValue = pbx.getMaxQuota() - pbxOld.getMaxQuota();
        logEventValue(eventValue);

        if (eventValue != 0.0)
        {
            StringBuilder cdr = new StringBuilder();
            cdr.append("<CurrentValue>");
            cdr.append(pbx.getMaxQuota());
            cdr.append("</CurrentValue>");

            cdr.append("<EventValue>");
            cdr.append(eventValue);
            cdr.append("</EventValue>");
            return cdr.toString();
        } else
        {
            return null;
        }
    }

    private String generateDIDData(PBXInfo pbx, PBXMaxValues pbxOld)
    {
        int eventValue = (int) (pbx.getDIDAmount() - pbxOld.getDidAmount());
        logEventValue(eventValue);

        if (eventValue != 0)
        {
            StringBuilder cdr = new StringBuilder();
            cdr.append("<CurrentValue>");
            cdr.append(pbx.getDIDAmount());
            cdr.append("</CurrentValue>");

            cdr.append("<EventValue>");
            cdr.append(eventValue);
            cdr.append("</EventValue>");
            return cdr.toString();
        } else
        {
            return null;
        }
    }

    private String generateUserData(PBXInfo pbx, PBXMaxValues pbxOld)
            throws DAOException
    {
        int eventValue = (int) (pbx.getMaxUsers() - pbxOld.getMaxUsers());
        logEventValue(eventValue);

        if (eventValue != 0)
        {
            StringBuilder cdr = new StringBuilder();
            cdr.append("<CurrentValue>");
            cdr.append(pbx.getMaxUsers());
            cdr.append("</CurrentValue>");

            cdr.append("<EventValue>");
            cdr.append(eventValue);
            cdr.append("</EventValue>");

            return cdr.toString();
        } else
        {
            return null;
        }
    }

    private String generateServiceRecord(PBXInfo pbx, PBXMaxValues pbxOld,
            String serviceId) throws DAOException
    {
        NDC.push(new StringBuilder("service: ").append(serviceId).toString());
        logger.debug("Generating Service Record");

        String serviceData = null;
        String result = null;

        if (serviceId.equals(USER_SERVICE_ID))
            serviceData = generateUserData(pbx, pbxOld);
        else if (serviceId.equals(DID_SERVICE_ID))
            serviceData = generateDIDData(pbx, pbxOld);
        else if (serviceId.equals(IVR_SERVICE_ID))
            serviceData = generateIVRData(pbx, pbxOld);
        else if (serviceId.equals(STORAGE_SERVICE_ID))
            serviceData = generateStorageData(pbx, pbxOld);

        if (serviceData != null)
        {
            StringBuilder cdr = new StringBuilder();
            cdr.append("<IPXBillByDayRecord>");

            cdr.append("<IPXBillByDayGuid>");
            cdr.append(GUID.asString());
            cdr.append("</IPXBillByDayGuid>");

            cdr.append("<AccountID>");
            cdr.append(pbx.getAccountId());
            cdr.append("</AccountID>");

            cdr.append("<BillByDayServiceID>");
            cdr.append(serviceId);
            cdr.append("</BillByDayServiceID>");

            cdr.append("<DateEffective>");
            cdr.append(new SimpleDateFormat("yyyy-MM-dd").format(Calendar
                    .getInstance().getTime()));
            cdr.append("</DateEffective>");

            cdr.append(serviceData);

            // Atualmente esta indo com o address key, e o address do default
            // operator, a ideia eh que futuramente seja preenchido com o
            // userkey, do usuario que solicitou a mudanca de quantidade.
            cdr.append("<UserID>");
            cdr.append(pbx.getDefaultOperatorKey());
            cdr.append("</UserID>");
            cdr.append("<UserName>" + getDefaultOperatorName(pbx)
                    + "</UserName>");

            cdr.append("<Timezone>");
            cdr.append(TimeZone.getDefault().getOffset(
                    Calendar.getInstance().getTimeInMillis())
                    / (1000 * 3600));
            cdr.append("</Timezone>");

            cdr.append("</IPXBillByDayRecord>");

            log4j.debug("Service record generated successfully");
            result = cdr.toString();
        } else
        {
            log4j.debug("No changes in service data");
        }

        NDC.pop();
        return result;
    }

    private String getDefaultOperatorName(PBXInfo pbx) throws DAOException
    {
        AddressDAO addressDao = dao.getDAO(AddressDAO.class);
        Address address = addressDao.getByKey(pbx.getDefaultOperatorKey());
        return address.getAddress();
    }

    protected String getCDRPath() throws IOException
    {
        String folder = null;

        String cdrHome = null;

        try
        {
            cdrHome = IPXProperties.getProperty(IPXPropertiesType.CDR_PATH);
            File f = new File(cdrHome);

            if (!f.exists())
                f.mkdir();

            folder = f.getCanonicalPath();
        } catch (IOException e1)
        {
            throw e1;
        }
        return folder;
    }

    private void logEventValue(Object value)
    {
        if (log4j.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder("Event value: ");
            sb.append(value);
            log4j.debug(sb);
        }
    }
}

class PBXMaxValues implements Serializable
{
    private static final long serialVersionUID = 7312620013735906155L;

    private int maxUsers = 0;

    private int maxIVRApplications = 0;

    private Float maxQuota = new Float(0);

    private int didAmount = 0;

    public int getDidAmount()
    {
        return didAmount;
    }

    public void setDidAmount(int didAmount)
    {
        this.didAmount = didAmount;
    }

    public int getMaxIVRApplications()
    {
        return maxIVRApplications;
    }

    public void setMaxIVRApplications(int maxIVRApplications)
    {
        this.maxIVRApplications = maxIVRApplications;
    }

    public Float getMaxQuota()
    {
        return maxQuota;
    }

    public void setMaxQuota(Float maxQuota)
    {
        this.maxQuota = maxQuota;
    }

    public int getMaxUsers()
    {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers)
    {
        this.maxUsers = maxUsers;
    }
}
