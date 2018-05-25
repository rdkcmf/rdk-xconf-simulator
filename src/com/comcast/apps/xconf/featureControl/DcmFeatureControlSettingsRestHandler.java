/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.comcast.apps.xconf.featureControl;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.comcast.apps.dcmFeature.settings.DcmFeatureControlSettings;
import com.comcast.apps.dcmFeature.settings.DcmFeatureSettings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


/**
 * {@link DcmFeatureControlSettingsRestHandler} is used to get the DCM setting
 * Production from XCONF and forwarded to STB(with or without modification,
 * 
 * @author Rahul Raveendran
 *
 */
@Path("/featureControl")
public class DcmFeatureControlSettingsRestHandler {
	/**
	 * Logger instance for {@link DcmFeatureControlSettingsRestHandler}
	 */
	private static final Logger LOGGER = Logger.getLogger("DcmFeatureControlSettingsRestHandler");

	private static final ConcurrentHashMap<String, DcmFeatureControlSettings> DATA_STORE_UPLOAD_SETTINGS = new ConcurrentHashMap<String, DcmFeatureControlSettings>();

	/**
	 * 
	 * Method which sends the updated data to the STB once the user sends as GET request
	 * 
	 * @param httpServletRequest
	 * @param estbMacAddress			STBs MAC address
	 * @return
	 */
	@GET
	@Path("/getSettings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDcmSettings(@Context HttpServletRequest httpServletRequest,
			@QueryParam("estbMacAddress") String estbMacAddress) {

		ResponseBuilder jerseyResponse = null;

		try {
			
			String remoteHostAddress = httpServletRequest.getRemoteAddr();

			LOGGER.info("HTTP REQUEST RECEIVED FROM  HOST '" + remoteHostAddress + "' is "
					+ httpServletRequest.getQueryString());

			//XCONF Simulator: Hardcode dcmConfiguration. 
			String dcmConfiguration = "{\"featureControl\":{\"features\":[]}}";

			LOGGER.info("The response obtained is : " + dcmConfiguration);

			if (DATA_STORE_UPLOAD_SETTINGS.containsKey(estbMacAddress)) {
				DcmFeatureControlSettings dcmFeatureControlSettings = DATA_STORE_UPLOAD_SETTINGS.get(estbMacAddress);
						
				if (null != dcmFeatureControlSettings && null != dcmConfiguration) {
	
					JSONObject xconfDcmSetting = new JSONObject(dcmConfiguration);
	
					JSONArray dcmFeatureSettings = dcmFeatureControlSettings.getDcmfeatureSettings();
					if (null != dcmFeatureSettings) {
						modifyUploadDcmFeatureControlSettings(xconfDcmSetting, dcmFeatureSettings);	
					}
					dcmConfiguration = xconfDcmSetting.toString();	
					LOGGER.info("Modified Production DCM settings. updated DCM configuration \n " + dcmConfiguration);
				}				
			}else {
				LOGGER.info("No modification required for Production DCM settings. Sending default configuration");
			}
	
			jerseyResponse = Response.ok(dcmConfiguration);
			
		}catch (Exception e) {
			LOGGER.warning(e.getMessage());
		}

		return jerseyResponse.build();
	}
	

