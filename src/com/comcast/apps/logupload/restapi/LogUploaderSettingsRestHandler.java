package com.comcast.apps.logupload.restapi;

import java.io.BufferedReader;
import java.io.File;
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

import com.comcast.apps.logupload.settings.DcmSettings;
import com.comcast.apps.logupload.settings.LogSettings;
import com.comcast.apps.logupload.settings.TelemetrySettings;
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
@Path("/dcm")
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
     * Location to save dcm configuration for backup purpose. This will be used if there any connection or read failure
     * happens with Production XCONF.
     */
    private static final String BACKUP_DCM_CONFIGURATION_LOCATION = System.getProperty("user.home") + File.separator
	    + "DCM" + File.separator;

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

	long startTime = System.currentTimeMillis();

	int statusCode = 0;
	String dcmConfiguration = null;

	long startTimeProdXconf = System.currentTimeMillis();

	try {

	    Client client = Client.create();
	    client.setConnectTimeout(CONNECTION_TIMEOUT);
	    client.setReadTimeout(READ_TIMEOUT);

	    WebResource webResource = client
		    .resource("https://xconf.xcal.tv/loguploader/getSettings?" + httpServletRequest.getQueryString());
	    ClientResponse response = webResource.header("X-Forwarded-For", remoteHostAddress)
		    .header("User-Agent", "RDKV-AUTO").get(ClientResponse.class);

	    statusCode = response.getStatus();

	    dcmConfiguration = response.getEntity(String.class);

	    LOGGER.info("[Performance Metrics] Time taken to complete one request in real Setup using PROD XCONF = "
		    + (System.currentTimeMillis() - startTimeProdXconf));

	    LOGGER.info("HTTP STATUS FOR REQUEST RECEIVED FROM XCONF SERVER FOR HOST '" + remoteHostAddress + "' is  "
		    + statusCode);

	    if (200 == statusCode) {
		/*
		 * Spawn a thread to save prod DCM configuration to file system. Writing to file system happens in
		 * background.
		 */
		Thread writerThread = new Thread(new DcmConfigurationWriterThread(model, dcmConfiguration));
		writerThread.start();
	    }
	} catch (Exception ex) {
	    LOGGER.info("Exception occurred during Prod XCONF " + ex.getLocalizedMessage());
	    statusCode = 0;
	}

	/*
	 * If Server status is not HTTP 200, check whether request is having valid model name, Then use backup
	 * production DCM configuration for log upload and telemetry automation.
	 */
	if (200 != statusCode) {

	    dcmConfiguration = getBackupProdDcmConfiguration(model, dcmConfiguration);
	}

	if (isValidString(dcmConfiguration)) {
	    estbMacAddress = estbMacAddress.toUpperCase();

	    if (DATA_STORE_UPLOAD_SETTINGS.containsKey(estbMacAddress)) {
		DcmSettings dcmSettings = DATA_STORE_UPLOAD_SETTINGS.get(estbMacAddress);
		try {

		    if (null != dcmSettings) {

			JSONObject xconfDcmSetting = new JSONObject(dcmConfiguration);

			LogSettings logSettings = dcmSettings.getLogSettings();
			if (null != logSettings) {
			    modifyUploadLogSettingsFromProdXconf(xconfDcmSetting, logSettings);

			}

			TelemetrySettings telemetrySettings = dcmSettings.getTelemetrySettings();
			if (null != telemetrySettings) {
			    modifyUploadTelemeterySettingsFromProdXconf(xconfDcmSetting, telemetrySettings);
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

	LOGGER.info("[Performance Metrics] Time taken to complete one request from STB = "
		+ (System.currentTimeMillis() - startTime));

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
	    File configuration = new File(BACKUP_DCM_CONFIGURATION_LOCATION + model + ".json");

	    LOGGER.info("Backup DCM Configuration Path =" + configuration.getAbsolutePath());

	    if (configuration.exists()) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configuration)));
		StringBuilder out = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
		    out.append(line);
		}
		reader.close();
		dcmConfiguration = out.toString();
		LOGGER.info("Backup DCM Configuration read from file system '" + configuration.getAbsolutePath());

	    } else {
		LOGGER.info("Backup configuration Configuration file not existing in expected location "
			+ BACKUP_DCM_CONFIGURATION_LOCATION);
		dcmConfiguration = null;
	    }
	} catch (IOException e) {
	    LOGGER.info("Exception occured while reading Backup configuration Configuration from "
		    + BACKUP_DCM_CONFIGURATION_LOCATION);
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
    private void modifyUploadLogSettingsFromProdXconf(JSONObject logUploadDcmSettings, LogSettings logSettings)
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
    private void modifyUploadTelemeterySettingsFromProdXconf(JSONObject xconfDcmSetting,
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
     * Runnable thread which handles the storage of Production DCM configuration to file system without affecting the
     * other functionality. This class is responsible for updating configuration once in every 4 hour corresponding each
     * device model. This is really required to unblock the automation execution if any connectivity issue happened
     * between PROXY DCM and PROD DCM.
     * 
     * @author smariy003c
     *
     */
    class DcmConfigurationWriterThread implements Runnable {

	/**
	 * The device model name corresponding to device.
	 */
	private String deviceModel = null;
	/**
	 * The production DCM configuration.
	 */
	private String dcmConfiguraltion = null;

	/**
	 * Constructor with model and dcm configuration.
	 * 
	 * @param model
	 *            The device model
	 * @param dcmConfig
	 *            The production DCM configuration.
	 */
	public DcmConfigurationWriterThread(String model, String dcmConfig) {
	    this.dcmConfiguraltion = dcmConfig;
	    this.deviceModel = model;
	}

	@Override
	public void run() {

	    LOGGER.info("Started Thread to Write DCM configuration to file system");
	    boolean update = true;
	    File configLocation = new File(BACKUP_DCM_CONFIGURATION_LOCATION);
	    if (!configLocation.exists()) {
		configLocation.mkdirs();
	    }
	    File configuration = new File(BACKUP_DCM_CONFIGURATION_LOCATION + deviceModel + ".json");
	    if (configuration.exists()) {
		long lastModified = configuration.lastModified();
		LOGGER.info("LAST MODIFIED TIME for configuration '" + configuration.getAbsolutePath() + " ' is = "
			+ lastModified);

		long currentTime = System.currentTimeMillis();
		LOGGER.info("CURRENT TIME is = " + currentTime);

		update = (currentTime - lastModified) >= CONFIGURATION_UPDATION_TIME_INTERVAL_IN_MILLI_SECONDS;
	    }
	    if (update) {

		FileOutputStream fileOutputStream = null;
		try {
		    fileOutputStream = new FileOutputStream(configuration);
		    fileOutputStream.write(dcmConfiguraltion.getBytes());
		    LOGGER.info("Backup DCM Configuration is saved in file system. Location is "
			    + configuration.getAbsolutePath());
		} catch (Exception e) {
		    LOGGER.info(
			    "Exception happened while writing file to location " + BACKUP_DCM_CONFIGURATION_LOCATION);
		} finally {
		    if (null != fileOutputStream) {
			try {
			    fileOutputStream.close();
			} catch (IOException e) {
			    LOGGER.info("Exception happened while writing file to location "
				    + BACKUP_DCM_CONFIGURATION_LOCATION);
			}
		    }
		}
	    } else {
		LOGGER.info(
			"Exit Writer Thread, since there is no updation required. last modified configuration time is less than 4 hours");
	    }
	    LOGGER.info("Completed Thread to Write DCM configuration to file system");
	}
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
	return isValidString(deviceModel) && (deviceModel.contains("AX013AN") || deviceModel.contains("AX11RAN")
		|| deviceModel.contains("CS011AN") || deviceModel.contains("CXD01ANI")
		|| deviceModel.contains("MX011AN") || deviceModel.contains("PR150BN") || deviceModel.contains("PX001AN")
		|| deviceModel.contains("PX013AN") || deviceModel.contains("PX022AN")
		|| deviceModel.contains("PX032ANI") || deviceModel.contains("PX051AEI")
		|| deviceModel.contains("PXD01ANI") || deviceModel.contains("SR150BN")
		|| deviceModel.contains("SX022AN") || deviceModel.contains("PX031ANI")
		|| deviceModel.contains("CX041AEI") || deviceModel.contains("SERXW3")
		|| deviceModel.contains("CGM4140COM") || deviceModel.contains("TG3482")
		|| deviceModel.contains("TG1682") || deviceModel.contains("DPC3941") || deviceModel.contains("DPC3939")
		|| deviceModel.contains("PX5001") || deviceModel.contains("AX014AN") || deviceModel.contains("SX061AEI")
		|| deviceModel.contains("AX061AEI") || deviceModel.contains("CGA4131COM"));
    }
}
