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
package com.comcast.apps.xconf.featureControl.settings;

import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author Rahul Raveendran
 *
 */
public class DcmFeatureControlSettings {
	/** eSTB Mac address of the STB**/
	private String estbMacAddress = null;
	/** DCM feature settings reference**/
	private JSONArray dcmfeatureSettings = null;
	/** String to store the config set ID**/
	private String configSetId = null;
	/** String to store the config set label**/
	private String configSetLabel = null;

	public JSONArray getDcmfeatureSettings() {
		return dcmfeatureSettings;
	}

	public void setDcmfeatureSettings(JSONArray dcmfeatureSettings) {
		this.dcmfeatureSettings = dcmfeatureSettings;
	}

	public String getConfigSetId() {
		return configSetId;
	}

	public void setConfigSetId(String configSetId) {
		this.configSetId = configSetId;
	}

	public String getConfigSetLabel() {
		return configSetLabel;
	}

	public void setConfigSetLabel(String configSetLabel) {
		this.configSetLabel = configSetLabel;
	}

	public String getEstbMacAddress() {
		return estbMacAddress;
	}

	public void setEstbMacAddress(String estbMacAddress) {
		this.estbMacAddress = estbMacAddress;
	}
	
	
	
}
