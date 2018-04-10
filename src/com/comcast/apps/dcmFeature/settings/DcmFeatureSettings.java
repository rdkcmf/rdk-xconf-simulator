package com.comcast.apps.dcmFeature.settings;

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
