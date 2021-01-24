/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.ventaair.internal;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.ventaair.internal.VentaThingHandler.StateUpdatedCallback;
import org.openhab.binding.ventaair.internal.message.CommandMessage;
import org.openhab.binding.ventaair.internal.message.DeviceInfoMessage;
import org.openhab.binding.ventaair.internal.message.Header;
import org.openhab.binding.ventaair.internal.message.Message;
import org.openhab.binding.ventaair.internal.message.action.Action;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Communicator} is responsible for sending/receiving commands to/from the device
 *
 * @author Stefan Triller
 *
 */
public class Communicator {
    private final Logger logger = LoggerFactory.getLogger(Communicator.class);

    private String ipAddress;
    private Header header;
    private StateUpdatedCallback callback;

    private Gson gson = new Gson();

    private ScheduledFuture<?> pollingJob;

    public Communicator(String ipAddress, Header header, StateUpdatedCallback callback) {
        this.ipAddress = ipAddress;
        this.header = header;
        this.callback = callback;
    }

    /**
     * Sends a request message to the device, reads the reply and informs the listener about the current device data
     */
    public void pollDataFromDevice() {
        String messageJson = gson.toJson(new Message(header));

        try (Socket socket = new Socket(ipAddress, VentaAirBindingConstants.PORT)) {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            byte[] dataToSend = buildMessageBytes(messageJson, "GET", "Complete");
            logger.debug("Sending request data message (String):\n{}", new String(dataToSend));
            logger.debug("Sending request data message (bytes): [{}]", HexUtils.bytesToHex(dataToSend, ", "));
            output.write(dataToSend);

            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String reply = "";
            while ((reply = br.readLine()) != null) {
                if (reply.startsWith("{")) {
                    // remove padding byte(s) after JSON data
                    String data = String.valueOf(reply.toCharArray(), 0, reply.length() - 1);
                    logger.debug("Got Data: {}", data);

                    DeviceInfoMessage deviceInfoMessage = gson.fromJson(data, DeviceInfoMessage.class);
                    if (deviceInfoMessage != null) {
                        callback.stateUpdated(deviceInfoMessage);
                    }
                }
            }
            socket.close();
        } catch (IOException e) {
            logger.debug("Communcation problem while polling data from device", e);
            callback.communicationProblem();
        }
    }

    private byte[] buildMessageBytes(String message, String method, String endpoint) throws IOException {
        ByteArrayOutputStream getInfoOutputStream = new ByteArrayOutputStream();
        getInfoOutputStream.write(createMessageHeader(method, endpoint, message.length()).getBytes());
        getInfoOutputStream.write(message.getBytes());
        getInfoOutputStream.write(new byte[] { 0x1c, 0x00 });
        return getInfoOutputStream.toByteArray();
    }

    private String createMessageHeader(String method, String endPoint, int contentLength) {
        return method + " /" + endPoint + "\n" + "Content-Length: " + contentLength + "\n" + "\n";
    }

    /**
     * Sends and {@link Action} to the device to set for example the FanSpeed or TargetHumidity
     *
     * @param action - The action to be send to the device
     */
    public void sendActionToDevice(Action action) throws IOException {
        CommandMessage message = new CommandMessage(action, header);

        String messageJson = gson.toJson(message);
        logger.debug("command message={}", messageJson);

        try (Socket socket = new Socket(ipAddress, VentaAirBindingConstants.PORT)) {
            OutputStream output = socket.getOutputStream();

            byte[] dataToSend = buildMessageBytes(messageJson, "POST", "Action");

            logger.debug("sending: {}", new String(dataToSend));
            logger.debug("sendingArray: {}", Arrays.toString(dataToSend));

            output.write(dataToSend);
            socket.close();
        }
    }

    /**
     * Starts the polling job to fetch the current device data
     *
     * @param scheduler - The scheduler of the {@link ThingHandler}
     */
    public void startPollDataFromDevice(ScheduledExecutorService scheduler) {
        stopPollDataFromDevice();
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDataFromDevice, 2, 10, TimeUnit.SECONDS);
    }

    /**
     * Stops the polling for device data
     */
    public void stopPollDataFromDevice() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob != null && !localPollingJob.isCancelled()) {
            logger.debug("Canceling polling job");
            localPollingJob.cancel(true);
        }
        logger.debug("Setting polling job to null");
        pollingJob = null;
    }
}
