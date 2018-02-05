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
package com.comcast.apps.xconf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comcast.apps.xconf.constant.XconfConstant;
import com.comcast.apps.xconf.dao.XConfiguration;

/**
 * 
 * @author smariy003c
 *
 */
@Path("/swu/stb")
public class XconfServiceRestHandler {

	/**
	 * Flag is to check whether cleanupTriggered or not. This is to avoid
	 * multiple trigger and confusion.
	 */
	private static volatile boolean isAutoDetectionStarted = false;

	/**
	 * Logger instance for {@link XconfServiceRestHandler}
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger("XconfServiceRestHandler");

	/**
	 * Rest service to provide the device specific firmware configuration to
	 * requested devices.
	 * 
	 * @param req
	 *            The {@link HttpServletRequest} request.
	 * @param eStbMacAddress
	 *            The device MAC Address.
	 * @param deviceModel
	 *            The device model name.
	 * @param additionalFwVerInfo
	 *            The additional Firmware version information.
	 * @param requestParamData
	 *            The requested query parameters from Device.
	 * @return Firmware configuration targeted for particular device.
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFirmwareSettings(@Context HttpServletRequest req,
			@QueryParam(XconfConstant.REQUEST_KEY_DEVICE_MAC_ADDRESS) String eStbMacAddress,
			@QueryParam(XconfConstant.REQUEST_KEY_DEVICE_MODEL_NAME) String deviceModel,
			@QueryParam(XconfConstant.REQUEST_KEY_ADDITIONAL_FIRMWARE_INFO) String additionalFwVerInfo,
			String requestParamData) {

		return getFirmwareUpgradeConfiguration(req, eStbMacAddress, deviceModel, additionalFwVerInfo, requestParamData);
	}

	/**
	 * Helper method to get the value of Request parameter from Name value pair.
	 * 
	 * @param reqParam
	 *            The name value pair
	 * @param defaultValue
	 *            The default value expected
	 * @return the value corresponding to name.
	 */
	private String getValueFromRequestNameValuePair(String reqParam, String defaultValue) {
		String[] nameValuePair = reqParam.split("=");
		return nameValuePair.length > 1 ? nameValuePair[1] : defaultValue;
	}

	/**
	 * Helper method to get STB firmware upgradeConfiguration.
	 * 
	 * @param req
	 *            The {@link HttpServletRequest} request.
	 * @param eStbMacAddress
	 *            The device STB mac address.
	 * @param deviceModel
	 *            The device model.
	 * @return Response corresponding to STB.
	 */
	private Response getFirmwareUpgradeConfiguration(HttpServletRequest req, String eStbMacAddress, String deviceModel,
			String additionalFwVerInfo, String requestParamData) {

		String[] parameters = requestParamData.split("&");
		for (String reqParam : parameters) {
			if (reqParam.contains(XconfConstant.REQUEST_KEY_DEVICE_MAC_ADDRESS)) {
				eStbMacAddress = getValueFromRequestNameValuePair(reqParam, eStbMacAddress);
			}
			if (reqParam.contains(XconfConstant.REQUEST_KEY_DEVICE_MODEL_NAME)) {
				deviceModel = getValueFromRequestNameValuePair(reqParam, deviceModel);
			}
			if (reqParam.contains(XconfConstant.REQUEST_KEY_ADDITIONAL_FIRMWARE_INFO)) {
				additionalFwVerInfo = getValueFromRequestNameValuePair(reqParam, additionalFwVerInfo);
			}
		}

		String errorMessage = null;

		String queryParams = requestParamData;

		if ((queryParams == null) || (queryParams.isEmpty())) {
			queryParams = req.getQueryString();
		}

		int statusCode = 0;

		LOGGER.info("Received  <" + req.getMethod() + "> Request from remote Host IP address = <" + req.getRemoteAddr()
				+ "> for STB MAC Address = <" + eStbMacAddress + "> and Device Model = <" + deviceModel
				+ "> Additional Firmware Version = <" + additionalFwVerInfo + "> Request Parameters = " + queryParams);

		if (isValidString(deviceModel)) {

			String xconfServerConfiguration = "";
			if (isValidString(eStbMacAddress)) {
				/*
				 * Always use mac address in Upper case format.
				 */
				eStbMacAddress = eStbMacAddress.toUpperCase();

				if (XconfConstant.DATA_STORE_XCONF_CONFIGURATION.containsKey(eStbMacAddress)) {

					XConfiguration xconfiguration = XconfConstant.DATA_STORE_XCONF_CONFIGURATION.get(eStbMacAddress);
					xconfServerConfiguration = getFormattedXconfUpgradeConfiguration(req, xconfiguration,
							additionalFwVerInfo, deviceModel);

					LOGGER.info(xconfServerConfiguration);

					return Response.ok().entity(xconfServerConfiguration)
							.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
							.header(HttpHeaders.CONTENT_LENGTH, xconfServerConfiguration.length()).build();
				} else {
					statusCode = XconfConstant.RESPONSE_ERROR_CODE_FIRM_WARE_CONFIGURATION_NOT_FOUND;
					errorMessage = "Download configurations for device '" + eStbMacAddress
							+ "' is not available in XCONF Server";
					LOGGER.error("Error code = " + statusCode + ", Error Message = " + errorMessage);
				}
			} else {
				statusCode = XconfConstant.RESPONSE_ERROR_CODE_FIRM_WARE_CONFIGURATION_NOT_FOUND;
				errorMessage = "Download configurations is not available in XCONF Server, eSTB Mac Address is empty.";
				LOGGER.error("Error code = " + statusCode + ", Error Message = " + errorMessage);
			}
		} else {
			statusCode = XconfConstant.RESPONSE_ERROR_CODE_UN_RECOGNIZED_DEVICE_MODEL;
			errorMessage = "STB sending Unrecognized device model to XCONF server";
			LOGGER.error("Error code = " + statusCode + ", Error Message = " + errorMessage);
		}

		return Response.status(statusCode).entity(errorMessage).build();
	}

