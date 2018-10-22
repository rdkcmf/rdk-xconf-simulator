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
package com.comcast.apps.xconf.logUploader;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.comcast.apps.xconf.logUploader.settings.DcmSettings;
import com.comcast.apps.xconf.logUploader.settings.LogSettings;
import com.comcast.apps.xconf.logUploader.settings.TelemetrySettings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * {@link LogUploaderSettingsRestHandler} is used to get the DCM setting Production from XCONF and forwarded to STB(with
 * or without modification,
 * 
 * @author smariy003c
 *
 */
@Path("/logUploader")
public class LogUploaderSettingsRestHandler {

    /**
     * Logger instance for {@link LogUploaderSettingsRestHandler}
     */
    private static final Logger LOGGER = Logger.getLogger("LogUploaderSettingsRestHandler");

    /**
     * Jersey client connection timeout.
     */
    private static final int CONNECTION_TIMEOUT = 3000;

    /**
     * Jersey client read timeout.
     */
    private static final int READ_TIMEOUT = 3000;

    /**
     * DCM configuration updation time interval in milliseconds.
     */
    private static final long CONFIGURATION_UPDATION_TIME_INTERVAL_IN_MILLI_SECONDS = 14400 * 1000;

    /**
     * Data store is used to keep all test related configuration for automation execution.
     */
    private static final ConcurrentHashMap<String, DcmSettings> DATA_STORE_UPLOAD_SETTINGS = new ConcurrentHashMap<String, DcmSettings>();

    /**
     * 
     * Rest API to get the DCM configuration.
     * 
     * @param httpServletRequest
     *            The {@link HttpServletRequest} to get remote IP address and Request queries.
     * @param estbMacAddress
     *            The device mac address.
     * @param model
     *            The device model.
     * @return {@link Response}
     */
    @GET
    @Path("/getSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDcmSettings(@Context HttpServletRequest httpServletRequest,
	    @QueryParam("estbMacAddress") String estbMacAddress, @QueryParam("model") String model) {

	String remoteHostAddress = httpServletRequest.getRemoteAddr();
	LOGGER.info("HTTP REQUEST RECEIVED FROM  HOST '" + remoteHostAddress + "' is "
		+ httpServletRequest.getQueryString());
	/*
	 * Current version of DCM doesn't support all models.
	 */
	if (!isSupportedDevice(model)) {
	    return Response.status(601).entity("STB sending Unrecognized device model to XCONF server").build();
	}

	int statusCode = 0;
	String dcmConfiguration = null;

	/*
	 * For xconf simulator, read configuration from backup file instead of contacting actual xconf server. 
	 */

	dcmConfiguration = getBackupProdDcmConfiguration(model, dcmConfiguration);

	if (isValidString(dcmConfiguration)) {
	    estbMacAddress = estbMacAddress.toUpperCase();

	    if (DATA_STORE_UPLOAD_SETTINGS.containsKey(estbMacAddress)) {
		DcmSettings dcmSettings = DATA_STORE_UPLOAD_SETTINGS.get(estbMacAddress);
		try {

		    if (null != dcmSettings) {

			JSONObject xconfDcmSetting = new JSONObject(dcmConfiguration);

			LogSettings logSettings = dcmSettings.getLogSettings();
			if (null != logSettings) {
			    modifyUploadLogSettings(xconfDcmSetting, logSettings);

			}

			TelemetrySettings telemetrySettings = dcmSettings.getTelemetrySettings();
			if (null != telemetrySettings) {
			    modifyUploadTelemeterySettings(xconfDcmSetting, telemetrySettings);
			}

			dcmConfiguration = xconfDcmSetting.toString().replaceAll("\\\\", "");

			LOGGER.info("Modified Production DCM settings. updated DCM configuration ");
		    }
		} catch (Exception e) {
		    LOGGER.info("Exception occured -> Send the default configuration ");
		}

	    } else {
		LOGGER.info("No modification required for Production DCM settings. Sending default configuration");
	    }
	} else {
	    return Response.status(404).entity("No configuration present in Production as well as backup DCM").build();
	}

	return Response.ok(dcmConfiguration).build();
    }

    /**
     * Method to read backup PROD DCM configuration from file system.
     * 
     * @param model
     *            Model name corresponding to each device.
     * @param dcmConfiguration
     *            The production DCM configuration.
     * @return Prod DCM configuration.
     */
    private String getBackupProdDcmConfiguration(String model, String dcmConfiguration) {
	try {
	    ClassLoader classLoader = getClass().getClassLoader();
	    URL resourceUrl = classLoader.getResource(model + ".json");

	    if (resourceUrl != null) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream()));
		StringBuilder out = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
		    out.append(line);
		}
		reader.close();
		dcmConfiguration = out.toString();
		LOGGER.info("Backup DCM Configuration read from file system '" + resourceUrl);

	    } else {
		LOGGER.info("Backup configuration Configuration file not existing in expected location WEB-INF/classes ");
		dcmConfiguration = null;
	    }
	} catch (IOException e) {
	    LOGGER.info("Exception occured while reading Backup configuration Configuration WEB-INF/classes ");
	    dcmConfiguration = null;
	}
	return dcmConfiguration;
    }

    /**
     * Helper method to validate the string.
     * 
     * @param anyString
     *            Any string to be validated
     * @return true if give string is not null and not empty.
     */
    private boolean isValidString(String anyString) {
	return anyString != null && !anyString.isEmpty();
    }

    /**
     * Helper method to modify the production log upload settings with test requested settings.
     * 
     * @param logUploadDcmSettings
     *            The current production log upload settings.
     * @param logSettings
     *            Test requested configuration.
     * @throws JSONException
     *             Throws exception if anything goes wrong.
     */
    private void modifyUploadLogSettings (JSONObject logUploadDcmSettings, LogSettings logSettings)
	    throws JSONException {

	if (null != logSettings) {

	    if (null != logSettings.getUploadProtocol()) {
		logUploadDcmSettings.remove("urn:settings:LogUploadSettings:UploadRepository:uploadProtocol");
		logUploadDcmSettings.put("urn:settings:LogUploadSettings:UploadRepository:uploadProtocol",
			logSettings.getUploadProtocol());
	    }

	    if (null != logSettings.getUploadRepositoryUrl()) {
		logUploadDcmSettings.remove("urn:settings:LogUploadSettings:UploadRepository:URL");
		logUploadDcmSettings.put("urn:settings:LogUploadSettings:UploadRepository:URL",
			logSettings.getUploadRepositoryUrl());
	    }

	    if (null != logSettings.getUploadScheduleCron()) {
		logUploadDcmSettings.remove("urn:settings:LogUploadSettings:UploadSchedule:cron");
		logUploadDcmSettings.put("urn:settings:LogUploadSettings:UploadSchedule:cron",
			logSettings.getUploadScheduleCron());
	    }
	    if (logSettings.isUploadOnReboot()) {

		/*
		 * By default, 'uploadOnReboot' should be false. so no need to add check to update this property.
		 */
		logUploadDcmSettings.remove("urn:settings:LogUploadSettings:UploadOnReboot");
		logUploadDcmSettings.put("urn:settings:LogUploadSettings:UploadOnReboot",
			logSettings.isUploadOnReboot());
	    }

	}
    }

    /**
     * Helper method to modify the telemetry configuration which retrieved from production DCM server based on test
     * request.
     * 
     * @param xconfDcmSetting
     *            The production telemetry configuration.
     * @param telemetrySettings
     *            The telemetry configuration requested by automation test.
     * @throws JSONException
     *             Throws exception if anything went wrong.
     */
    private void modifyUploadTelemeterySettings(JSONObject xconfDcmSetting,
	    TelemetrySettings telemetrySettings) throws JSONException {

	JSONObject telemetryProfileSettings = xconfDcmSetting.getJSONObject("urn:settings:TelemetryProfile");
	xconfDcmSetting.remove("urn:settings:TelemetryProfile");

	if (null != telemetrySettings) {

	    if (null != telemetrySettings.getTelemetryProfile()) {
		JSONArray profileToBeAdded = telemetrySettings.getTelemetryProfile();
		JSONArray currentProdTelemetryProfile = telemetryProfileSettings.getJSONArray("telemetryProfile");

		int newProfileLength = profileToBeAdded.length();

		LOGGER.info("NEWLY REQUESTED TELEMETRY PROFILES LENGTH = " + newProfileLength
			+ "AND CURRENT PRODUCTION TELEMETRY PROFILES LENGTH = " + currentProdTelemetryProfile.length());

		for (int index = 0; index < newProfileLength; index++) {
		    currentProdTelemetryProfile.put(profileToBeAdded.get(index));
		}

		LOGGER.info("NEWLY MODIFIED TELEMETRY PROFILES LENGTH = " + currentProdTelemetryProfile.length());

		telemetryProfileSettings.remove("telemetryProfile");
		telemetryProfileSettings.put("telemetryProfile", currentProdTelemetryProfile);
	    }

	    // Re arrange the JSON order for uploadRepository:uploadProtocol
	    String schedule = telemetryProfileSettings.getString("schedule");
	    telemetryProfileSettings.remove("schedule");
	    if (null != telemetrySettings.getScheduleCron()) {
		telemetryProfileSettings.put("schedule", telemetrySettings.getScheduleCron());
	    } else {
		telemetryProfileSettings.put("schedule", schedule);
	    }

	    // Re arrange the JSON order for uploadRepository:uploadProtocol
	    String repositoryUrl = telemetryProfileSettings.getString("uploadRepository:URL");
	    telemetryProfileSettings.remove("uploadRepository:URL");
	    if (null != telemetrySettings.getUploadUrl()) {
		telemetryProfileSettings.put("uploadRepository:URL", telemetrySettings.getUploadUrl());
	    } else {
		telemetryProfileSettings.put("uploadRepository:URL", repositoryUrl);
	    }

	    // Re arrange the JSON order for uploadRepository:uploadProtocol
	    String repositoryProtocol = telemetryProfileSettings.getString("uploadRepository:uploadProtocol");
	    telemetryProfileSettings.remove("uploadRepository:uploadProtocol");
	    if (null != telemetrySettings.getUploadProtocol()) {
		telemetryProfileSettings.put("uploadRepository:uploadProtocol", telemetrySettings.getUploadProtocol());
	    } else {
		telemetryProfileSettings.put("uploadRepository:uploadProtocol", repositoryProtocol);
	    }

	}
	xconfDcmSetting.put("urn:settings:TelemetryProfile", telemetryProfileSettings);
    }

    /**
     * Rest API to update the DCM configuration.
     * 
     * @param uploadSettings
     *            test configuration for automation.
     * @return {@link Response}
     * @throws JSONException
     *             Throws if any exception happens.
     */
    @POST
    @Path("/updateSettings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLogUploadTelemetrySettings(String uploadSettings) throws JSONException {

	String errorMessage = null;
	int httpErrorCode = 0;

	JSONObject settings = new JSONObject(uploadSettings);

	boolean isMacAddressPresent = settings.has("estbMacAddress");
	String estbMacAddress = null;
	if (!isMacAddressPresent) {
	    errorMessage = "estbMacAddress should be present in the configuration";
	    httpErrorCode = 603;

	} else {
	    estbMacAddress = settings.getString("estbMacAddress");
	    if (isValidString(estbMacAddress)) {

		estbMacAddress = estbMacAddress.toUpperCase();

		boolean isLogUploadSettingsPresent = settings.has("logUploadSettings");
		boolean isTelemetrySettingsPresent = settings.has("telemetrySettings");

		JSONObject logUploadSettings = null;
		if (isLogUploadSettingsPresent) {
		    logUploadSettings = settings.getJSONObject("logUploadSettings");
		}

		JSONObject teleSettings = null;
		if (isTelemetrySettingsPresent) {
		    teleSettings = settings.getJSONObject("telemetrySettings");
		}

		if (isLogUploadSettingsPresent || isTelemetrySettingsPresent) {

		    if ((null != logUploadSettings && logUploadSettings.length() > 0)
			    || (null != teleSettings && teleSettings.length() > 0)) {

			DcmSettings dcmSettings = new DcmSettings();

			dcmSettings.setEstbMacAddress(estbMacAddress);

			if (isLogUploadSettingsPresent) {

			    LogSettings logSettings = new LogSettings();

			    if (logUploadSettings.has("uploadOnReboot")) {
				boolean uploadOnReboot = logUploadSettings.getBoolean("uploadOnReboot");
				logSettings.setUploadOnReboot(uploadOnReboot);
			    }
			    if (logUploadSettings.has("uploadRepositoryUrl")) {
				String uploadRepositoryUrl = logUploadSettings.getString("uploadRepositoryUrl");

				logSettings.setUploadRepositoryUrl(uploadRepositoryUrl);
			    }

			    if (logUploadSettings.has("uploadProtocol")) {
				String uploadProtocol = logUploadSettings.getString("uploadProtocol");
				logSettings.setUploadProtocol(uploadProtocol);
			    }
			    if (logUploadSettings.has("uploadScheduleCron")) {
				String uploadScheduleCron = logUploadSettings.getString("uploadScheduleCron");
				logSettings.setUploadScheduleCron(uploadScheduleCron);
			    }

			    dcmSettings.setLogSettings(logSettings);
			}
			if (settings.has("telemetrySettings")) {

			    TelemetrySettings telemetrySettings = new TelemetrySettings();

			    if (teleSettings.has("scheduleCron")) {
				String scheduleCron = teleSettings.getString("scheduleCron");
				telemetrySettings.setScheduleCron(scheduleCron);
			    }

			    if (teleSettings.has("uploadUrl")) {
				String uploadUrl = teleSettings.getString("uploadUrl");
				telemetrySettings.setUploadUrl(uploadUrl);
			    }

			    if (teleSettings.has("uploadProtocol")) {
				String uploadProtocol = teleSettings.getString("uploadProtocol");
				telemetrySettings.setUploadProtocol(uploadProtocol);
			    }

			    if (teleSettings.has("telemetryProfile")) {
				JSONArray telemetryProfile = teleSettings.getJSONArray("telemetryProfile");
				telemetrySettings.setTelemetryProfile(telemetryProfile);
			    }

			    dcmSettings.setTelemetrySettings(telemetrySettings);
			}

			DATA_STORE_UPLOAD_SETTINGS.put(estbMacAddress, dcmSettings);

			LOGGER.info("SUCCESSFULLY ADDED LOG UPLOAD/TELEMETRY SETTINGS FOR HOST MAC ADDRESS '"
				+ estbMacAddress);

			return Response.ok().build();
		    } else {
			errorMessage = "'telemetrySettings' or 'logUploadSettings' should not be null or empty";
			httpErrorCode = 603;
		    }

		} else {
		    errorMessage = "Either 'telemetrySettings' or 'logUploadSettings'should be present in configuration, otherwise it will take default configuration from PROD XCONF Server";
		    httpErrorCode = 603;
		}
	    }

	    else {

		errorMessage = "estbMacAddress should not be null or empty";
		httpErrorCode = 603;
	    }
	}

	return Response.status(httpErrorCode).header("Content-Length", errorMessage.length()).entity(errorMessage)
		.build();
    }

    /**
     * Rest API to clear all or particular device configuration.
     * 
     * @param allSettings
     *            This should be true if we need to clear entire data storage.
     * @param estbMacAddress
     *            Device mac address.
     * @return HTTP 200
     */
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

    /**
     * Helper method to get the supported list. This check is to make sure device is providing proper device models to
     * server to get appropriate firmware configuration.
     * 
     * @param deviceModel
     *            The device model
     * @return true if the device is supported by XCONF server.
     */
    private boolean isSupportedDevice(String deviceModel) {
	return isValidString(deviceModel);
    }
}
