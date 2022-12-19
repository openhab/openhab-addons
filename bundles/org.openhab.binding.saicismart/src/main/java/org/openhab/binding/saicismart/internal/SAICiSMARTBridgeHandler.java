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
package org.openhab.binding.saicismart.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.bn.coders.IASN1PreparedElement;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import net.heberling.ismart.asn1.v1_1.MP_DispatcherBody;
import net.heberling.ismart.asn1.v1_1.MP_DispatcherHeader;
import net.heberling.ismart.asn1.v1_1.Message;
import net.heberling.ismart.asn1.v1_1.MessageCoder;
import net.heberling.ismart.asn1.v1_1.MessageCounter;
import net.heberling.ismart.asn1.v1_1.entity.AlarmSwitch;
import net.heberling.ismart.asn1.v1_1.entity.AlarmSwitchReq;
import net.heberling.ismart.asn1.v1_1.entity.MP_AlarmSettingType;
import net.heberling.ismart.asn1.v1_1.entity.MP_UserLoggingInReq;
import net.heberling.ismart.asn1.v1_1.entity.MP_UserLoggingInResp;
import net.heberling.ismart.asn1.v1_1.entity.MessageListReq;
import net.heberling.ismart.asn1.v1_1.entity.MessageListResp;
import net.heberling.ismart.asn1.v1_1.entity.StartEndNumber;
import net.heberling.ismart.asn1.v1_1.entity.VinInfo;

