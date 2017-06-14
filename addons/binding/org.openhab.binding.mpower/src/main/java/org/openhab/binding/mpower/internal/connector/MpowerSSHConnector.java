/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mpower.internal.connector;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.mpower.handler.MpowerHandler;
import org.openhab.binding.mpower.internal.MpowerSocketState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Core SSH connector for mPower devices.
 * Can read data and perform commands (such as switching outlets)
 *
 * @author Marko Donke - Initial contribution
 *
 */
public class MpowerSSHConnector {
    private String host;
    private int connectTimeout = 3000;
    private int sessionTimeout = 3000;
    private String user;
    private String password;
    private Session session;

    private int ports = 0;
    private boolean isConnecting = false;
    private final Logger logger = LoggerFactory.getLogger(MpowerSSHConnector.class);
    private MpowerHandler mPowerHandler;

    public MpowerSSHConnector(String host, String user, String password, MpowerHandler handler) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.mPowerHandler = handler;
    }

    /**
     * Starts the connector
     */
    public void start() {
        logger.debug("connection attempt");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        JSch jsch = new JSch();
        Session aSession;
        try {
            this.isConnecting = true;
            aSession = jsch.getSession(user, host, 22);
            aSession.setPassword(password);
            aSession.setConfig(config);
            aSession.setTimeout(sessionTimeout);
            aSession.setServerAliveInterval(1000 * 60);
            aSession.setServerAliveCountMax(10);
            aSession.connect(connectTimeout);
            this.session = aSession;
            getNumberOfSockets();
            enableEnergyMeasurement();
            updateBridgeStatus();
            logger.info("connected to mPower on host {}", this.host);
        } catch (JSchException e) {
            logger.error("Could not connect.", e);
        } finally {
            this.isConnecting = false;
        }
    }

    public void stop() {
        if (isRunning() && this.session != null) {
            this.session.disconnect();
            logger.info("Session closed to {}", this.host);
        }
    }

    public void pollData() {
        try {

            if (session != null && session.isConnected()) {
                SSHExecutor exec = new SSHExecutor(session);
                StringBuilder builder = new StringBuilder();
                if (getPorts() > 0) {
                    builder.append("cat");
                }
                for (int i = 1; i < getPorts() + 1; i++) {
                    builder.append(" /proc/power/v_rms").append(i);
                    builder.append(" /proc/power/active_pwr").append(i);
                    builder.append(" /proc/power/cf_count").append(i);
                    builder.append(" /proc/power/relay").append(i);
                }
                String command = builder.toString();
                if (StringUtils.isNotBlank(command)) {
                    String result = exec.execute(command);
                    publishStateToHandler(result);
                }
            }
        } catch (JSchException e) {
            logger.error("Failed to query mPower", e);
        }
    }

    /**
     * enables energy measurement on mPower. This is not enabled by default.
     * Will be set on each connect.
     */
    private void enableEnergyMeasurement() {
        try {
            SSHExecutor exec = new SSHExecutor(this.session);
            StringBuilder builder = new StringBuilder();

            for (int i = 1; i < this.ports + 1; i++) {
                builder.append("echo 1 > /proc/power/enabled").append(i).append(";");
            }
            String command = builder.toString();
            exec.execute(command);
        } catch (JSchException e) {
            logger.warn("Failed to enable enery measurement. {}", e.getMessage());
        }
    }

    private void updateBridgeStatus() {
        try {
            SSHExecutor exec = new SSHExecutor(this.session);
            String command = "cat /etc/version";
            String result = exec.execute(command);

            this.mPowerHandler.receivedBridgeStatusUpdateFromConnector(result);
        } catch (JSchException e) {
            logger.warn("Failed to retrieve firmware version. {}", e.getMessage());
        }

    }

    public boolean isRunning() {
        return this.isConnecting || (this.session != null && this.session.isConnected());
    }

    /**
     * The polling agent calls this with raw data from the mPower.
     * This translates the raw data into MpowerSocketState objects
     *
     * @param message
     */
    protected void publishStateToHandler(String message) {
        if (StringUtils.isNotBlank(message)) {

            String[] parts = message.split("\n");
            if (parts.length == this.ports * 4) {
                for (int i = 1; i < this.ports + 1; i++) {
                    MpowerSocketState state = new MpowerSocketState(parts[4 * (i - 1)], parts[4 * (i - 1) + 1],
                            parts[4 * (i - 1) + 2], parts[4 * (i - 1) + 3], i);
                    this.mPowerHandler.receivedUpdateFromConnector(state);
                }
            }
        }
    }

    /**
     * Sends relay switch commands
     *
     * @param socket
     * @param onOff
     */
    public void sendOnOff(int socket, OnOffType onOff) {
        try {
            SSHExecutor exec = new SSHExecutor(this.session);
            String onOffString = onOff == OnOffType.ON ? "1" : "0";
            String command = "echo " + onOffString + " > /proc/power/relay" + socket;
            exec.execute(command);
            exec = null;
        } catch (JSchException e) {
            logger.warn("Failed to switch {}", e.getMessage());
        }
    }

    /**
     * looks for number of ports directly on the mPower
     */
    private void getNumberOfSockets() {
        try {
            SSHExecutor exec = new SSHExecutor(this.session);
            String command = "cat /etc/board.inc | grep feature_power";
            String result = exec.execute(command);
            result = StringUtils.substringAfterLast(result, "=");
            result = StringUtils.substringBeforeLast(result, ";");
            if (StringUtils.isNotBlank(result) && StringUtils.isNumeric(result)) {
                logger.debug("This is a {} port mPower", result);
                this.ports = Integer.parseInt(result);
            }
        } catch (JSchException e) {
            logger.error("Failed to read number of ports", e);
        }
    }

    /**
     * Returns the number of ports as queried from the mPower
     *
     * @return
     */
    protected int getPorts() {
        return ports;
    }

    public String getId() {
        return this.mPowerHandler.getThing().getUID().getAsString();
    }
}