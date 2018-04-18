package com.comcast.apps.logupload.settings;

public class TelemetryProfile {

	private String content = null;

	private String type = null;

	private String header = null;

	private String pollingFrequency = null;

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * @return the pollingFrequency
	 */
	public String getPollingFrequency() {
		return pollingFrequency;
	}

	/**
	 * @param pollingFrequency
	 *            the pollingFrequency to set
	 */
	public void setPollingFrequency(String pollingFrequency) {
		this.pollingFrequency = pollingFrequency;
	}

}
