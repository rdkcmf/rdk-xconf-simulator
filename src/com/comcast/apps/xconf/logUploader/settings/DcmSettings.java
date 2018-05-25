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

/**
 * @author smariy003c
 *
 */
public class DcmSettings {

	private String estbMacAddress = null;
	private LogSettings logSettings = null;
	private TelemetrySettings telemetrySettings = null;

	/**
	 * @return the estbMacAddress
	 */
	public String getEstbMacAddress() {
		return estbMacAddress;
	}

	/**
	 * @param estbMacAddress
	 *            the estbMacAddress to set
	 */
	public void setEstbMacAddress(String estbMacAddress) {
		this.estbMacAddress = estbMacAddress;
	}

	/**
	 * @return the logSettings
	 */
	public LogSettings getLogSettings() {
		return logSettings;
	}

	/**
	 * @param logSettings
	 *            the logSettings to set
	 */
	public void setLogSettings(LogSettings logSettings) {
		this.logSettings = logSettings;
	}

	/**
	 * @return the telemetrySettings
	 */
	public TelemetrySettings getTelemetrySettings() {
		return telemetrySettings;
	}

	/**
	 * @param telemetrySettings
	 *            the telemetrySettings to set
	 */
	public void setTelemetrySettings(TelemetrySettings telemetrySettings) {
		this.telemetrySettings = telemetrySettings;
	}
}
