package br.com.voicetechnology.ng.ipx.rule.call.validationcall;

import br.com.voicetechnology.ng.ipx.commons.exception.callcontrol.ValidationException;
import br.com.voicetechnology.ng.ipx.commons.exception.dao.DAOException;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.RouteInfo;
import br.com.voicetechnology.ng.ipx.pojo.callcontrol.values.ValidationModes;

public class RouteInfoValidator implements RouteValidator
{

	public void validate(RouteInfo routeInfo, ValidationModes mode) throws ValidationException, DAOException
	{
		new FromLegValidator().validate(routeInfo, routeInfo.getFromLeg(), routeInfo.getToLeg(), mode);
		new ToLegValidator().validate(routeInfo.getCallInfo().isFromCallCenter(), routeInfo.getFromLeg(), routeInfo.getToLeg(), mode);
	}

}
