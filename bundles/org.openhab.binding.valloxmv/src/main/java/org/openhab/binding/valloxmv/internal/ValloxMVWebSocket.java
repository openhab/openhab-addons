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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.State;
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

    private int iBoostTime;
    private OnOffType ooBoostTimerEnabled;
    private int iFireplaceTime;
    private OnOffType ooFireplaceTimerEnabled;

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
        Future<?> sessionFuture = null;
        try {
            socket = new ValloxMVWebSocketListener(channelUID, updateState);

            ClientUpgradeRequest request = new ClientUpgradeRequest();
            logger.debug("Connecting to: {}", destUri);
            sessionFuture = client.connect(socket, destUri, request);
            socket.awaitClose(2, TimeUnit.SECONDS);
        } catch (InterruptedException | IOException e) {
            connectionError(e);
        } catch (Exception e) {
            logger.debug("Unexpected error");
            connectionError(e);
        } finally {
            if (sessionFuture != null && !sessionFuture.isDone()) {
                sessionFuture.cancel(true);
            }
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
        private int iValloxCmd;

        public ValloxMVWebSocketListener(ChannelUID channelUID, String updateState) {
            this.updateState = updateState;
            this.channelUID = channelUID;
        }

        @OnWebSocketConnect
        public void onConnect(Session session) {
            try {
                logger.debug("Connected to: {}", session.getRemoteAddress().getAddress());
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
         * @param mode 246 for data request, 249 for setting data
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
            if ((updateState == null) || (channelUID == null)) {
                // requestData (Length 3, Command to get data 246, empty set, checksum [sum of everything before])
                iValloxCmd = 246;
                return generateCustomRequest(246, new HashMap<>());
            }
            String strChannelUIDid = channelUID.getId();
            int iUpdateState = Integer.parseInt(updateState);
            Map<Integer, Integer> request = new HashMap<>();
            switch (strChannelUIDid) {
                case ValloxMVBindingConstants.CHANNEL_STATE:
                    switch (iUpdateState) {
                        case ValloxMVBindingConstants.STATE_FIREPLACE:
                            // Fireplace (Length 6, Command to set data 249, CYC_BOOST_TIMER (4612) = 0,
                            // CYC_FIREPLACE_TIMER (4613) = value from CYC_FIREPLACE_TIME, checksum)
                            // CYC_FIREPLACE_TIME is read during READ_TABLES and stored into outer class variable
                            if (iFireplaceTime < 1) {
                                // use 15 minutes in case not initialized (should never happen)
                                iFireplaceTime = 15;
                            }
                            if (OnOffType.ON.equals(ooFireplaceTimerEnabled)) {
                                logger.debug("Changing to Fireplace profile, timer {} minutes", iFireplaceTime);
                            } else {
                                logger.debug("Changing to Fireplace profile, timer not enabled");
                            }
                            request.put(4612, 0);
                            request.put(4613, iFireplaceTime);
                            break;
                        case ValloxMVBindingConstants.STATE_ATHOME:
                            // At Home (Length 8, Command to set data 249, CYC_STATE (4609) = 0,
                            // CYC_BOOST_TIMER (4612) = 0, CYC_FIREPLACE_TIMER (4613) = 0, checksum)
                            logger.debug("Changing to At Home profile");
                            request.put(4609, 0);
                            request.put(4612, 0);
                            request.put(4613, 0);
                            break;
                        case ValloxMVBindingConstants.STATE_AWAY:
                            // Away (Length 8, Command to set data 249, CYC_STATE (4609) = 1,
                            // CYC_BOOST_TIMER (4612) = 0, CYC_FIREPLACE_TIMER (4613) = 0, checksum)
                            logger.debug("Changing to Away profile");
                            request.put(4609, 1);
                            request.put(4612, 0);
                            request.put(4613, 0);
                            break;
                        case ValloxMVBindingConstants.STATE_BOOST:
                            // Boost (Length 6, Command to set data 249,
                            // CYC_BOOST_TIMER (4612) = value from CYC_BOOST_TIME,
                            // CYC_FIREPLACE_TIMER (4613) = 0, checksum)
                            // CYC_BOOST_TIME is read during READ_TABLES and stored into outer class variable
                            if (iBoostTime < 1) {
                                // use 30 minutes in case not initialized (should never happen)
                                iBoostTime = 30;
                            }
                            if (OnOffType.ON.equals(ooBoostTimerEnabled)) {
                                logger.debug("Changing to Boost profile, timer {} minutes", iBoostTime);
                            } else {
                                logger.debug("Changing to Boost profile, timer not enabled");
                            }
                            request.put(4612, iBoostTime);
                            request.put(4613, 0);
                            break;
                        default:
                            // This should never happen. Let's get back to basic profile.
                            // Clearing boost and fireplace timers.
                            logger.debug("Incorrect profile requested, changing back to basic profile");
                            request.put(4612, 0);
                            request.put(4613, 0);
                    }
                    break;
                case ValloxMVBindingConstants.CHANNEL_ONOFF:
                    request.put(4610, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_EXTR_FAN_BALANCE_BASE:
                    request.put(20485, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_SUPP_FAN_BALANCE_BASE:
                    request.put(20486, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_HOME_SPEED_SETTING:
                    request.put(20507, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_AWAY_SPEED_SETTING:
                    request.put(20501, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_BOOST_SPEED_SETTING:
                    request.put(20513, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_HOME_AIR_TEMP_TARGET:
                    request.put(20508, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_AWAY_AIR_TEMP_TARGET:
                    request.put(20502, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_BOOST_AIR_TEMP_TARGET:
                    request.put(20514, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_BOOST_TIME:
                    iBoostTime = iUpdateState;
                    request.put(20544, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_BOOST_TIMER_ENABLED:
                    ooBoostTimerEnabled = OnOffType.from(Integer.toString(iUpdateState));
                    request.put(21766, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_FIREPLACE_EXTR_FAN:
                    request.put(20487, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_FIREPLACE_SUPP_FAN:
                    request.put(20488, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_FIREPLACE_TIME:
                    iFireplaceTime = iUpdateState;
                    request.put(20545, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_FIREPLACE_TIMER_ENABLED:
                    ooFireplaceTimerEnabled = OnOffType.from(Integer.toString(iUpdateState));
                    request.put(21767, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_EXTRA_AIR_TEMP_TARGET:
                    request.put(20493, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_EXTRA_EXTR_FAN:
                    request.put(20494, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_EXTRA_SUPP_FAN:
                    request.put(20495, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_EXTRA_TIME:
                    request.put(20496, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_EXTRA_TIMER_ENABLED:
                    request.put(21772, iUpdateState);
                    break;
                case ValloxMVBindingConstants.CHANNEL_WEEKLY_TIMER_ENABLED:
                    request.put(4615, iUpdateState);
                    break;
                default:
                    return null;
            }
            iValloxCmd = 249;
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

                if ((bytes.length > 5) && (bytes.length % 2 == 0)) {
                    logger.debug("Response length: {} bytes", bytes.length);
                } else {
                    logger.debug("Response corrupted, length: {} bytes", bytes.length);
                    return;
                }

                int iDataLength = bytes.length / 2;

                // Verify responses to requests
                if ((iValloxCmd == 249) || (iValloxCmd == 250)) {
                    // COMMAND_WRITE_DATA (249) or COMMAND_READ_DATA (250)
                    int iChecksum = 0;
                    int[] arriData = new int[iDataLength];
                    for (int i = 0; i < iDataLength; i++) {
                        arriData[i] = getNumberLE(bytes, (i * 2));
                    }
                    for (int i = 0; i < (iDataLength - 1); i++) {
                        iChecksum += arriData[i];
                    }
                    iChecksum &= 0xffff;
                    if ((arriData[0] != (iDataLength - 1)) || (arriData[iDataLength - 1] != iChecksum)) {
                        // Data length or Checksum do not match
                        logger.debug("Response corrupted, Data length or Checksum do not match");
                        return;
                    }
                    // COMMAND_WRITE_DATA (249)
                    if (iValloxCmd == 249) {
                        String strChannelUIDid = channelUID.getId();
                        if (arriData[1] == 245) {
                            // ACK
                            logger.debug("Channel {} successfully updated to {}", strChannelUIDid, updateState);
                        } else {
                            logger.debug("Channel {} could not be updated", strChannelUIDid);
                        }
                        return;
                    }
                    // COMMAND_READ_DATA (250)
                    /* Read data command is not implemented, response check is ready for future implementation
                    // @formatter:off
                    if ((((iDataLength - 1) % 2) != 0) || (iDataLength < 5)) {
                        logger.debug("Response corrupted, data length is wrong");
                        return;
                    }
                    // number of data pairs (address, value) = (iDataLength - 3) / 2
                    // First data pair (address, value) is in positions 2 and 3
                    // (address, value) pairs could be put into array or hashmap
                    // @formatter:on
                    */
                    logger.debug("Vallox command {} not implemented", iValloxCmd);
                    return;
                } else if (iValloxCmd == 246) {
                    // COMMAND_READ_TABLES (246)
                    if (iDataLength > 704) {
                        logger.debug("Data table response with {} values, updating to channels", iDataLength);
                    } else {
                        logger.debug("Response corrupted, data table response not complete");
                        return;
                    }
                } else {
                    logger.debug("Vallox command {} not implemented", iValloxCmd);
                    return;
                }

                // COMMAND_READ_TABLES (246)
                // Read values from received tables
                int iFanspeed = getNumberBE(bytes, 128);
                int iFanspeedExtract = getNumberBE(bytes, 144);
                int iFanspeedSupply = getNumberBE(bytes, 146);
                BigDecimal bdTempInside = getTemperature(bytes, 130);
                BigDecimal bdTempExhaust = getTemperature(bytes, 132);
                BigDecimal bdTempOutside = getTemperature(bytes, 134);
                BigDecimal bdTempIncomingBeforeHeating = getTemperature(bytes, 136);
                BigDecimal bdTempIncoming = getTemperature(bytes, 138);

                int iHumidity = getNumberBE(bytes, 148);
                @SuppressWarnings("unused")
                int iCo2 = getNumberBE(bytes, 150);

                int iStateOrig = getNumberBE(bytes, 214);
                int iBoostTimer = getNumberBE(bytes, 220);
                int iFireplaceTimer = getNumberBE(bytes, 222);

                int iCellstate = getNumberBE(bytes, 228);
                int iUptimeYears = getNumberBE(bytes, 230);
                int iUptimeHours = getNumberBE(bytes, 232);
                int iUptimeHoursCurrent = getNumberBE(bytes, 234);

                int iRemainingTimeForFilter = getNumberBE(bytes, 236);
                int iFilterChangedDateDay = getNumberBE(bytes, 496);
                int iFilterChangedDateMonth = getNumberBE(bytes, 498);
                int iFilterChangedDateYear = getNumberBE(bytes, 500);

                Calendar cFilterChangedDate = Calendar.getInstance();
                cFilterChangedDate.set(iFilterChangedDateYear + 2000,
                        iFilterChangedDateMonth - 1 /* Month is 0-based */, iFilterChangedDateDay, 0, 0, 0);

                int iExtrFanBalanceBase = getNumberBE(bytes, 374);
                int iSuppFanBalanceBase = getNumberBE(bytes, 376);

                int iHomeSpeedSetting = getNumberBE(bytes, 418);
                int iAwaySpeedSetting = getNumberBE(bytes, 406);
                int iBoostSpeedSetting = getNumberBE(bytes, 430);
                BigDecimal bdHomeAirTempTarget = getTemperature(bytes, 420);
                BigDecimal bdAwayAirTempTarget = getTemperature(bytes, 408);
                BigDecimal bdBoostAirTempTarget = getTemperature(bytes, 432);

                // Using outer class variable for boost time and timer enabled
                iBoostTime = getNumberBE(bytes, 492);
                ooBoostTimerEnabled = OnOffType.from(Integer.toString(getNumberBE(bytes, 528)));
                int iFireplaceExtrFan = getNumberBE(bytes, 378);
                int iFireplaceSuppFan = getNumberBE(bytes, 380);
                // Using outer class variable for fireplace time and timer enabled
                iFireplaceTime = getNumberBE(bytes, 494);
                ooFireplaceTimerEnabled = OnOffType.from(Integer.toString(getNumberBE(bytes, 530)));
                BigDecimal bdExtraAirTempTarget = getTemperature(bytes, 390);
                int iExtraExtrFan = getNumberBE(bytes, 392);
                int iExtraSuppFan = getNumberBE(bytes, 394);
                int iExtraTime = getNumberBE(bytes, 396);
                OnOffType ooExtraTimerEnabled = OnOffType.from(Integer.toString(getNumberBE(bytes, 540)));
                OnOffType ooWeeklyTimerEnabled = OnOffType.from(Integer.toString(getNumberBE(bytes, 226)));

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

                OnOffType ooOnOff = OnOffType.from(bytes[217] != 5);

                // Update channels with read values
                updateChannel(ValloxMVBindingConstants.CHANNEL_ONOFF, ooOnOff);
                updateChannel(ValloxMVBindingConstants.CHANNEL_STATE, new DecimalType(bdState));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FAN_SPEED, new QuantityType<>(iFanspeed, Units.PERCENT));
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
                updateChannel(ValloxMVBindingConstants.CHANNEL_HUMIDITY, new QuantityType<>(iHumidity, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_CO2, new QuantityType<>(iCo2, Units.PARTS_PER_MILLION));
                updateChannel(ValloxMVBindingConstants.CHANNEL_CELLSTATE, new DecimalType(iCellstate));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_YEARS, new DecimalType(iUptimeYears));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_HOURS, new DecimalType(iUptimeHours));
                updateChannel(ValloxMVBindingConstants.CHANNEL_UPTIME_HOURS_CURRENT,
                        new DecimalType(iUptimeHoursCurrent));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FILTER_CHANGED_DATE, new DateTimeType(
                        ZonedDateTime.ofInstant(cFilterChangedDate.toInstant(), TimeZone.getDefault().toZoneId())));
                updateChannel(ValloxMVBindingConstants.CHANNEL_REMAINING_FILTER_DAYS,
                        new QuantityType<>(iRemainingTimeForFilter, Units.DAY));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTR_FAN_BALANCE_BASE,
                        new QuantityType<>(iExtrFanBalanceBase, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_SUPP_FAN_BALANCE_BASE,
                        new QuantityType<>(iSuppFanBalanceBase, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_HOME_SPEED_SETTING,
                        new QuantityType<>(iHomeSpeedSetting, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_AWAY_SPEED_SETTING,
                        new QuantityType<>(iAwaySpeedSetting, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_BOOST_SPEED_SETTING,
                        new QuantityType<>(iBoostSpeedSetting, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_HOME_AIR_TEMP_TARGET,
                        new QuantityType<>(bdHomeAirTempTarget, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_AWAY_AIR_TEMP_TARGET,
                        new QuantityType<>(bdAwayAirTempTarget, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_BOOST_AIR_TEMP_TARGET,
                        new QuantityType<>(bdBoostAirTempTarget, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_BOOST_TIME, new DecimalType(iBoostTime));
                updateChannel(ValloxMVBindingConstants.CHANNEL_BOOST_TIMER_ENABLED, ooBoostTimerEnabled);
                updateChannel(ValloxMVBindingConstants.CHANNEL_FIREPLACE_EXTR_FAN,
                        new QuantityType<>(iFireplaceExtrFan, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FIREPLACE_SUPP_FAN,
                        new QuantityType<>(iFireplaceSuppFan, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FIREPLACE_TIME, new DecimalType(iFireplaceTime));
                updateChannel(ValloxMVBindingConstants.CHANNEL_FIREPLACE_TIMER_ENABLED, ooFireplaceTimerEnabled);
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_AIR_TEMP_TARGET,
                        new QuantityType<>(bdExtraAirTempTarget, SIUnits.CELSIUS));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_EXTR_FAN,
                        new QuantityType<>(iExtraExtrFan, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_SUPP_FAN,
                        new QuantityType<>(iExtraSuppFan, Units.PERCENT));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_TIME, new DecimalType(iExtraTime));
                updateChannel(ValloxMVBindingConstants.CHANNEL_EXTRA_TIMER_ENABLED, ooExtraTimerEnabled);
                updateChannel(ValloxMVBindingConstants.CHANNEL_WEEKLY_TIMER_ENABLED, ooWeeklyTimerEnabled);

                voHandler.updateStatus(ThingStatus.ONLINE);
                logger.debug("Data updated successfully");

            } catch (IOException e) {
                connectionError(e);
            }
        }

        private void updateChannel(String strChannelName, State state) {
            voHandler.updateState(strChannelName, state);
        }

        private int getNumberBE(byte[] bytes, int pos) {
            return ((bytes[pos] & 0xff) << 8) | (bytes[pos + 1] & 0xff);
        }

        private int getNumberLE(byte[] bytes, int pos) {
            return (bytes[pos] & 0xff) | ((bytes[pos + 1] & 0xff) << 8);
        }

        @SuppressWarnings("null")
        private BigDecimal getTemperature(byte[] bytes, int pos) {
            // Fetch 2 byte number out of bytearray representing the temperature in centiKelvin
            BigDecimal bdTemperatureCentiKelvin = new BigDecimal(getNumberBE(bytes, pos));
            // Return number converted to degree celsius (= (centiKelvin - 27315) / 100 )
            return (new QuantityType<>(bdTemperatureCentiKelvin, MetricPrefix.CENTI(Units.KELVIN))
                    .toUnit(SIUnits.CELSIUS)).toBigDecimal();
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.debug("WebSocket Closed. Code: {}; Reason: {}", statusCode, reason);
            this.closeLatch.countDown();
        }

        public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
            return this.closeLatch.await(duration, unit);
        }
    }
}
