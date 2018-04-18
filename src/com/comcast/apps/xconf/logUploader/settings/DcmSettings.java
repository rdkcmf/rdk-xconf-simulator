package com.comcast.apps.logupload.settings;

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
