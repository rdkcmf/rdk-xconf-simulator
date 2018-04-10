package com.comcast.apps.logupload.settings;

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
