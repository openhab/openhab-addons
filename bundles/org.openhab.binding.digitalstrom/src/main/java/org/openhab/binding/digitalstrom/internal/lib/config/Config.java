/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.digitalstrom.internal.lib.config;

/**
 * The {@link Config} contains all configurations for the digitalSTROM-Library.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class Config {

    /* Client configuration */
    // connection configuration
    /**
     * The default application name to generate the application token.
     */
    public static final String DEFAULT_APPLICATION_NAME = "openHAB";
    /**
     * Defines the used tread pool name to get a thread pool from {@link ThreadPoolManager}.
     */
    public static final String THREADPOOL_NAME = "digitalSTROM";

    private String applicationName = DEFAULT_APPLICATION_NAME;

    private String host;
    private String userName;
    private String password;
    private String appToken;
    private String trustCertPath;
    private String cert;

    // Timeouts
    /**
     * Default connection timeout
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 4000;
    /**
     * High connection timeout for requests that take some time.
     */
    public static final int HIGH_CONNECTION_TIMEOUT = 60000;
    /**
     * Default read timeout
     */
    public static final int DEFAULT_READ_TIMEOUT = 10000;
    /**
     * High read timeout for requests that take some time.
     */
    public static final int HIGH_READ_TIMEOUT = 60000;
    /**
     * Default connection timeout for sensor readings from devices
     */
    public static final int DEFAULT_SENSORDATA_CONNECTION_TIMEOUT = 4000;
    /**
     * Default read timeout for sensor readings from devices
     */
    public static final int DEFAULT_SENSORDATA_READ_TIMEOUT = 20000;

    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private int sensordataConnectionTimeout = DEFAULT_SENSORDATA_CONNECTION_TIMEOUT;
    private int sensordataReadTimeout = DEFAULT_SENSORDATA_READ_TIMEOUT;

    /* Internal Configurations */
    // Trash Bin Config
    /**
     * The default number of days after the trash devices is deleted.
     */
    public static final int DEFAULT_TRASH_DEVICE_DELETE_TIME = 7;
    private int trashDeviceDeleteTime = DEFAULT_TRASH_DEVICE_DELETE_TIME;

    /**
     * The default milliseconds after the trash devices will be checked if its time to delete.
     */
    public static final int DEFAULT_BIN_CHECK_TIME = 360000; // in milliseconds
    private int binCheckTime = DEFAULT_BIN_CHECK_TIME; // in milliseconds

    // Device update config

    /**
     * Default interval of the polling frequency in milliseconds. The digitalSTROM-rules state that the
     * polling interval must to be at least 1 second.
     */
    public static final int DEFAULT_POLLING_FREQUENCY = 1000; // in milliseconds
    private int pollingFrequency = DEFAULT_POLLING_FREQUENCY; // in milliseconds

    /* Sensordata */
    // Sensodata read config

    /**
     * The default interval to refresh the sensor data.
     */
    public static final int DEFAULT_SENSORDATA_REFRESH_INTERVAL = 60000;
    private int sensordataRefreshInterval = DEFAULT_SENSORDATA_REFRESH_INTERVAL;

    /**
     * The default interval to refresh the total power sensor data.
     */
    public static final int DEFAULT_TOTAL_POWER_UPDATE_INTERVAL = 30000;
    private int totalPowerUpdateInterval = DEFAULT_TOTAL_POWER_UPDATE_INTERVAL;

    /**
     * Default time to wait between another {@link SensorJob} can be executed on a circuit.
     */
    public static final int DEFAULT_SENSOR_READING_WAIT_TIME = 60000;
    private int sensorReadingWaitTime = DEFAULT_SENSOR_READING_WAIT_TIME;

    // sensor data Prioritys
    /**
     * Priority for never refresh the sensor value.
     */
    public static final String REFRESH_PRIORITY_NEVER = "never";
    /**
     * Priority for refresh the sensor value with low priority.
     */
    public static final String REFRESH_PRIORITY_LOW = "low";
    /**
     * Priority for refresh the sensor value with medium priority.
     */
    public static final String REFRESH_PRIORITY_MEDIUM = "medium";
    /**
     * Priority for refresh the sensor value with high priority.
     */
    public static final String REFRESH_PRIORITY_HIGH = "high";

    // max sensor reading cyclic to wait
    /**
     * The default factor to prioritize medium {@link SensorJob}s down.
     */
    public static final long DEFAULT_MEDIUM_PRIORITY_FACTOR = 5;
    /**
     * The default factor to prioritize low {@link SensorJob}s down.
     */
    public static final long DEFAULT_LOW_PRIORITY_FACTOR = 10;

    private long mediumPriorityFactor = DEFAULT_MEDIUM_PRIORITY_FACTOR;
    private long lowPriorityFactor = DEFAULT_LOW_PRIORITY_FACTOR;

    /**
     * Defines the event polling interval of the {@link EventListener} in milliseconds.
     */
    private int eventListenerRefreshinterval = DEFAULT_POLLING_FREQUENCY;

    /**
     * The default max standby active power for a device. It's needed to set a {@link Device} with output mode
     * {@link OutputModeEnum#WIPE} on if it isen't any more in standby mode.
     */
    public static final int DEFAULT_STANDBY_ACTIVE_POWER = 2;

    private int standbyActivePower = DEFAULT_STANDBY_ACTIVE_POWER;

    /**
     * Creates a new {@link Config} and set the given hostAddress, userName, password and applicationToken. The other
     * configurations will be set to default.
     *
     * @param hostAddress of the digitalSTROM-Server, must not be null
     * @param userName to login, can be null
     * @param password to login, can be null
     * @param applicationToken to login , can be null
     */
    public Config(String hostAddress, String userName, String password, String applicationToken) {
        this.host = hostAddress;
        this.userName = userName;
        this.password = password;
        this.appToken = applicationToken;
    }

    /**
     * Creates a {@link Config} with default values.
     */
    public Config() {
        // config with default values
    }

    /**
     * Returns the host name from the server.
     *
     * @return the host address
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host name of the Server.
     * <br>
     * <br>
     * <b>Note:</b><br>
     * If the host dosen't use the default port (8080), the port has to be set after the host name. e.g.
     * <i>my-digitalSTROM-Server.com:58080</i>
     *
     * @param hostAddress of the digitalSTROM-Server
     */
    public void setHost(String hostAddress) {
        this.host = hostAddress;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the username.
     *
     * @param userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the Application-Token.
     *
     * @return the Application-Token
     */
    public String getAppToken() {
        return appToken;
    }

    /**
     * Sets the Application-Token.
     *
     * @param applicationToken to set
     */
    public void setAppToken(String applicationToken) {
        this.appToken = applicationToken;
    }

    /**
     * Returns the connection timeout.
     *
     * @return the connection timeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectionTimeout to set
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Returns the read timeout.
     *
     * @return the read timeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout.
     *
     * @param readTimeout the readTimeout to set
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Returns the connection timeout for sensor readings from devices.
     *
     * @return the connection sensor data timeout
     */
    public int getSensordataConnectionTimeout() {
        return sensordataConnectionTimeout;
    }

    /**
     * Sets the connection timeout for sensor readings from devices.
     *
     * @param sensordataConnectionTimeout to set
     */
    public void setSensordataConnectionTimeout(int sensordataConnectionTimeout) {
        this.sensordataConnectionTimeout = sensordataConnectionTimeout;
    }

    /**
     * Returns the read timeout for sensor readings from devices.
     *
     * @return the read sensor data timeout
     */
    public int getSensordataReadTimeout() {
        return sensordataReadTimeout;
    }

    /**
     * Sets the connection timeout for sensor readings from devices.
     *
     * @param sensordataReadTimeout to set
     */
    public void setSensordataReadTimeout(int sensordataReadTimeout) {
        this.sensordataReadTimeout = sensordataReadTimeout;
    }

    /**
     * Returns the path to the SSL-Certificate.
     *
     * @return the path to the trust certification
     */
    public String getTrustCertPath() {
        return trustCertPath;
    }

    /**
     * Return the SSL-Certificate of the server.
     *
     * @return the SSL-Certificate of the server
     */
    public String getCert() {
        return cert;
    }

    /**
     * Sets the SSL-Certificate of the server, will be set automatically by the {@link HttpTransportImpl} if no
     * SSL-Certification path is set.
     *
     * @param cert of the digitalSTROM-Server to set
     */
    public void setCert(String cert) {
        this.cert = cert;
    }

    /**
     * Sets the path to the SSL-Certificate. It can be a absolute or relative path.
     *
     * @param trustCertPath path to a SSL-Certificate
     */
    public void setTrustCertPath(String trustCertPath) {
        this.trustCertPath = trustCertPath;
    }

    /**
     * Returns the number of days after the trash devices is deleted.
     *
     * @return the trash-device delete time in days
     */
    public int getTrashDeviceDeleteTime() {
        return trashDeviceDeleteTime;
    }

    /**
     * Sets the number of days after the trash devices is deleted.
     *
     * @param trashDeviceDeleteTime in days
     */
    public void setTrashDeviceDeleteTime(int trashDeviceDeleteTime) {
        this.trashDeviceDeleteTime = trashDeviceDeleteTime;
    }

    /**
     * Sets the milliseconds after the trash devices will be checked, if its time to delete.
     *
     * @return the bin check time in milliseconds
     */
    public int getBinCheckTime() {
        return binCheckTime;
    }

    /**
     * Returns the milliseconds after the trash devices will be checked, if its time to delete.
     *
     * @param binCheckTime in milliseconds
     */
    public void setBinCheckTime(int binCheckTime) {
        this.binCheckTime = binCheckTime;
    }

    /**
     * Returns the interval of the polling frequency in milliseconds. The digitalSTROM-rules state that the
     * polling interval must to be at least 1 second.
     *
     * @return the pollingFrequency in milliseconds
     */
    public int getPollingFrequency() {
        return pollingFrequency;
    }

    /**
     * Sets the interval of the polling frequency in milliseconds. The digitalSTROM-rules state that the
     * polling interval must to be at least 1 second.
     *
     * @param pollingFrequency to set
     */
    public void setPollingFrequency(int pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
    }

    /**
     * Returns the interval in milliseconds to refresh the sensor data.
     *
     * @return the sensor data refresh interval in milliseconds
     */
    public int getSensordataRefreshInterval() {
        return sensordataRefreshInterval;
    }

    /**
     * Sets the interval in milliseconds to refresh the sensor data.
     *
     * @param sensordataRefreshInterval in milliseconds.
     */
    public void setSensordataRefreshInterval(int sensordataRefreshInterval) {
        this.sensordataRefreshInterval = sensordataRefreshInterval;
    }

    /**
     * Returns the interval to refresh the total power sensor data.
     *
     * @return the total power update interval in milliseconds.
     */
    public int getTotalPowerUpdateInterval() {
        return totalPowerUpdateInterval;
    }

    /**
     * Sets the interval in milliseconds to refresh the total power sensor data.
     *
     * @param totalPowerUpdateInterval in milliseconds
     */
    public void setTotalPowerUpdateInterval(int totalPowerUpdateInterval) {
        this.totalPowerUpdateInterval = totalPowerUpdateInterval;
    }

    /**
     * Returns the time in milliseconds to wait between another {@link SensorJob} can be executed on a circuit.
     *
     * @return the sensor reading wait time in milliseconds
     */
    public int getSensorReadingWaitTime() {
        return sensorReadingWaitTime;
    }

    /**
     * Sets the time in milliseconds to wait between another {@link SensorJob} can be executed on a circuit.
     *
     * @param sensorReadingWaitTime in milliseconds
     */
    public void setSensorReadingWaitTime(int sensorReadingWaitTime) {
        this.sensorReadingWaitTime = sensorReadingWaitTime;
    }

    /**
     * Returns the factor to prioritize medium {@link SensorJob}s in the {@link SensorJobExecutor} down.
     *
     * @return the medium priority factor
     */
    public long getMediumPriorityFactor() {
        return mediumPriorityFactor;
    }

    /**
     * Sets the factor to prioritize medium {@link SensorJob}s in the {@link SensorJobExecutor} down.
     *
     * @param mediumPriorityFactor to set
     */
    public void setMediumPriorityFactor(long mediumPriorityFactor) {
        this.mediumPriorityFactor = mediumPriorityFactor;
    }

    /**
     * Returns the factor to prioritize low {@link SensorJob}s in the {@link SensorJobExecutor} down.
     *
     * @return the low priority factor
     */
    public long getLowPriorityFactor() {
        return lowPriorityFactor;
    }

    /**
     * Sets the factor to prioritize low {@link SensorJob}s in the {@link SensorJobExecutor}down.
     *
     * @param lowPriorityFactor to set
     */
    public void setLowPriorityFactor(long lowPriorityFactor) {
        this.lowPriorityFactor = lowPriorityFactor;
    }

    /**
     * Returns the polling interval in milliseconds to poll the {@link Event}s in the {@link EventListener}.
     *
     * @return the EventListener refresh interval in milliseconds
     */
    public int getEventListenerRefreshinterval() {
        return eventListenerRefreshinterval;
    }

    /**
     * Sets the polling interval in milliseconds to poll the {@link Event}s in the {@link EventListener}.
     *
     * @param eventListenerRefreshinterval to set
     */
    public void setEventListenerRefreshinterval(int eventListenerRefreshinterval) {
        this.eventListenerRefreshinterval = eventListenerRefreshinterval;
    }

    /**
     * Returns the max standby active power for a device. It's needed to set a {@link Device} with output mode
     * {@link OutputModeEnum#WIPE} on if it isen't any more in standby mode.
     *
     * @return the standby active power
     */
    public int getStandbyActivePower() {
        return standbyActivePower;
    }

    /**
     * Sets the max standby active power for a device. It's needed to set a {@link Device} with output mode
     * {@link OutputModeEnum#WIPE} on if it isen't any more in standby mode.
     *
     * @param standbyActivePower to set
     */
    public void setStandbyActivePower(int standbyActivePower) {
        this.standbyActivePower = standbyActivePower;
    }

    /**
     * Returns the application name to generate the application token.
     *
     * @return the applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets the application name to generate the application token.
     *
     * @param applicationName to set
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Removes the configured username and password from this {@link Config}.
     */
    public void removeUsernameAndPassword() {
        this.userName = null;
        this.password = null;
    }

    /**
     * Updates this {@link Config} with the configuration of given {@link Config}.
     *
     * @param config new config
     */
    public void updateConfig(Config config) {
        setHost(config.getHost());
        setUserName(config.getUserName());
        setPassword(config.getPassword());
        setAppToken(config.getAppToken());
        setConnectionTimeout(config.getConnectionTimeout());
        setReadTimeout(config.getReadTimeout());
        setSensordataConnectionTimeout(config.getSensordataConnectionTimeout());
        setSensordataReadTimeout(config.getSensordataReadTimeout());
        setTrustCertPath(config.getTrustCertPath());
        setTrashDeviceDeleteTime(config.getTrashDeviceDeleteTime());
        setBinCheckTime(config.getBinCheckTime());
        setPollingFrequency(config.getPollingFrequency());
        setSensordataRefreshInterval(config.getSensordataRefreshInterval());
        setTotalPowerUpdateInterval(config.getTotalPowerUpdateInterval());
        setSensorReadingWaitTime(config.getSensorReadingWaitTime());
        setMediumPriorityFactor(config.getMediumPriorityFactor());
        setLowPriorityFactor(config.getLowPriorityFactor());
        setEventListenerRefreshinterval(config.getEventListenerRefreshinterval());
        setStandbyActivePower(config.getStandbyActivePower());
        setApplicationName(config.getApplicationName());
    }

    @Override
    public String toString() {
        return "Config [applicationName=" + applicationName + ", host=" + host + ", userName=" + userName
                + ", password=" + password + ", appToken=" + appToken + ", connectionTimeout=" + connectionTimeout
                + ", readTimeout=" + readTimeout + ", sensordataConnectionTimeout=" + sensordataConnectionTimeout
                + ", sensordataReadTimeout=" + sensordataReadTimeout + ", trustCertPath=" + trustCertPath
                + ", trashDeviceDeleteTime=" + trashDeviceDeleteTime + ", binCheckTime=" + binCheckTime
                + ", pollingFrequency=" + pollingFrequency + ", sensordataRefreshInterval=" + sensordataRefreshInterval
                + ", totalPowerUpdateInterval=" + totalPowerUpdateInterval + ", sensorReadingWaitTime="
                + sensorReadingWaitTime + ", mediumPriorityFactor=" + mediumPriorityFactor + ", lowPriorityFactor="
                + lowPriorityFactor + ", eventListenerRefreshinterval=" + eventListenerRefreshinterval
                + ", standbyActivePower=" + standbyActivePower + "]";
    }
}
