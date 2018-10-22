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

import org.codehaus.jettison.json.JSONObject;

/**
 * Entity class which holds the settings fo the features to be enabled/disabled
 * 
 * @author rahulraveendran
 *
 */
public class DcmFeatureSettings {
	/** String to store the feature Name**/
	private String featureName = null;
	/** for feature effective Immediate status**/
	private String  effectiveImmediate = null;
	/** This determines whether the feature should be enabled or not**/
	private String featureEnable = null;
	/** Config data for the feature**/
	private JSONObject configData = null;
	public String getFeatureName() {
		return featureName;
	}
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public String getEffectiveImmediate() {
		return effectiveImmediate;
	}
	public void setEffectiveImmediate(String effectiveImmediate) {
		this.effectiveImmediate = effectiveImmediate;
	}
	public String getFeatureEnable() {
		return featureEnable;
	}
	public void setFeatureEnable(String featureEnable) {
		this.featureEnable = featureEnable;
	}
	public JSONObject getConfigData() {
		return configData;
	}
	public void setConfigData(JSONObject configData) {
		this.configData = configData;
	}
	
	
}
