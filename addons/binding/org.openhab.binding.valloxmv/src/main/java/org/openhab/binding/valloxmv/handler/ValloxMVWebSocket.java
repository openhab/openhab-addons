/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.valloxmv.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.valloxmv.ValloxMVBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ValloxMVWebSocket} is responsible for socket communication with the vallox ventilation unit
 *
 * @author Björn Brings - Initial contribution
 */
public class ValloxMVWebSocket {
    private String ip;
    private ValloxMVHandler voHandler;
    private Logger logger;
    private WebSocketClient client;
    private ValloxMVWebSocketListener socket;

    public ValloxMVWebSocket(ValloxMVHandler voHandler, String ip) {
        this.voHandler = voHandler;
        this.ip = ip;
        logger = LoggerFactory.getLogger(ValloxMVWebSocket.class);
        client = new WebSocketClient();
    }

    public void request(ChannelUID channelUID, String updateState) {
        try {
            socket = new ValloxMVWebSocketListener(channelUID, updateState);
            client.start();

            URI destUri = new URI("ws://" + ip + ":80");

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            logger.debug("Connecting to: {}", destUri);
            client.connect(socket, destUri, request);
            socket.awaitClose(10, TimeUnit.SECONDS);
        } catch (IOException e) {
            logger.error("Error: {}", e.getMessage().toString());
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage().toString());
        }
    }

    public void close() {
        try {
            client.stop();
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage().toString());
        }
        client.destroy();
        client = null;
    }

    @WebSocket
    public class ValloxMVWebSocketListener {
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        private Logger logger = LoggerFactory.getLogger(ValloxMVWebSocket.class);
        private String updateState;
        private ChannelUID channelUID;

        public ValloxMVWebSocketListener(ChannelUID channelUID, String updateState) {
            super();
            this.updateState = updateState;
            this.channelUID = channelUID;
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            voHandler.updateStatus(ThingStatus.ONLINE);
            try {
                logger.debug("Connect: {}", session.getRemoteAddress().getAddress());
                ByteBuffer buf = generateRequest();
                session.getRemote().sendBytes(buf);
            } catch (Exception e) {
                logger.error("Error: {}", e.getMessage().toString());
            }
        }

        /**
         * Method to generate ByteBuffer request to be sent to vallox online websocket
         * to request or set data
         *
         * @param mode 246 for data request, 249 for setting data
         * @param hmParameters HashMap for setting data with register as key and value as value
         * @return ByteBuffer to be sent to websocket
         */
        public ByteBuffer generateCustomRequest(Integer mode, HashMap<Integer, Integer> hmParameters) {
            // If we just want data the format is different so its just hardcoded here
            if (mode == 246) {
                // requestData (Length 3, Command to get data 246, empty set, checksum [sum of everything before])
                return ByteBuffer.wrap(new byte[] { 3, 0, (byte) 246, 0, 0, 0, (byte) 249, 0 });
            }

            int numberParameters = (hmParameters.size() + 1) * 2; // Parameters + Checksum
            int checksum = numberParameters;
            // Allocate for Content incl. Checksum + Length
            ByteBuffer bb = ByteBuffer.allocate((numberParameters + 1) * 2)
                    .put(convertIntegerIntoByteBuffer(numberParameters));

            // Put Mode, HashMap and checksum to ByteArray
            bb.put(convertIntegerIntoByteBuffer(mode));
            checksum += mode;

            for (Map.Entry<Integer, Integer> i : hmParameters.entrySet()) {
                bb.put(convertIntegerIntoByteBuffer(i.getKey()));
                bb.put(convertIntegerIntoByteBuffer(i.getValue()));
                checksum += i.getKey() + i.getValue();
            }

            bb.put(convertIntegerIntoByteBuffer(checksum));
            bb.position(0);
            return bb;
        }

        // Convert Integer to ByteBuffer in Little-endian
        public ByteBuffer convertIntegerIntoByteBuffer(Integer i) {
            byte b1 = (byte) (i % 256);
            byte b2 = (byte) ((i - i % 256) / 256);

            return ByteBuffer.wrap(new byte[] { b1, b2 });
        }

        public ByteBuffer generateRequest() {
            if (updateState == null || channelUID == null) {
                // requestData (Length 3, Command to get data 246, empty set, checksum [sum of everything before])
                return generateCustomRequest(246, new HashMap<Integer, Integer>());
            }
            if (channelUID.getId().equals(ValloxMVBindingConstants.CHANNEL_STATE)) {
                if (Integer.parseInt(updateState) == ValloxMVBindingConstants.STATE_FIREPLACE) {
                    // 15 Min fireplace (Length 6, Command to set data 249, CYC_BOOST_TIMER (4612) = 0,
                    // CYC_FIREPLACE_TIMER
                    // (4613) = 15, checksum)
                    HashMap<Integer, Integer> request = new HashMap<Integer, Integer>();
                    request.put(4612, 0);
                    request.put(4613, 15);
                    return generateCustomRequest(249, request);
                } else if (Integer.parseInt(updateState) == ValloxMVBindingConstants.STATE_ATHOME) {
                    // At Home (Length 8, Command to set data 249, CYC_STATE (4609) = 0, CYC_BOOST_TIMER (4612) = 0,
                    // CYC_FIREPLACE_TIMER (4613) = 0, checksum)
                    HashMap<Integer, Integer> request = new HashMap<Integer, Integer>();
                    request.put(4609, 0);
                    request.put(4612, 0);
                    request.put(4613, 0);
                    return generateCustomRequest(249, request);
                } else if (Integer.parseInt(updateState) == ValloxMVBindingConstants.STATE_AWAY) {
                    // Away (Length 8, Command to set data 249, CYC_STATE (4609) = 1, CYC_BOOST_TIMER (4612) = 0,
                    // CYC_FIREPLACE_TIMER (4613) = 0, checksum)
                    HashMap<Integer, Integer> request = new HashMap<Integer, Integer>();
                    request.put(4609, 1);
                    request.put(4612, 0);
                    request.put(4613, 0);
                    return generateCustomRequest(249, request);
                } else if (Integer.parseInt(updateState) == ValloxMVBindingConstants.STATE_BOOST) {
                    // 30 Min boost (Length 6, Command to set data 249, CYC_BOOST_TIMER (4612) = 30, CYC_FIREPLACE_TIMER
                    // (4613) = 0, checksum)
                    HashMap<Integer, Integer> request = new HashMap<Integer, Integer>();
                    request.put(4612, 30);
                    request.put(4613, 0);
                    return generateCustomRequest(249, request);
                }
            } else if (channelUID.getId().equals(ValloxMVBindingConstants.CHANNEL_ONOFF)) {
                HashMap<Integer, Integer> request = new HashMap<Integer, Integer>();
                request.put(4610, Integer.parseInt(updateState));
                return generateCustomRequest(249, request);
            }
            return null;
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.debug("Message from Server: {}", message);
        }

        @OnWebSocketError
        public void onError(Throwable cause) {
            voHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.error("Error: {}", cause.getMessage().toString());
            ValloxMVWebSocket.this.close();
        }

        @OnWebSocketMessage
        public void onBinary(InputStream in) {
            logger.debug("Got binary message");
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = in.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                buffer.flush();

                byte[] bytes = buffer.toByteArray();

                logger.debug("Response length: {}", bytes.length);

                // neglect non data responses
                if (updateState != null) {
                    if ((bytes[2] & 0xFF) == 245) {
                        logger.debug("State successfully updated to {} ", updateState);
                    } else {
                        logger.debug("State could not be updated");
                    }
                    return;
                }

                logger.debug("Fan Speed: {}", (bytes[129] & 0xFF));

                BigDecimal bdFanspeed = getNumber(bytes, 128);
                BigDecimal bdFanspeedExtract = getNumber(bytes, 144);
                BigDecimal bdFanspeedSupply = getNumber(bytes, 146);
                BigDecimal bdTempInside = getTemperature(bytes, 130);
                BigDecimal bdTempExhaust = getTemperature(bytes, 132);
                BigDecimal bdTempOutside = getTemperature(bytes, 134);
                BigDecimal bdTempIncomingBeforeHeating = getTemperature(bytes, 136);
                BigDecimal bdTempIncoming = getTemperature(bytes, 138);
                BigDecimal bdHumidity = getNumber(bytes, 166);

                BigDecimal bdStateOrig = getNumber(bytes, 214);
                BigDecimal bdBoostTimer = getNumber(bytes, 220);
                BigDecimal bdFireplaceTimer = getNumber(bytes, 222);

                BigDecimal bdCellstate = getNumber(bytes, 228);
                BigDecimal bdUptimeYears = getNumber(bytes, 230);
                BigDecimal bdUptimeHours = getNumber(bytes, 232);
                BigDecimal bdUptimeHoursCurrent = getNumber(bytes, 234);

                BigDecimal bdState;
                if (bytes[223] != 0) {
                    bdState = new BigDecimal(ValloxMVBindingConstants.STATE_FIREPLACE);
                } else if (bytes[221] != 0) {
                    bdState = new BigDecimal(ValloxMVBindingConstants.STATE_BOOST);
                } else if (bytes[215] == 1) {
                    bdState = new BigDecimal(ValloxMVBindingConstants.STATE_AWAY);
                } else {
                    bdState = new BigDecimal(ValloxMVBindingConstants.STATE_ATHOME);
                }

                OnOffType onoff;
                if (bytes[217] != 5) {
                    onoff = OnOffType.ON;
                } else {
                    onoff = OnOffType.OFF;
                }

                updateChannel(ValloxMVBindingConstants.CHANNEL_ONOFF, onoff);
                updateChannel(ValloxMVBindingConstants.CHANNEL_STATE, new DecimalType(bdState));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FAN_SPEED, new DecimalType(bdFanspeed));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FAN_SPEED_EXTRACT,
                        new DecimalType(bdFanspeedExtract));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FAN_SPEED_SUPPLY, new DecimalType(bdFanspeedSupply));
                updateChannel(ValloxMVBindingConstants.CHANNEL_TEMPERATURE_INSIDE,
                        new QuantityType<>(bdTempInside, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_TEMPERATURE_OUTSIDE,
                        new QuantityType<>(bdTempOutside, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_TEMPERATURE_EXHAUST,
                        new QuantityType<>(bdTempExhaust, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_TEMPERATURE_INCOMING_BEFORE_HEATING,
                        new QuantityType<>(bdTempIncomingBeforeHeating, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_TEMPERATURE_INCOMING,
                        new QuantityType<>(bdTempIncoming, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_HUMIDITY, new DecimalType(bdHumidity));
                updateChannel(ValloxMVBindingConstants.CHANNEL_CELLSTATE, new DecimalType(bdCellstate));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_YEARS, new DecimalType(bdUptimeYears));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_HOURS, new DecimalType(bdUptimeHours));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_HOURS_CURRENT,
                        new DecimalType(bdUptimeHoursCurrent));

                // logger.debug("Humidity: {}", bdHumidity);
                // logger.debug(
                // "FanSpeed: {}, Temp inside: {}, Temp outside: {}, Temp exhaust: {}, Temp incoming: {}, Humidity: {}",
                // bdFanspeed, bdTempInside, bdTempOutside, bdTempExhaust, bdTempIncoming, bdHumidity);
                logger.debug("Status: {} [State: {}, Boost timer: {}, Fireplace timer: {}]", bdState, bdStateOrig,
                        bdBoostTimer, bdFireplaceTimer);
                // logger.debug("Cellstate: {}, Uptime {}Y, {}h, Current Updtime {}h", bdCellstate, bdUptimeYears,
                // bdUptimeHours, bdUptimeHoursCurrent);

                // for (String key : ValloxMVBindingConstants.MAPADRESSRETURNBYTEARRAY.keySet()) {
                // logger.debug("Key: {}, Value: {}", key,
                // getNumber(bytes, ValloxMVBindingConstants.MAPADRESSRETURNBYTEARRAY.get(key)));
                // }
            } catch (Exception e) {
                logger.error("Error: {}", e.getStackTrace().toString());
            }
            logger.debug("Print final");
        }

        private void updateChannel(String strChannelName, State state) {
            Channel channel = voHandler.getThing().getChannel(strChannelName);
            if (channel != null) {
                voHandler.updateState(channel.getUID(), state);
            }
        }

        private BigDecimal getNumber(byte[] bytes, int pos) {
            return (new BigDecimal(bytes[pos] & 0xff)).multiply(new BigDecimal(256))
                    .add(new BigDecimal(bytes[pos + 1] & 0xff));
        }

        private BigDecimal getTemperature(byte[] bytes, int pos) {
            // Fetch 2 byte number out of bytearray representing the temperature in milli degree kelvin
            BigDecimal bdTempMiliKelvin = getNumber(bytes, pos);
            // Return number converted to degree celsius
            return bdTempMiliKelvin.divide(new BigDecimal(100))
                    .subtract((new BigDecimal(27315).divide(new BigDecimal(100))));
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.debug("WebSocket Closed. Code: {}", statusCode);
        }

        public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
            return this.closeLatch.await(duration, unit);
        }
    }
}