	/**
	 * Helper method to get the formatted XCONF upgrade configuration.
	 * Configuration will be formatted based on parameters like
	 * additionalFwVerInfo and firmwareDownloadProtocol. If
	 * firmwareDownloadProtocol is configured as TFTP, then both
	 * firmwareLocation and ipv6FirmwareLocation should be added to
	 * configuration, if it is HTTP, then only firmwareLocation is added,
	 * ipv6FirmwareLocation will be ignored. Similarly if additionalFwVerInfo is
	 * present in the request which device sent to XCONF server, then add
	 * additionalFwVerInfo in response configuration.
	 * 
	 * @param req
	 *            The {@link HttpServletRequest} instance to get the request
	 *            parameters.
	 * @param xconfiguration
	 *            The XCONF configuration uploaded in server via Rest API from
	 *            test clients.
	 * @param additionalFwVerInfo
	 *            The additional firmware info
	 * @param deviceModel
	 *            The device model for categorization
	 * @return Formatted XCONF configuration for Software upgrade.
	 */
	private String getFormattedXconfUpgradeConfiguration(HttpServletRequest req, XConfiguration xconfiguration,
			String additionalFwVerInfo, String deviceModel) {

		JSONObject stbUpgradeConfig = xconfiguration.getXconfStbUpgradeConfig();
		JSONObject formattedFirewareConfig = new JSONObject();
		String formattedConfiguration = stbUpgradeConfig.toString();

		try {

			boolean isAdditionalFwVerInfo = isValidString(additionalFwVerInfo);

			String firmwareDownloadProtocol = stbUpgradeConfig
					.getString(XconfConstant.JSON_KEY_FIRMWARE_DOWNLOAD_PROTOCOL);

			boolean isTftpDownload = isValidString(firmwareDownloadProtocol)
					&& firmwareDownloadProtocol.equalsIgnoreCase("tftp");

			if (stbUpgradeConfig.has(XconfConstant.JSON_KEY_FIRMWARE_DOWNLOAD_PROTOCOL)) {
				String firmwareDwProtocol = stbUpgradeConfig
						.getString(XconfConstant.JSON_KEY_FIRMWARE_DOWNLOAD_PROTOCOL);
				formattedFirewareConfig.put(XconfConstant.JSON_KEY_FIRMWARE_DOWNLOAD_PROTOCOL, firmwareDwProtocol);
			}

			if (stbUpgradeConfig.has(XconfConstant.JSON_KEY_FIRMWARE_FILE_NAME)) {
				String firmwareDwFileName = stbUpgradeConfig.getString(XconfConstant.JSON_KEY_FIRMWARE_FILE_NAME);
				formattedFirewareConfig.put(XconfConstant.JSON_KEY_FIRMWARE_FILE_NAME, firmwareDwFileName);
			}

			if (stbUpgradeConfig.has(XconfConstant.JSON_KEY_FIRMWARE_DOWNLOAD_LOCATION)) {
				String firmwareDwLocation = stbUpgradeConfig
						.getString(XconfConstant.JSON_KEY_FIRMWARE_DOWNLOAD_LOCATION);
				formattedFirewareConfig.put(XconfConstant.JSON_KEY_FIRMWARE_DOWNLOAD_LOCATION, firmwareDwLocation);
			}

			if (stbUpgradeConfig.has(XconfConstant.JSON_KEY_FIRMWARE_VERSION)) {
				String firmwareVersion = stbUpgradeConfig.getString(XconfConstant.JSON_KEY_FIRMWARE_VERSION);
				formattedFirewareConfig.put(XconfConstant.JSON_KEY_FIRMWARE_VERSION, firmwareVersion);
			}

			if (isTftpDownload) {

				if (stbUpgradeConfig.has(XconfConstant.JSON_KEY_FIRMWARE_IPV6_DOWNLOAD_LOCATION)) {
					String firmwareIpv6DownloadLocation = stbUpgradeConfig
							.getString(XconfConstant.JSON_KEY_FIRMWARE_IPV6_DOWNLOAD_LOCATION);
					formattedFirewareConfig.put(XconfConstant.JSON_KEY_FIRMWARE_IPV6_DOWNLOAD_LOCATION,
							firmwareIpv6DownloadLocation);
				}
			}

			if (stbUpgradeConfig.has(XconfConstant.JSON_KEY_REBOOT_IMMEDIATELY)) {
				String rebootImmediatelyFlag = stbUpgradeConfig.getString(XconfConstant.JSON_KEY_REBOOT_IMMEDIATELY);
				formattedFirewareConfig.put(XconfConstant.JSON_KEY_REBOOT_IMMEDIATELY, rebootImmediatelyFlag);
			}

			if (isAdditionalFwVerInfo) {

				String additionalFirmwareInfo = null;
				if (stbUpgradeConfig.has(XconfConstant.JSON_KEY_ADDITIONAL_FIRMWARE_VERSION_INFO)) {
					additionalFirmwareInfo = stbUpgradeConfig
							.getString(XconfConstant.JSON_KEY_ADDITIONAL_FIRMWARE_VERSION_INFO);

				}
				if (isValidString(additionalFirmwareInfo)) {
					formattedFirewareConfig.put(XconfConstant.JSON_KEY_ADDITIONAL_FIRMWARE_VERSION_INFO,
							additionalFirmwareInfo);
				}
			}

			formattedConfiguration = formattedFirewareConfig.toString().replaceAll("\\\\", "");
		} catch (JSONException jex) {
			LOGGER.error("Seems like Something happpend during JSON Processing, Please check error message "
					+ jex.getMessage(), jex);
			/*
			 * If any exception happens, please revert to old configuration
			 * posted by client devices.
			 */
			formattedConfiguration = stbUpgradeConfig.toString().replaceAll("\\\\", "");
		}

		return formattedConfiguration;
	}

