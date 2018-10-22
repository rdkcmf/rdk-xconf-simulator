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
package com.comcast.apps.xconf.imageUpgrade.constant;

import java.util.concurrent.ConcurrentHashMap;

import com.comcast.apps.xconf.imageUpgrade.dao.XConfiguration;

/**
 * @author smariy003c
 *
 */
public interface XconfConstant {

	/**
	 * Constant for maximum Time to live value for {@link XConfiguration}
	 * object.
	 */
	long MAXIMUM_TTL_VALUE_FOR_XCONFIGURATION_OBJECT = 18000 * 1000;

	/**
	 * HashMap to hold the configuration for XCONF code download.
	 */
	ConcurrentHashMap<String, XConfiguration> DATA_STORE_XCONF_CONFIGURATION = new ConcurrentHashMap<String, XConfiguration>();

	/**
	 * JSON key for Addition firmware version info.
	 */
	String JSON_KEY_ADDITIONAL_FIRMWARE_VERSION_INFO = "additionalFwVerInfo";

	/**
	 * JSON key for firmware download protocol.
	 */
	String JSON_KEY_FIRMWARE_DOWNLOAD_PROTOCOL = "firmwareDownloadProtocol";
	/**
	 * JSON key for firmware file name.
	 */
	String JSON_KEY_FIRMWARE_FILE_NAME = "firmwareFilename";

	/**
	 * JSON key for firmware download location.
	 */
	String JSON_KEY_FIRMWARE_DOWNLOAD_LOCATION = "firmwareLocation";

	/**
	 * JSON key for firmware version.
	 */
	String JSON_KEY_FIRMWARE_VERSION = "firmwareVersion";

	/**
	 * JSON key for firmware ipv6 download location.
	 */
	String JSON_KEY_FIRMWARE_IPV6_DOWNLOAD_LOCATION = "ipv6FirmwareLocation";

	/**
	 * JSON key for reboot immediately flag.
	 */
	String JSON_KEY_REBOOT_IMMEDIATELY = "rebootImmediately";

	/**
	 * JSON key for XCONF Server configuration.
	 */
	String JSON_KEY_XCONF_SERVER_CONFIGURATION = "xconfServerConfig";

	/**
	 * Response error code - Empty firmware configuration.
	 */
	int RESPONSE_ERROR_CODE_EMPTY_FIRM_WARE_CONFIGURATION = 602;

	/**
	 * Response error code - JSON format issues.
	 */
	int RESPONSE_ERROR_CODE_JSON_FORMAT_ISSUE = 605;

	/**
	 * Response error code - Device MAC or xconf server configuration empty.
	 */
	int RESPONSE_ERROR_CODE_ESTB_MAC_OR_SERVER_CONFIG_EMPTY = 603;

	/**
	 * Response error code - Firmware configuration not found.
	 */
	int RESPONSE_ERROR_CODE_FIRM_WARE_CONFIGURATION_NOT_FOUND = 404;

	/**
	 * Response error code - Unrecognized device model.
	 */
	int RESPONSE_ERROR_CODE_UN_RECOGNIZED_DEVICE_MODEL = 601;

	/**
	 * Request Key for Additional firmware information.
	 */
	String REQUEST_KEY_ADDITIONAL_FIRMWARE_INFO = "additionalFwVerInfo";

	/**
	 * Request key for device model name.
	 */
	String REQUEST_KEY_DEVICE_MODEL_NAME = "model";

	/**
	 * Request key for device MAC address.
	 */
	String REQUEST_KEY_DEVICE_MAC_ADDRESS = "eStbMac";

}
