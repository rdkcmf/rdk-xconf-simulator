package com.comcast.apps.dcmFeature.settings;

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
