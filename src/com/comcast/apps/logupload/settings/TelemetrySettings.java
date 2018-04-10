package com.comcast.apps.logupload.settings;

import org.codehaus.jettison.json.JSONArray;

public class TelemetrySettings {

	private String scheduleCron = null;
	private String uploadUrl = null;
	private String uploadProtocol = null;
	private JSONArray telemetryProfile = null;

	/**
	 * @return the scheduleCron
	 */
	public String getScheduleCron() {
		return scheduleCron;
	}

	/**
	 * @param scheduleCron
	 *            the scheduleCron to set
	 */
	public void setScheduleCron(String scheduleCron) {
		this.scheduleCron = scheduleCron;
	}

	/**
	 * @return the uploadUrl
	 */
	public String getUploadUrl() {
		return uploadUrl;
	}

	/**
	 * @param uploadUrl
	 *            the uploadUrl to set
	 */
	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}

	/**
	 * @return the uploadProtocol
	 */
	public String getUploadProtocol() {
		return uploadProtocol;
	}

	/**
	 * @param uploadProtocol
	 *            the uploadProtocol to set
	 */
	public void setUploadProtocol(String uploadProtocol) {
		this.uploadProtocol = uploadProtocol;
	}

	/**
	 * @return the telemetryProfile
	 */
	public JSONArray getTelemetryProfile() {
		return telemetryProfile;
	}

	/**
	 * @param telemetryProfile
	 *            the telemetryProfile to set
	 */
	public void setTelemetryProfile(JSONArray telemetryProfile) {
		this.telemetryProfile = telemetryProfile;
	}
}