	/**
	 * Update the configuration to data store for XCONF software upgrade.
	 * 
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response uploadFirmwareSettings(@Context HttpServletRequest req, String firmwareUpgradeConfig) {

		String errorMessage = null;
		int statusCode = 0;
		if (isValidString(firmwareUpgradeConfig)) {
			try {
				JSONObject stbUpgradeConfig = new JSONObject(firmwareUpgradeConfig);
				String eStbMacAddress = stbUpgradeConfig.getString(XconfConstant.REQUEST_KEY_DEVICE_MAC_ADDRESS);

				LOGGER.info("Received <" + req.getMethod() + "> Request from remote Host IP address = "
						+ req.getRemoteAddr() + " for STB MAC Address = " + eStbMacAddress
						+ "  Configuration requested = " + firmwareUpgradeConfig);

				JSONObject xconfServerConfig = stbUpgradeConfig
						.getJSONObject(XconfConstant.JSON_KEY_XCONF_SERVER_CONFIGURATION);

				if (isValidString(eStbMacAddress) && null != xconfServerConfig) {
					/*
					 * Always use device MAC address in Upper case format.
					 */
					eStbMacAddress = eStbMacAddress.toUpperCase();
					XConfiguration config = new XConfiguration(eStbMacAddress, xconfServerConfig);
					if (XconfConstant.DATA_STORE_XCONF_CONFIGURATION.containsKey(eStbMacAddress)) {
						XconfConstant.DATA_STORE_XCONF_CONFIGURATION.remove(eStbMacAddress);
					}

					XconfConstant.DATA_STORE_XCONF_CONFIGURATION.put(eStbMacAddress, config);
					/*
					 * Trigger the mechanism to auto delete expired objects.
					 * This will helps to stop upgrading bad builds again and
					 * again on same device due to stale data.
					 */
					triggerAutoDeletionOfExpiredConfiguration();