/**
 * The {@link SAICiSMARTBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class SAICiSMARTBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(SAICiSMARTBridgeHandler.class);

    private @Nullable SAICiSMARTBridgeConfiguration config;

    private @Nullable String uid;

    private @Nullable String token;

    private @Nullable Collection<VinInfo> vinList;
    private HttpClientFactory httpClientFactory;
    private @Nullable Future<?> pollingJob;

    public SAICiSMARTBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands available
    }

    @Override
    public void initialize() {
        config = getConfigAs(SAICiSMARTBridgeConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            if (uid == null || token == null) {
                Message<MP_UserLoggingInReq> loginRequestMessage = createMessage(
                        "0000000000000000000000000000000000000000000000000#".substring(config.username.length())
                                + config.username,
                        null, "501", 513, 1, new MP_UserLoggingInReq());

                loginRequestMessage.getApplicationData().setPassword(config.password);

                String loginRequest = new MessageCoder<>(MP_UserLoggingInReq.class).encodeRequest(loginRequestMessage);

                try {
                    String loginResponse = sendRequest(loginRequest, "https://tap-eu.soimt.com/TAP.Web/ota.mp");

                    Message<MP_UserLoggingInResp> loginResponseMessage = new MessageCoder<>(MP_UserLoggingInResp.class)
                            .decodeResponse(loginResponse);

                    logger.info("Got message: {}",
                            new GsonBuilder().setPrettyPrinting().create().toJson(loginResponseMessage));

                    uid = loginResponseMessage.getBody().getUid();
                    token = loginResponseMessage.getApplicationData().getToken();
                    vinList = loginResponseMessage.getApplicationData().getVinList();

                    // register for all alarm types
                    AlarmSwitchReq alarmSwitchReq = new AlarmSwitchReq();
                    alarmSwitchReq.setAlarmSwitchList(Stream.of(MP_AlarmSettingType.EnumType.values())
                            .map(v -> createAlarmSwitch(v, true)).collect(Collectors.toList()));
                    alarmSwitchReq.setPin(hashMD5("123456"));

                    Message<AlarmSwitchReq> alarmSwitchMessage = createMessage(uid, token, "521", 513, 1,
                            alarmSwitchReq);
                    String alarmSwitchRequest = new MessageCoder<>(AlarmSwitchReq.class)
                            .encodeRequest(alarmSwitchMessage);
                    String alarmSwitchResponse = sendRequest(alarmSwitchRequest,
                            "https://tap-eu.soimt.com/TAP.Web/ota.mp");
                    Message<IASN1PreparedElement> alarmSwitchResponseMessage = new MessageCoder<>(
                            IASN1PreparedElement.class).decodeResponse(alarmSwitchResponse);

                    logger.info("Got message: {}",
                            new GsonBuilder().setPrettyPrinting().create().toJson(alarmSwitchResponseMessage));

                    if (alarmSwitchResponseMessage.getBody().getErrorMessage() != null) {
                        throw new TimeoutException(new String(alarmSwitchResponseMessage.getBody().getErrorMessage(),
                                StandardCharsets.UTF_8));
                    }

                    updateStatus(ThingStatus.ONLINE);
                } catch (TimeoutException | URISyntaxException | ExecutionException | InterruptedException
                        | NoSuchAlgorithmException e) {
                    updateStatus(ThingStatus.OFFLINE);
                    logger.error("Could not login to SAIC iSMART account", e);
                }
            } else {
                Message<MessageListReq> messageListRequestMessage = createMessage(uid, token, "531", 513, 1,
                        new MessageListReq());

                messageListRequestMessage.getHeader().setProtocolVersion(18);

                // We currently assume that the newest message is the first.
                // TODO: get all messages
                // TODO: delete old messages
                // TODO: handle case when no messages are there
                // TODO: create a message channel, that delivers messages to openhab, that don't belong to a specific
                // car
                // TODO: automatically subscribe for engine start messages
                messageListRequestMessage.getApplicationData().setStartEndNumber(new StartEndNumber());
                messageListRequestMessage.getApplicationData().getStartEndNumber().setStartNumber(1L);
                messageListRequestMessage.getApplicationData().getStartEndNumber().setEndNumber(5L);
                messageListRequestMessage.getApplicationData().setMessageGroup("ALARM");

                String messageListRequest = new MessageCoder<>(MessageListReq.class)
                        .encodeRequest(messageListRequestMessage);

                try {
                    String messageListResponse = sendRequest(messageListRequest,
                            "https://tap-eu.soimt.com/TAP.Web/ota.mp");

                    Message<MessageListResp> messageListResponseMessage = new MessageCoder<>(MessageListResp.class)
                            .decodeResponse(messageListResponse);

                    logger.info("Got message: {}",
                            new GsonBuilder().setPrettyPrinting().create().toJson(messageListResponseMessage));

                    if (messageListResponseMessage.getApplicationData() != null) {
                        for (net.heberling.ismart.asn1.v1_1.entity.Message message : messageListResponseMessage
                                .getApplicationData().getMessages()) {
                            if (message.isVinPresent()) {
                                String vin = message.getVin();
                                getThing().getThings().stream().filter(t -> t.getUID().getId().equals(vin))
                                        .map(Thing::getHandler).filter(Objects::nonNull)
                                        .filter(SAICiSMARTHandler.class::isInstance).map(SAICiSMARTHandler.class::cast)
                                        .forEach(t -> t.handleMessage(message));
                            }
                        }
                    } else {
                        logger.warn("No application data found!");
                    }

                    updateStatus(ThingStatus.ONLINE);

                } catch (TimeoutException | URISyntaxException | ExecutionException | InterruptedException e) {
                    updateStatus(ThingStatus.OFFLINE);
                    logger.error("Could not get messages from SAIC iSMART account", e);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private static <X extends IASN1PreparedElement> Message<X> createMessage(@Nullable String uid,
            @Nullable String token, String applicationID, int applicationDataProtocolVersion, int messageID,
            X applicationData) {
        Message<X> loginRequestMessage = new Message<>(new MP_DispatcherHeader(), new MP_DispatcherBody(),
                applicationData);

        MessageCounter messageCounter = new MessageCounter();
        messageCounter.setDownlinkCounter(0);
        messageCounter.setUplinkCounter(1);
        loginRequestMessage.getBody().setMessageCounter(messageCounter);

        loginRequestMessage.getBody().setMessageID(messageID);
        loginRequestMessage.getBody().setIccID("12345678901234567890");
        loginRequestMessage.getBody().setSimInfo("1234567890987654321");
        loginRequestMessage.getBody().setEventCreationTime(Instant.now().getEpochSecond());
        loginRequestMessage.getBody().setApplicationID(applicationID);
        loginRequestMessage.getBody().setApplicationDataProtocolVersion(applicationDataProtocolVersion);
        loginRequestMessage.getBody().setTestFlag(2);
        loginRequestMessage.getBody().setUid(uid);
        loginRequestMessage.getBody().setToken(token);

        return loginRequestMessage;
    }

    private static AlarmSwitch createAlarmSwitch(MP_AlarmSettingType.EnumType type, boolean enabled) {
        AlarmSwitch alarmSwitch = new AlarmSwitch();
        MP_AlarmSettingType alarmSettingType = new MP_AlarmSettingType();
        alarmSettingType.setValue(type);
        alarmSettingType.setIntegerForm(type.ordinal());
        alarmSwitch.setAlarmSettingType(alarmSettingType);
        alarmSwitch.setAlarmSwitch(enabled);
        alarmSwitch.setFunctionSwitch(enabled);
        return alarmSwitch;
    }

    public String hashMD5(String password) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(VehicleDiscovery.class);
    }

    @Nullable
    public String getUid() {
        return uid;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    public Collection<VinInfo> getVinList() {
        return Optional.ofNullable(vinList).orElse(Collections.emptyList());
    }

    public String sendRequest(String request, String endpoint)
            throws URISyntaxException, ExecutionException, InterruptedException, TimeoutException {
        HttpClient httpClient = httpClientFactory.getCommonHttpClient();

        return httpClient.POST(new URI(endpoint)).content(new StringContentProvider(request), "text/html").send()
                .getContentAsString();
    }

    public void relogin() {
        uid = null;
        token = null;
    }

    @Override
    public void dispose() {
        Future<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }
}
