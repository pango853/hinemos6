/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.reflect.Method;

import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.validation.MethodValidator.MethodValidationContext;

public class AuthorizingValidator_scope_location implements CustomMethodValidator {
	@Override
	public void validate(Method method, ParamHolder params, String group, MethodValidationContext context) throws PluginException {
		String cloudScopeId = params.getParam("XCLOUD_CORE_CLOUDSCOPE_ID", String.class);
		CloudScopeEntity cloudScope = CloudManager.singleton().getCloudScopes().getCloudScopeByCurrentHinemosUser(cloudScopeId);
		String locationId = params.getParam("XCLOUD_CORE_LOCATION_ID", String.class);
		LocationEntity location = cloudScope.getLocation(locationId);
		if (location == null)
			throw ErrorCode.LOCATION_NOT_FOUND.cloudManagerFault(cloudScope.getId(), locationId);
	}
}
