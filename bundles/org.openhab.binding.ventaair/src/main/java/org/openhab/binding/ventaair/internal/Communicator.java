/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.math.BigDecimal;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ventaair.internal.VentaThingHandler.StateUpdatedCallback;
import org.openhab.binding.ventaair.internal.message.action.Action;
import org.openhab.binding.ventaair.internal.message.dto.CommandMessage;
import org.openhab.binding.ventaair.internal.message.dto.DeviceInfoMessage;
import org.openhab.binding.ventaair.internal.message.dto.Header;
import org.openhab.binding.ventaair.internal.message.dto.Message;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Communicator} is responsible for sending/receiving commands to/from the device
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class Communicator {
    private static final Duration COMMUNICATION_TIMEOUT = Duration.ofSeconds(5);

    private final Logger logger = LoggerFactory.getLogger(Communicator.class);

    private @Nullable String ipAddress;
    private Header header;
    private int pollingTimeInSeconds;
    private StateUpdatedCallback callback;

    private Gson gson = new Gson();

    private @Nullable ScheduledFuture<?> pollingJob;

    public Communicator(@Nullable String ipAddress, Header header, @Nullable BigDecimal pollingTime,
            StateUpdatedCallback callback) {
        this.ipAddress = ipAddress;
        this.header = header;
        if (pollingTime != null) {
            this.pollingTimeInSeconds = pollingTime.intValue();
        } else {
            this.pollingTimeInSeconds = 60;
        }
        this.callback = callback;
    }

    /**
     * Sends a request message to the device, reads the reply and informs the listener about the current device data
     */
    public void pollDataFromDevice() {
        String messageJson = gson.toJson(new Message(header));

        try (Socket socket = new Socket(ipAddress, VentaAirBindingConstants.PORT)) {
            socket.setSoTimeout((int) COMMUNICATION_TIMEOUT.toMillis());
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            byte[] dataToSend = buildMessageBytes(messageJson, "GET", "Complete");
            // we write these lines to the log in order to help users with new/other venta devices, so they only need to
            // enable debug logging
            logger.debug("Sending request data message (String):\n{}", new String(dataToSend));
            logger.debug("Sending request data message (bytes): [{}]", HexUtils.bytesToHex(dataToSend, ", "));
            output.write(dataToSend);

            BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            String reply = "";
            while ((reply = br.readLine()) != null) {
                if (reply.startsWith("{")) {
                    // remove padding byte(s) after JSON data
                    String data = String.valueOf(reply.toCharArray(), 0, reply.length() - 1);
                    // we write this line to the log in order to help users with new/other venta devices, so they only
                    // need to enable debug logging
                    logger.debug("Got Data from device: {}", data);

                    DeviceInfoMessage deviceInfoMessage = gson.fromJson(data, DeviceInfoMessage.class);
                    if (deviceInfoMessage != null) {
                        callback.stateUpdated(deviceInfoMessage);
                    }
                }
            }
            br.close();
            socket.close();
        } catch (IOException e) {
            callback.communicationProblem();
        }
    }

    private byte[] buildMessageBytes(String message, String method, String endpoint) throws IOException {
        ByteArrayOutputStream getInfoOutputStream = new ByteArrayOutputStream();
        getInfoOutputStream
                .write(createMessageHeader(method, endpoint, message.length()).getBytes(StandardCharsets.UTF_8));
        getInfoOutputStream.write(message.getBytes(StandardCharsets.UTF_8));
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

        try (Socket socket = new Socket(ipAddress, VentaAirBindingConstants.PORT)) {
            OutputStream output = socket.getOutputStream();

            byte[] dataToSend = buildMessageBytes(messageJson, "POST", "Action");

            // we write these lines to the log in order to help users with new/other venta devices, so they only need to
            // enable debug logging
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
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollDataFromDevice, 2, pollingTimeInSeconds,
                TimeUnit.SECONDS);
    }

    /**
     * Stops the polling for device data
     */
    public void stopPollDataFromDevice() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob != null && !localPollingJob.isCancelled()) {
            localPollingJob.cancel(true);
        }
        logger.debug("Setting polling job to null");
        pollingJob = null;
    }
}
