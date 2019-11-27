/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.energenie.internal.handler;

import static org.openhab.binding.energenie.internal.EnergenieBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergenieHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class EnergenieHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EnergenieHandler.class);

    private String protocol = "EG_PROTO_V20";
    private String host = "";
    private String password = "";

    @Nullable
    Socket socket = null;
    @Nullable
    private OutputStream output = null;
    @Nullable
    private InputStream input = null;

    private byte[] key = new byte[KEY_LEN];
    private byte[] task = new byte[TASK_LEN];
    private byte[] solution = new byte[SOLUTION_LEN];

    /**
     * The default refresh interval in Seconds.
     */
    private int DEFAULT_REFRESH_INTERVAL = 60;
    @Nullable
    private ScheduledFuture<?> refreshJob;
    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                socket = new Socket(host, TCP_PORT);
                socket.setSoTimeout(1500);
                updateStatus(ThingStatus.ONLINE);
                output = socket.getOutputStream();
                input = socket.getInputStream();
                authorize();
                socket.close();
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("Couldn't get I/O for the connection to: {}:{}.", host, TCP_PORT);
            }
        }
    };

    public EnergenieHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // refresh not supported
        } else if (command instanceof OnOffType) {
            byte[] ctrl = { DONT_SWITCH, DONT_SWITCH, DONT_SWITCH, DONT_SWITCH };
            switch (channelUID.getId()) {
                case SOCKET_1:
                    ctrl[0] = command.equals(OnOffType.ON) ? SWITCH_ON : SWITCH_OFF;
                    break;
                case SOCKET_2:
                    ctrl[1] = command.equals(OnOffType.ON) ? SWITCH_ON : SWITCH_OFF;
                    break;
                case SOCKET_3:
                    ctrl[2] = command.equals(OnOffType.ON) ? SWITCH_ON : SWITCH_OFF;
                    break;
                case SOCKET_4:
                    ctrl[3] = command.equals(OnOffType.ON) ? SWITCH_ON : SWITCH_OFF;
                    break;
            }
            String ctrlString = getByteString(ctrl);
            logger.trace("byte control (int) '{}' (hex)'{}'", ctrl, ctrlString);

            try {
                socket = new Socket(host, TCP_PORT);
                socket.setSoTimeout(1500);
                output = socket.getOutputStream();
                input = socket.getInputStream();
                authorize();
                byte[] controlmessage = encryptControls(ctrl);
                output.write(controlmessage);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("Couldn't get I/O for the connection to: {}:{}.", host, TCP_PORT);
            }
        }
    }

    @Override
    public void initialize() {
        Configuration config = getConfig();

        if ((config.get("host") != null) && (config.get("password") != null)) {
            host = (String) config.get("host");
            password = (String) config.get("password");
            key = getKey();
            logger.debug("Initializing EnergenieHandler for Host '{}'", host);
            if (getThing().getThingTypeUID().getId().equals("pmslan")) {
                protocol = "EG_PROTO_V20";
            } else if (getThing().getThingTypeUID().getId().equals("pmswlan")) {
                protocol = "EG_PROTO_WLAN";
            } else {
                protocol = "EG_PROTO_V21";
            }

            try {
                socket = new Socket(host, TCP_PORT);
                socket.setSoTimeout(1500);
                updateStatus(ThingStatus.ONLINE);
                output = socket.getOutputStream();
                input = socket.getInputStream();
                authorize();
                socket.close();
                onUpdate();

            } catch (UnknownHostException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("Can't find host: {}:{}.", host, TCP_PORT);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("Couldn't get I/O for the connection to: {}:{}.", host, TCP_PORT);
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device , IP-Address or password not set");
        }
    }

    @Override
    public void dispose() {
        logger.debug("EnergenieHandler disposed.");
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    public void authorize() {
        byte[] message = { 0x11 };
        byte[] response = new byte[4];
        byte[] statcryp = new byte[4];

        try {
            if (output != null && input != null) {
                output.write(message);
                logger.trace("Start Condition '{}' send to EG", message);
                input.read(response);
                for (int i = 0; i < 4; i++) {
                    task[i] = response[i];
                }
                String taskString = getByteString(task);
                logger.trace("EG responded with task (int) '{}' (hex) '{}'", task, taskString);

                byte[] solutionMessage = calculateSolution();

                output.write(solutionMessage);
                logger.trace("Solution '{}' send to EG", solutionMessage);

                input.read(statcryp);
                String statcrypString = getByteString(statcryp);
                logger.trace("EG responded with statcryp (int) '{}' (hex) '{}'", statcryp, statcrypString);

                byte[] status = decryptStatus(statcryp);

                String statusString = getByteString(status);
                logger.trace("EG responded with status (int) '{}' (hex) '{}'", status, statusString);

                stateUpdate(status);
            }
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.debug("Can't find host: {}:{}.", host, TCP_PORT);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.debug("Couldn't get I/O for the connection to: {}:{}.", host, TCP_PORT);
        }
    }

    public byte[] getKey() {
        int passwordLength = password.length();
        String passwordString = password;
        for (int i = 0; i < (8 - passwordLength); i++) {
            passwordString = passwordString + " ";
        }
        byte[] key = passwordString.getBytes();
        String ts = getByteString(key);
        return key;
    }

    public String getByteString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("0x%02X ", b));
        }
        String byteString = sb.toString();
        return byteString;
    }

    public byte[] calculateSolution() {
        int[] uIntTask = new int[4];
        for (int i = 0; i < 4; i++) {
            uIntTask[i] = Byte.toUnsignedInt(task[i]);
        }

        int test = (((uIntTask[0] ^ key[2]) * key[0]) ^ (key[6] | (key[4] << 8)) ^ uIntTask[2]);

        int solutionLoword = (((uIntTask[0] ^ key[2]) * key[0]) ^ (key[6] | (key[4] << 8)) ^ uIntTask[2]);

        byte loword[] = ByteBuffer.allocate(4).putInt(solutionLoword).array();

        int solutionHiword = (((uIntTask[1] ^ key[3]) * key[1]) ^ (key[7] | (key[5] << 8)) ^ uIntTask[3]);
        byte hiword[] = ByteBuffer.allocate(4).putInt(solutionHiword).array();

        solution[0] = loword[3];
        solution[1] = loword[2];
        solution[2] = hiword[3];
        solution[3] = hiword[2];

        return solution;
    }

    public byte[] decryptStatus(byte[] statcryp) {
        byte[] status = new byte[4];
        int[] status_i = new int[4];
        for (int i = 0; i < 4; i++) {
            status_i[i] = ((((statcryp[3 - i] - key[1]) ^ key[0]) - task[3]) ^ task[2]);
            status[i] = (byte) status_i[i];
        }
        return status;
    }

    public byte[] encryptControls(byte[] controls) {
        byte[] ctrlcryp = new byte[CTRLCRYP_LEN];
        int[] ctrlcryp_i = new int[CTRLCRYP_LEN];
        for (int i = 0; i < 4; i++) {
            ctrlcryp_i[i] = (((controls[3 - i] ^ task[2]) + task[3]) ^ key[0]) + key[1];
            ctrlcryp[i] = (byte) ctrlcryp_i[i];
        }

        return ctrlcryp;
    }

    public void stateUpdate(byte[] status) {
        String statusOn = STATE_ON;
        String statusOff = STATE_OFF;
        byte stat = status[0];
        switch (protocol) {
            case "EG_PROTO_V20":
                statusOn = STATE_ON;
                statusOff = STATE_OFF;
                break;
            case "EG_PROTO_V21":
                statusOn = V21_STATE_ON;
                statusOff = V21_STATE_OFF;
                break;
            case "EG_PROTO_WLAN":
                statusOn = WLAN_STATE_ON;
                statusOff = WLAN_STATE_OFF;
                break;
        }

        for (int i = 0; i < 4; i++) {
            String socket = SOCKET_1;
            switch (i) {
                case 0:
                    socket = SOCKET_1;
                    break;
                case 1:
                    socket = SOCKET_2;
                    break;
                case 2:
                    socket = SOCKET_3;
                    break;
                case 3:
                    socket = SOCKET_4;
                    break;
            }
            stat = status[i];

            StringBuilder sbStatus = new StringBuilder();
            sbStatus.append(String.format("%02x", stat));
            String stringStatus = sbStatus.toString();
            stringStatus = "0x" + stringStatus;

            if (stringStatus.equals(statusOn)) {
                updateState(socket, OnOffType.ON);
            } else if (stringStatus.equals(statusOff)) {
                updateState(socket, OnOffType.OFF);
            }
        }

    }

    private synchronized void onUpdate() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Configuration config = getThing().getConfiguration();
            int refreshInterval = DEFAULT_REFRESH_INTERVAL;
            Object refreshConfig = config.get("refresh");
            if (refreshConfig != null) {
                refreshInterval = ((BigDecimal) refreshConfig).intValue();
            }
            refreshJob = scheduler.scheduleWithFixedDelay(refreshRunnable, 5, refreshInterval, TimeUnit.SECONDS);
        }
    }

}
