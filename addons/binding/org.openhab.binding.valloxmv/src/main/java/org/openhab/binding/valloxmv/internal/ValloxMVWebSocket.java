/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.valloxmv.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ValloxMVWebSocket} is responsible for socket communication with the vallox ventilation unit
 *
 * @author Björn Brings - Initial contribution
 */
public class ValloxMVWebSocket {
    private final String ip;
    private final ValloxMVHandler voHandler;
    private WebSocketClient client;
    private ValloxMVWebSocketListener socket;

    private final Logger logger = LoggerFactory.getLogger(ValloxMVWebSocket.class);

    public ValloxMVWebSocket(ValloxMVHandler voHandler, String ip) {
        this.voHandler = voHandler;
        this.ip = ip;
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
            socket.awaitClose(2, TimeUnit.SECONDS);
        } catch (URISyntaxException | InterruptedException | IOException e) {
            connectionError(e);
        } catch (Exception e) {
            logger.debug("Unexpected error");
            connectionError(e);
        }
    }

    public void close() {
        try {
            client.stop();
        } catch (Exception e) {
            logger.debug("Error while closing connection: {}", e);
        }
        client.destroy();
        client = null;
    }

    public void connectionError(Exception e) {
        logger.debug("Error connecting vallox unit.", e);
        voHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
    }

    @WebSocket
    public class ValloxMVWebSocketListener {
        private final CountDownLatch closeLatch = new CountDownLatch(1);

        private final Logger logger = LoggerFactory.getLogger(ValloxMVWebSocketListener.class);
        private final String updateState;
        private ChannelUID channelUID;

        public ValloxMVWebSocketListener(ChannelUID channelUID, String updateState) {
            this.updateState = updateState;
            this.channelUID = channelUID;
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            try {
                logger.debug("Connect: {}", session.getRemoteAddress().getAddress());
                ByteBuffer buf = generateRequest();
                session.getRemote().sendBytes(buf);
            } catch (IOException e) {
                connectionError(e);
            }
        }

        /**
         * Method to generate ByteBuffer request to be sent to vallox online websocket
         * to request or set data
         *
         * @param mode         246 for data request, 249 for setting data
         * @param hmParameters HashMap for setting data with register as key and value as value
         * @return ByteBuffer to be sent to websocket
         */
        public ByteBuffer generateCustomRequest(Integer mode, Map<Integer, Integer> hmParameters) {
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
            Map<Integer, Integer> request = new HashMap<>();
            if (ValloxMVBindingConstants.CHANNEL_STATE.equals(channelUID.getId())) {
                if (Integer.parseInt(updateState) == ValloxMVBindingConstants.STATE_FIREPLACE) {
                    // 15 Min fireplace (Length 6, Command to set data 249, CYC_BOOST_TIMER (4612) = 0,
                    // CYC_FIREPLACE_TIMER
                    // (4613) = 15, checksum)
                    request.put(4612, 0);
                    request.put(4613, 15);
                } else if (Integer.parseInt(updateState) == ValloxMVBindingConstants.STATE_ATHOME) {
                    // At Home (Length 8, Command to set data 249, CYC_STATE (4609) = 0, CYC_BOOST_TIMER (4612) = 0,
                    // CYC_FIREPLACE_TIMER (4613) = 0, checksum)
                    request.put(4609, 0);
                    request.put(4612, 0);
                    request.put(4613, 0);
                } else if (Integer.parseInt(updateState) == ValloxMVBindingConstants.STATE_AWAY) {
                    // Away (Length 8, Command to set data 249, CYC_STATE (4609) = 1, CYC_BOOST_TIMER (4612) = 0,
                    // CYC_FIREPLACE_TIMER (4613) = 0, checksum)
                    request.put(4609, 1);
                    request.put(4612, 0);
                    request.put(4613, 0);
                } else if (Integer.parseInt(updateState) == ValloxMVBindingConstants.STATE_BOOST) {
                    // 30 Min boost (Length 6, Command to set data 249, CYC_BOOST_TIMER (4612) = 30, CYC_FIREPLACE_TIMER
                    // (4613) = 0, checksum)
                    request.put(4612, 30);
                    request.put(4613, 0);
                }
            } else if (ValloxMVBindingConstants.CHANNEL_ONOFF.equals(channelUID.getId())) {
                request.put(4610, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_EXTR_FAN_BALANCE_BASE.equals(channelUID.getId())) {
                request.put(20485, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_SUPP_FAN_BALANCE_BASE.equals(channelUID.getId())) {
                request.put(20486, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_HOME_SPEED_SETTING.equals(channelUID.getId())) {
                request.put(20507, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_HOME_AIR_TEMP_TARGET.equals(channelUID.getId())) {
                request.put(20508, Integer.parseInt(updateState));
            } else {
                return null;
            }
            return generateCustomRequest(249, request);
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            logger.debug("Message from Server: {}", message);
        }

        @OnWebSocketError
        public void onError(Throwable cause) {
            voHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.debug("Connection failed: {}", cause.getMessage());
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

                int bdFanspeed = getNumber(bytes, 128);
                int bdFanspeedExtract = getNumber(bytes, 144);
                int bdFanspeedSupply = getNumber(bytes, 146);
                BigDecimal bdTempInside = getTemperature(bytes, 130);
                BigDecimal bdTempExhaust = getTemperature(bytes, 132);
                BigDecimal bdTempOutside = getTemperature(bytes, 134);
                BigDecimal bdTempIncomingBeforeHeating = getTemperature(bytes, 136);
                BigDecimal bdTempIncoming = getTemperature(bytes, 138);
                int bdHumidity = getNumber(bytes, 166);

                int bdStateOrig = getNumber(bytes, 214);
                int bdBoostTimer = getNumber(bytes, 220);
                int bdFireplaceTimer = getNumber(bytes, 222);

                int bdCellstate = getNumber(bytes, 228);
                int bdUptimeYears = getNumber(bytes, 230);
                int bdUptimeHours = getNumber(bytes, 232);
                int bdUptimeHoursCurrent = getNumber(bytes, 234);

                int bdExtrFanBalanceBase = getNumber(bytes, 374);
                int bdSuppFanBalanceBase = getNumber(bytes, 376);

                int bdHomeSpeedSetting = getNumber(bytes, 418);
                BigDecimal bdHomeAirTempTarget = getTemperature(bytes, 420);

                BigDecimal bdState;
                if (bdFireplaceTimer > 0) {
                    bdState = new BigDecimal(ValloxMVBindingConstants.STATE_FIREPLACE);
                } else if (bdBoostTimer > 0) {
                    bdState = new BigDecimal(ValloxMVBindingConstants.STATE_BOOST);
                } else if (bdStateOrig == 1) {
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
                updateChannel(ValloxMVBindingConstants.CHANNEL_FAN_SPEED,
                        new QuantityType<>(bdFanspeed, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FAN_SPEED_EXTRACT, new DecimalType(bdFanspeedExtract));
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
                updateChannel(ValloxMVBindingConstants.CHANNEL_HUMIDITY,
                        new QuantityType<>(bdHumidity, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_CELLSTATE, new DecimalType(bdCellstate));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_YEARS, new DecimalType(bdUptimeYears));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_HOURS, new DecimalType(bdUptimeHours));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_HOURS_CURRENT,
                        new DecimalType(bdUptimeHoursCurrent));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTR_FAN_BALANCE_BASE,
                        new QuantityType<>(bdExtrFanBalanceBase, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_SUPP_FAN_BALANCE_BASE,
                        new QuantityType<>(bdSuppFanBalanceBase, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_HOME_SPEED_SETTING,
                        new QuantityType<>(bdHomeSpeedSetting, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_HOME_AIR_TEMP_TARGET,
                        new QuantityType<>(bdHomeAirTempTarget, SIUnits.CELSIUS));

                voHandler.updateStatus(ThingStatus.ONLINE);
                voHandler.dataUpdated();
            } catch (IOException e) {
                connectionError(e);
            }
            logger.debug("Data updated successfully");
        }

        private void updateChannel(String strChannelName, State state) {
            voHandler.updateState(strChannelName, state);
        }

        private int getNumber(byte[] bytes, int pos) {
            return (bytes[pos] & 0xff) * 256 + (bytes[pos + 1] & 0xff);
        }

        @SuppressWarnings("null")
        private BigDecimal getTemperature(byte[] bytes, int pos) {
            // Fetch 2 byte number out of bytearray representing the temperature in milli degree kelvin
            BigDecimal bdTemperatureMiliKelvin = new BigDecimal(getNumber(bytes, pos));
            // Return number converted to degree celsius
            return (new QuantityType<>(bdTemperatureMiliKelvin, MetricPrefix.CENTI(SmartHomeUnits.KELVIN))
                    .toUnit(SIUnits.CELSIUS)).toBigDecimal();
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.debug("WebSocket Closed. Code: {}; Reason: {}", statusCode, reason);
        }

        public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
            return this.closeLatch.await(duration, unit);
        }
    }
}
