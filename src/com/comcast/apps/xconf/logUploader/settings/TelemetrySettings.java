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
package com.comcast.apps.xconf.logUploader.settings;

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
