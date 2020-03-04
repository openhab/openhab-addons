/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.valloxmv.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
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
import org.eclipse.smarthome.core.library.types.DateTimeType;
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
    private final ValloxMVHandler voHandler;
    private final WebSocketClient client;
    private final URI destUri;
    private ValloxMVWebSocketListener socket;

    private final Logger logger = LoggerFactory.getLogger(ValloxMVWebSocket.class);

    public ValloxMVWebSocket(WebSocketClient webSocketClient, ValloxMVHandler voHandler, String ip) {
        this.voHandler = voHandler;
        this.client = webSocketClient;
        URI tempUri;
        try {
            tempUri = new URI("ws://" + ip + ":80");
        } catch (URISyntaxException e) {
            tempUri = null;
            connectionError(e);
        }
        destUri = tempUri;
    }

    public void request(ChannelUID channelUID, String updateState) {
        try {
            socket = new ValloxMVWebSocketListener(channelUID, updateState);

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            logger.debug("Connecting to: {}", destUri);
            client.connect(socket, destUri, request);
            socket.awaitClose(2, TimeUnit.SECONDS);
        } catch (InterruptedException | IOException e) {
            connectionError(e);
        } catch (Exception e) {
            logger.debug("Unexpected error");
            connectionError(e);
        }
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
                // requestData (Length 3, Command to get data 246, 0, checksum [sum of everything before])
                return ByteBuffer.wrap(new byte[] { 3, 0, (byte) 246, 0, 0, 0, (byte) 249, 0 });
            }

            int numberParameters = (hmParameters.size() * 2) + 2; // Parameters (key + value) + Mode + Checksum
            int checksum = numberParameters;
            // Allocate for bytebuffer incl. Length
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

            // We have to make sure that checksum is within the range
            // Checksum may hold larger value than 65535 from the above loop
            // We will never reach integer max value in the loop so it is ok to do this after the loop
            // This is not needed if we make sure that conversion to bytes will take care of it
            // bitwise AND inside convertIntegerIntoByteBuffer() takes care of this
            // checksum = checksum & 0xffff; 

            bb.put(convertIntegerIntoByteBuffer(checksum));
            bb.position(0);
            return bb;
        }

        // Convert Integer to ByteBuffer in Little-endian
        public ByteBuffer convertIntegerIntoByteBuffer(Integer i) {
            // Use bitwise operators to extract two rightmost bytes from the integer
            byte b1 = (byte) (i & 0xff); // Rightmost byte
            byte b2 = (byte) ((i >> 8) & 0xff); // Second rightmost byte

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
                    // CYC_FIREPLACE_TIMER (4613) = 15, checksum)
                    // To do: we should check this case already in 'request' function and first request Vallox
                    // to provide value from modbus address 20545. Data to send: (3,250,20545,checksum)
                    // Vallox will reply with binary data (4,249,20545,value,checksum) from here we can save value to
                    // variable. After this 'request' function can request this state update, here we can use variable
                    // for setting timer value to modbus address 4613.
                    // Note: we should be able to request 2 values at a time, same if clause could be used for both
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
                    // To do: we should check this case already in 'request' function and first request Vallox
                    // to provide value from modbus address 20544. Data to send: (3,250,20544,checksum)
                    // Vallox will reply with binary data (4,249,20544,value,checksum) from here we can save value to
                    // variable. After this 'request' function can request this state update, here we can use variable
                    // for setting timer value to modbus address 4612.
                    // Note: we should be able to request 2 values at a time, same if clause could be used for both
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
            } else if (ValloxMVBindingConstants.CHANNEL_AWAY_SPEED_SETTING.equals(channelUID.getId())) {
                request.put(20501, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_BOOST_SPEED_SETTING.equals(channelUID.getId())) {
                request.put(20513, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_HOME_AIR_TEMP_TARGET.equals(channelUID.getId())) {
                request.put(20508, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_AWAY_AIR_TEMP_TARGET.equals(channelUID.getId())) {
                request.put(20502, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_BOOST_AIR_TEMP_TARGET.equals(channelUID.getId())) {
                request.put(20514, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_BOOST_TIME.equals(channelUID.getId())) {
                request.put(20544, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_BOOST_TIMER_ENABLED.equals(channelUID.getId())) {
                request.put(21766, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_FIREPLACE_EXTR_FAN.equals(channelUID.getId())) {
                request.put(20487, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_FIREPLACE_SUPP_FAN.equals(channelUID.getId())) {
                request.put(20488, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_FIREPLACE_TIME.equals(channelUID.getId())) {
                request.put(20545, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_FIREPLACE_TIMER_ENABLED.equals(channelUID.getId())) {
                request.put(21767, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_EXTRA_AIR_TEMP_TARGET.equals(channelUID.getId())) {
                request.put(20493, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_EXTRA_EXTR_FAN.equals(channelUID.getId())) {
                request.put(20494, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_EXTRA_SUPP_FAN.equals(channelUID.getId())) {
                request.put(20495, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_EXTRA_TIME.equals(channelUID.getId())) {
                request.put(20496, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_EXTRA_TIMER_ENABLED.equals(channelUID.getId())) {
                request.put(21772, Integer.parseInt(updateState));
            } else if (ValloxMVBindingConstants.CHANNEL_WEEKLY_TIMER_ENABLED.equals(channelUID.getId())) {
                request.put(4615, Integer.parseInt(updateState));
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

                int iFanspeed = getNumber(bytes, 128);
                int iFanspeedExtract = getNumber(bytes, 144);
                int iFanspeedSupply = getNumber(bytes, 146);
                BigDecimal bdTempInside = getTemperature(bytes, 130);
                BigDecimal bdTempExhaust = getTemperature(bytes, 132);
                BigDecimal bdTempOutside = getTemperature(bytes, 134);
                BigDecimal bdTempIncomingBeforeHeating = getTemperature(bytes, 136);
                BigDecimal bdTempIncoming = getTemperature(bytes, 138);
                int iHumidity = getNumber(bytes, 166);

                int iStateOrig = getNumber(bytes, 214);
                int iBoostTimer = getNumber(bytes, 220);
                int iFireplaceTimer = getNumber(bytes, 222);

                int iCellstate = getNumber(bytes, 228);
                int iUptimeYears = getNumber(bytes, 230);
                int iUptimeHours = getNumber(bytes, 232);
                int iUptimeHoursCurrent = getNumber(bytes, 234);

                int iRemainingTimeForFilter = getNumber(bytes, 236);
                int iFilterChangedDateDay = getNumber(bytes, 496);
                int iFilterChangedDateMonth = getNumber(bytes, 498);
                int iFilterChangedDateYear = getNumber(bytes, 500);

                Calendar cFilterChangedDate = Calendar.getInstance();
                cFilterChangedDate.set(iFilterChangedDateYear + 2000,
                        iFilterChangedDateMonth - 1 /* Month is 0-based */, iFilterChangedDateDay, 0, 0, 0);

                int iExtrFanBalanceBase = getNumber(bytes, 374);
                int iSuppFanBalanceBase = getNumber(bytes, 376);

                int iHomeSpeedSetting = getNumber(bytes, 418);
                int iAwaySpeedSetting = getNumber(bytes, 406);
                int iBoostSpeedSetting = getNumber(bytes, 430);
                BigDecimal bdHomeAirTempTarget = getTemperature(bytes, 420);
                BigDecimal bdAwayAirTempTarget = getTemperature(bytes, 408);
                BigDecimal bdBoostAirTempTarget = getTemperature(bytes, 432);

                int iBoostTime = getNumber(bytes, 492);
                int iBoostTimerEnabled = getNumber(bytes, 528);
                int iFireplaceExtrFan = getNumber(bytes, 378);
                int iFireplaceSuppFan = getNumber(bytes, 380);
                int iFireplaceTime = getNumber(bytes, 494);
                int iFireplaceTimerEnabled = getNumber(bytes, 530);
                BigDecimal bdExtraAirTempTarget = getTemperature(bytes, 390);
                int iExtraExtrFan = getNumber(bytes, 392);
                int iExtraSuppFan = getNumber(bytes, 394);
                int iExtraTime = getNumber(bytes, 396);
                int iExtraTimerEnabled = getNumber(bytes, 540);
                int iWeeklyTimerEnabled = getNumber(bytes, 226);

                BigDecimal bdState;
                if (iFireplaceTimer > 0) {
                    bdState = new BigDecimal(ValloxMVBindingConstants.STATE_FIREPLACE);
                } else if (iBoostTimer > 0) {
                    bdState = new BigDecimal(ValloxMVBindingConstants.STATE_BOOST);
                } else if (iStateOrig == 1) {
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
                        new QuantityType<>(iFanspeed, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FAN_SPEED_EXTRACT, new DecimalType(iFanspeedExtract));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FAN_SPEED_SUPPLY, new DecimalType(iFanspeedSupply));
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
                        new QuantityType<>(iHumidity, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_CELLSTATE, new DecimalType(iCellstate));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_YEARS, new DecimalType(iUptimeYears));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_HOURS, new DecimalType(iUptimeHours));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_HOURS_CURRENT,
                        new DecimalType(iUptimeHoursCurrent));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FILTER_CHANGED_DATE, new DateTimeType(
                        ZonedDateTime.ofInstant(cFilterChangedDate.toInstant(), TimeZone.getDefault().toZoneId())));
                updateChannel(ValloxMVBindingConstants.CHANNEL_REMAINING_FILTER_DAYS,
                        new QuantityType<>(iRemainingTimeForFilter, SmartHomeUnits.DAY));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTR_FAN_BALANCE_BASE,
                        new QuantityType<>(iExtrFanBalanceBase, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_SUPP_FAN_BALANCE_BASE,
                        new QuantityType<>(iSuppFanBalanceBase, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_HOME_SPEED_SETTING,
                        new QuantityType<>(iHomeSpeedSetting, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_AWAY_SPEED_SETTING,
                        new QuantityType<>(iAwaySpeedSetting, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_BOOST_SPEED_SETTING,
                        new QuantityType<>(iBoostSpeedSetting, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_HOME_AIR_TEMP_TARGET,
                        new QuantityType<>(bdHomeAirTempTarget, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_AWAY_AIR_TEMP_TARGET,
                        new QuantityType<>(bdAwayAirTempTarget, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_BOOST_AIR_TEMP_TARGET,
                        new QuantityType<>(bdBoostAirTempTarget, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_BOOST_TIME, new DecimalType(iBoostTime));
                updateChannel(ValloxMVBindingConstants.CHANNEL_BOOST_TIMER_ENABLED,
                        new DecimalType(iBoostTimerEnabled));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FIREPLACE_EXTR_FAN,
                        new QuantityType<>(iFireplaceExtrFan, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FIREPLACE_SUPP_FAN,
                        new QuantityType<>(iFireplaceSuppFan, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FIREPLACE_TIME, new DecimalType(iFireplaceTime));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FIREPLACE_TIMER_ENABLED,
                        new DecimalType(iFireplaceTimerEnabled));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_AIR_TEMP_TARGET,
                        new QuantityType<>(bdExtraAirTempTarget, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_EXTR_FAN,
                        new QuantityType<>(iExtraExtrFan, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_SUPP_FAN,
                        new QuantityType<>(iExtraSuppFan, SmartHomeUnits.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_TIME, new DecimalType(iExtraTime));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_TIMER_ENABLED,
                        new DecimalType(iExtraTimerEnabled));
                updateChannel(ValloxMVBindingConstants.CHANNEL_WEEKLY_TIMER_ENABLED,
                        new DecimalType(iWeeklyTimerEnabled));

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
            // Fetch 2 byte number out of bytearray representing the temperature in centKelvin
            BigDecimal bdTemperatureCentKelvin = new BigDecimal(getNumber(bytes, pos));
            // Return number converted to degree celsius (= (centKelvin - 27315) / 100 )
            return (new QuantityType<>(bdTemperatureCentKelvin, MetricPrefix.CENTI(SmartHomeUnits.KELVIN))
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