					return Response.ok().entity("Successfully added configuration").build();

				} else {
					statusCode = XconfConstant.RESPONSE_ERROR_CODE_ESTB_MAC_OR_SERVER_CONFIG_EMPTY;
					errorMessage = "eStbMac or  xconfServerConfig is empty.";
					LOGGER.error("Error code = " + statusCode + ", Error Message = " + errorMessage);
				}
			} catch (JSONException jex) {
				statusCode = XconfConstant.RESPONSE_ERROR_CODE_JSON_FORMAT_ISSUE;
				errorMessage = "Something wrong in JSON format" + jex.getLocalizedMessage();
				LOGGER.error("Error code = " + statusCode + ", Error Message = " + errorMessage);
			}

		} else {
			statusCode = XconfConstant.RESPONSE_ERROR_CODE_EMPTY_FIRM_WARE_CONFIGURATION;
			errorMessage = "Firmware firmware configuration is empty.";
			LOGGER.error("Error code = " + statusCode + ", Error Message = " + errorMessage);
		}

		return Response.status(statusCode).entity(errorMessage).build();
	}

	/**
	 * Update the configuration to data store for XCONF software upgrade.
	 * 
	 */
	@DELETE
	public Response deleteFirmwareSettings(@Context HttpServletRequest req,
			@QueryParam(XconfConstant.REQUEST_KEY_DEVICE_MAC_ADDRESS) String eStbMacAddress) {
		LOGGER.info("Received <" + req.getMethod() + "> Request from remote Host IP address = " + req.getRemoteAddr()
				+ " for STB MAC Address = " + eStbMacAddress);
		if (isValidString(eStbMacAddress)) {
			eStbMacAddress = eStbMacAddress.toUpperCase();
			if (eStbMacAddress.contains("ALL")) {
				XconfConstant.DATA_STORE_XCONF_CONFIGURATION.clear();
			} else {
				XconfConstant.DATA_STORE_XCONF_CONFIGURATION.remove(eStbMacAddress);
			}
		}

		return Response.ok().build();
	}

	/**
	 * Helper method to schedule the periodic check of expired object.
	 */
	private void triggerAutoDeletionOfExpiredConfiguration() {

		if (!isAutoDetectionStarted) {

			LOGGER.info("Triggering Auto deletion of expired configuration");

			isAutoDetectionStarted = true;
			ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
			try {
				scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

					@Override
					public void run() {

						LOGGER.info("@@@@@@@@@  Auto Detection of Expired Configuration  Thread Started  @@@@@@@@@ ");

						Map<String, XConfiguration> tempCache = XconfConstant.DATA_STORE_XCONF_CONFIGURATION;

						List<XConfiguration> configiration = new ArrayList<XConfiguration>(tempCache.values());
						for (XConfiguration xConfiguration : configiration) {

							long objectCreationTime = xConfiguration.getCreationTime();
							long currentTime = System.currentTimeMillis();
							boolean isExpired = (currentTime
									- objectCreationTime) >= XconfConstant.MAXIMUM_TTL_VALUE_FOR_XCONFIGURATION_OBJECT;

							if (isExpired) {
								LOGGER.info("Found expired configuration, searching corresponding key for deletion.");
								String tempKey = "";
								for (String key : tempCache.keySet()) {
									XConfiguration value = tempCache.get(key);
									if (value.equals(xConfiguration)) {
										tempKey = key;
										break;
									}
								}
								XconfConstant.DATA_STORE_XCONF_CONFIGURATION.remove(tempKey);
								LOGGER.info("Successfully Deleted expired configuration for mac address ' " + tempKey
										+ " '");
							}
						}

						LOGGER.info("@@@@@@@@@  Auto Detection of Expired Configuration  Thread Completed  @@@@@@@@@ ");

					}
				}, 5, 2, TimeUnit.HOURS);

			} catch (Exception ex) {
				/*
				 * This try catch is to re-trigger the scheduled Thread
				 */
				isAutoDetectionStarted = false;
				scheduledExecutorService.shutdown();
				LOGGER.info("Exception occurred -> " + ex.getMessage());
			}

			LOGGER.info("Successfully triggered Auto deletion of expired configuration");
		}

	}

	/**
	 * Helper method to validate the string value.
	 * 
	 * @param value
	 *            Value to be checked.
	 * @return true if given string is valid.
	 */
	private boolean isValidString(String value) {

		boolean isValid = false;
		if (value != null && (value.length() != 0)) {
			isValid = true;
		}
		return isValid;
	}
}
