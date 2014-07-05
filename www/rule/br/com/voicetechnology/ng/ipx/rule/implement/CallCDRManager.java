package br.com.voicetechnology.ng.ipx.rule.implement;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.Cdr;

public class CallCDRManager extends ServiceCDRManager 
{

	private static final String CALL_CDR_PREFIX = "call_cdr";
	
	public CallCDRManager(String loggerPath) throws DAOException 
	{
		super(loggerPath);
	}

	public void saveCallCDR(Cdr cdr) 
	{
		try
		{    
			SimpleDateFormat datetimeFormater = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SS");
			SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
			StringBuffer sb = new StringBuffer();
			Charset cs = Charset.forName("Shift_JIS"); 
			
			// jfarah: correcao do bugid 5534: o nome do CDR deve ser composto com o IPXCALLGUID
			OutputStream fOut = new FileOutputStream(getCDRPath() + "/" + getFileName(CALL_CDR_PREFIX, cdr.getIpxCallGuid()));
			OutputStream bOut= new BufferedOutputStream(fOut);
			OutputStreamWriter out = new OutputStreamWriter(bOut, cs);
			
			int timezone = TimeZone.getDefault().getOffset(Calendar.getInstance().getTimeInMillis()) / (1000 * 3600);

			sb.append(XML_HEADER);
			sb.append("<IPXCalls>");
			sb.append("<IPXCall>");
			sb.append("<IPXCallGuid>");
			sb.append(cdr.getIpxCallGuid());
			sb.append("</IPXCallGuid>");
			sb.append("<DateReference>");
			sb.append(dateFormater.format(cdr.getDateReference().getTime()));
			sb.append("</DateReference>");
			sb.append("<CallStartTime>");
			sb.append(datetimeFormater.format(cdr.getCallStartTime().getTime()));
			sb.append("</CallStartTime>");
			sb.append("<CallEndTime>");
			sb.append(datetimeFormater.format(cdr.getCallEndTime().getTime()));
			sb.append("</CallEndTime>");
			sb.append("<CallDuration>");
			sb.append(cdr.getCallDuration());
			sb.append("</CallDuration>");
			sb.append("<OriginNumber>");
			sb.append(cdr.getOriginNumber());
			sb.append("</OriginNumber>");
			sb.append("<DialedNumber>");
			sb.append(cdr.getDialedNumber());
			sb.append("</DialedNumber>");
			sb.append("<DestinationNumber>");
			sb.append(cdr.getDestinationNumber());
			sb.append("</DestinationNumber>");
			sb.append("<CallType>");
			sb.append(cdr.getCallType());
			sb.append("</CallType>");
			sb.append("<CallDirection>");
			sb.append(cdr.getCallDirection());
			sb.append("</CallDirection>");
			sb.append("<AccountId>");
			sb.append(cdr.getAccountId());
			sb.append("</AccountId>");
			sb.append("<UserType>");
			sb.append(cdr.getUserType());
			sb.append("</UserType>");
			sb.append("<UserId>");
			sb.append(cdr.getUserId());
			sb.append("</UserId>");
			sb.append("<UserName>");
			sb.append(cdr.getUserName());
			sb.append("</UserName>");

			sb.append("<CallOwnerUserId>");
			sb.append(cdr.getCallOwnerUserId());
			sb.append("</CallOwnerUserId>");
			sb.append("<CallOwnerUserName>");
			sb.append(cdr.getCallOwnerUserName());
			sb.append("</CallOwnerUserName>");
			
			sb.append("<IPXCallId>");
			sb.append(cdr.getIpxCallId());
			sb.append("</IPXCallId>");
			sb.append("<SigCallId>");
			sb.append(cdr.getSigCallId());
			sb.append("</SigCallId>");
			sb.append("<Timezone>");
			sb.append(timezone);
			sb.append("</Timezone>");
			sb.append("<ApplicationVersion>");
			sb.append(cdr.getApplicationVersion());
			sb.append("</ApplicationVersion>");					
//			sb.append("<NoIdCause>");
//			sb.append("0");
//			sb.append("</NoIdCause>");
//			sb.append("<ProcessFlag>");
//			sb.append("");
//			sb.append("</ProcessFlag>");
//			sb.append("<ProcessServer>");
//			sb.append("");
//			sb.append("</ProcessServer>");
//			sb.append("<ProcessRetry>");
//			sb.append("");
//			sb.append("</ProcessRetry>");
//			sb.append("<DateLastUpdated>");
//			sb.append("");
//			sb.append("</DateLastUpdated>");
//			sb.append("<LastUpdateRemarks>");
//			sb.append("");
//			sb.append("</LastUpdateRemarks>");		
			sb.append("</IPXCall>");
			sb.append("</IPXCalls>");
			out.write(sb.toString());
			out.flush();  
			out.close();
		} catch(FileNotFoundException e)
		{
			logger.info("Could not generate Cdr file!");
			e.printStackTrace();
		} catch(IOException e)
		{
			logger.info("Could not generate Cdr file!");
			e.printStackTrace();
		}
	}
}