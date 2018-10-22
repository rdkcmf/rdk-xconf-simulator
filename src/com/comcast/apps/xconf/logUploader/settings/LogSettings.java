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

public class LogSettings {

	private boolean uploadOnReboot = false;
	private String uploadRepositoryUrl = null;
	private String uploadProtocol = null;
	private String uploadScheduleCron = null;

	/**
	 * @return the uploadOnReboot
	 */
	public boolean isUploadOnReboot() {
		return uploadOnReboot;
	}

	/**
	 * @param uploadOnReboot
	 *            the uploadOnReboot to set
	 */
	public void setUploadOnReboot(boolean uploadOnReboot) {
		this.uploadOnReboot = uploadOnReboot;
	}

	/**
	 * @return the uploadRepositoryUrl
	 */
	public String getUploadRepositoryUrl() {
		return uploadRepositoryUrl;
	}

	/**
	 * @param uploadRepositoryUrl
	 *            the uploadRepositoryUrl to set
	 */
	public void setUploadRepositoryUrl(String uploadRepositoryUrl) {
		this.uploadRepositoryUrl = uploadRepositoryUrl;
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
	 * @return the uploadScheduleCron
	 */
	public String getUploadScheduleCron() {
		return uploadScheduleCron;
	}

	/**
	 * @param uploadScheduleCron
	 *            the uploadScheduleCron to set
	 */
	public void setUploadScheduleCron(String uploadScheduleCron) {
		this.uploadScheduleCron = uploadScheduleCron;
	}
}