	/**
	 * Helper method to modify the default DCM configurations with the updated ones
	 * 
	 * @param xconfDcmSetting			default DCM configurations
	 * @param dcmFeatureSettings		ArrayList of {@link DcmFeatureSettings}				
	 * @throws JSONException
	 */
	private void modifyUploadDcmFeatureControlSettings(JSONObject xconfDcmSetting, JSONArray dcmFeatureSettings)
			throws JSONException {
		
		boolean isPresent = false;
		JSONObject featureControlSettings = xconfDcmSetting.getJSONObject("featureControl");
		JSONArray featureSettings = featureControlSettings.getJSONArray("features");
		LOGGER.info("Feature json is " + featureSettings.toString());
		JSONObject currentFeatureSettings = new JSONObject();
		JSONObject newFeatureSettings = new JSONObject();
		LOGGER.info("Removing features....");
		xconfDcmSetting.remove("features");
		int jsonArrayLength = featureSettings.length();
		
		/*
		 * Here we are iterating through the user-added config and verifying whether the value of the key "name"
		 * matches with the key value of the JSON obtained from PROD XCONF. If so, then the new object will replace the 
		 * existing object from the JSON Array retrieved from PROD XCONF. If a feature added by user is not present in the defaukt config,
		 * then it will be added to it
		 * 
		 */
		if(null != featureSettings) {			
			for(int count = 0; count < dcmFeatureSettings.length(); count ++) {							
				newFeatureSettings = dcmFeatureSettings.getJSONObject(count);
				LOGGER.info("new feature settings - " + count + " = " + newFeatureSettings);
				for(int index = 0; index < jsonArrayLength; index++) {				
					currentFeatureSettings = featureSettings.getJSONObject(index);
					LOGGER.info("current Feature Settings - " + index + " = " + currentFeatureSettings);
					if(currentFeatureSettings.get("name").equals(newFeatureSettings.get("name"))) {
						featureSettings.put(index,newFeatureSettings);
						isPresent = true;
					}
			    }	
				if(!isPresent) {
					featureSettings.put(newFeatureSettings);
				}
			}
		}
		//adding the JSONArray to the key entry features and then adding features as the value to the key featureControl
		featureControlSettings.put("features", featureSettings)	;
		xconfDcmSetting.put("featureControl", featureControlSettings);
	}
	
	@POST
	@Path("/updateSettings")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDcmFeatureControlSettings(String featureSettings) throws JSONException {
		
		String errorMessage = null;
		int httpErrorCode = 0;

		JSONObject settings = new JSONObject(featureSettings);

		boolean isMacAddressPresent = settings.has("estbMacAddress");
		String estbMacAddress = null;
		if (!isMacAddressPresent) {
			errorMessage = "estbMacAddress should be present in the configuration";
			httpErrorCode = 603;

		} else {
			estbMacAddress = settings.getString("estbMacAddress");
			LOGGER.info("The eSTB Mac address is --->" + estbMacAddress);
			
			if (null != estbMacAddress && !estbMacAddress.isEmpty()) {
				
				boolean isFeatureSettingsPresent = settings.has("features");
				
				JSONArray dcmfeatureSettings = new JSONArray();
				DcmFeatureControlSettings dcmFeatureControlSettings = new DcmFeatureControlSettings();
				if (isFeatureSettingsPresent) {
					dcmfeatureSettings = settings.getJSONArray("features");
					LOGGER.info("The dcm Features are -->" + dcmfeatureSettings);
					if(null != dcmfeatureSettings && dcmfeatureSettings.length() > 0) {
						
						dcmFeatureControlSettings.setEstbMacAddress(estbMacAddress);						
						dcmFeatureControlSettings.setDcmfeatureSettings(dcmfeatureSettings);
						
						DATA_STORE_UPLOAD_SETTINGS.put(estbMacAddress, dcmFeatureControlSettings);
						LOGGER.info("SUCCESSFULLY ADDED DCM Feature enable/disable seetings for ESTB MAC ADDRESS '"
								+ estbMacAddress);

						return Response.ok().build();
					} else {
						errorMessage = "'features' should not be null or empty";
						httpErrorCode = 603;
					}
					
				} else {
					errorMessage = "Either 'features' or 'configset-label' or 'confiset-id' should be present in the configurations";
					httpErrorCode = 603;
				} 
			} else {
				errorMessage = "estbMacAddress should not be null or empty";
				httpErrorCode = 603;
			}
		}
		
		return Response.status(httpErrorCode).header("Content-Length", errorMessage.length()).entity(errorMessage)
				.build();
	}
	
	@DELETE
	@Path("/clear")
        public Response clearAllTelemetrySummary(@QueryParam("allSettings") boolean allSettings,
                        @QueryParam("estbMacAddress") String estbMacAddress) {
                if (allSettings) {
                        DATA_STORE_UPLOAD_SETTINGS.clear();
                } else {
                        DATA_STORE_UPLOAD_SETTINGS.remove(estbMacAddress);
                }

                return Response.ok().build();
        }

}
