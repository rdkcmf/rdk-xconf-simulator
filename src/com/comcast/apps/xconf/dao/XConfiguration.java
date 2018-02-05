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
package com.comcast.apps.xconf.dao;

import org.codehaus.jettison.json.JSONObject;

/**
 * @author smariy003c
 *
 */
public class XConfiguration {

	/**
	 * This holds the eSTB MAC address for particular device.
	 */
	private String eStbMacAddress;

	/**
	 * Holds the software upgrade configuration object.
	 */
	private JSONObject xconfStbUpgradeConfig;

	/**
	 * Object creation time to delete after particular time period. TO avoid
	 * repeated code download and memory full scenario.
	 */
	private long creationTime;

	/**
	 * Constructor with two parameter.
	 * 
	 * @param eStbMac
	 *            The STB mac address.
	 * @param upgradeConfig
	 *            firmware configuration.
	 */
	public XConfiguration(String eStbMac, JSONObject upgradeConfig) {
		this.eStbMacAddress = eStbMac;
		this.xconfStbUpgradeConfig = upgradeConfig;
		this.creationTime = System.currentTimeMillis();
	}

	/**
	 * @return the eStbMacAddress
	 */
	public String geteStbMacAddress() {
		return eStbMacAddress;
	}

	/**
	 * @param eStbMacAddress
	 *            the eStbMacAddress to set
	 */
	public void seteStbMacAddress(String eStbMacAddress) {
		this.eStbMacAddress = eStbMacAddress;
	}

	/**
	 * @return the xconfStbUpgradeConfig
	 */
	public JSONObject getXconfStbUpgradeConfig() {
		return xconfStbUpgradeConfig;
	}

	/**
	 * @param xconfStbUpgradeConfig
	 *            the xconfStbUpgradeConfig to set
	 */
	public void setXconfStbUpgradeConfig(JSONObject xconfStbUpgradeConfig) {
		this.xconfStbUpgradeConfig = xconfStbUpgradeConfig;
	}

	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime
	 *            the creationTime to set
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
}
