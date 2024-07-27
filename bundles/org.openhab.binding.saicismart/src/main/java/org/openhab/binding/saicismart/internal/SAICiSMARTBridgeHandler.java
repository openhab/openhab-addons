/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.API_ENDPOINT_V11;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import net.heberling.ismart.asn1.v1_1.Message;
import net.heberling.ismart.asn1.v1_1.MessageCoder;
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
    private HttpClient httpClient;
    private @Nullable Future<?> pollingJob;

    public SAICiSMARTBridgeHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands available
    }

    @Override
    public void initialize() {
        config = getConfigAs(SAICiSMARTBridgeConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        // Validate configuration
        if (this.config.username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-type.config.saicismart.bridge.username.required");
            return;
        }
        if (this.config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-type.config.saicismart.bridge.password.required");
            return;
        }
        if (this.config.username.length() > 50) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-type.config.saicismart.bridge.username.toolong");
            return;
        }
        pollingJob = scheduler.scheduleWithFixedDelay(this::updateStatus, 1,
                SAICiSMARTBindingConstants.REFRESH_INTERVAL, TimeUnit.SECONDS);
    }

    private void updateStatus() {
        if (uid == null || token == null) {
            login();
        } else {
            registerForMessages();
        }
    }

    private void login() {
        MessageCoder<MP_UserLoggingInReq> mpUserLoggingInRequestMessageCoder = new MessageCoder<>(
                MP_UserLoggingInReq.class);

        MP_UserLoggingInReq mpUserLoggingInReq = new MP_UserLoggingInReq();
        mpUserLoggingInReq.setPassword(config.password);
        Message<MP_UserLoggingInReq> loginRequestMessage = mpUserLoggingInRequestMessageCoder.initializeMessage(
                StringUtils.padLeft("#" + config.username, 50, "0"), null, null, "501", 513, 1, mpUserLoggingInReq);

        String loginRequest = mpUserLoggingInRequestMessageCoder.encodeRequest(loginRequestMessage);

        try {
            String loginResponse = sendRequest(loginRequest, API_ENDPOINT_V11);

            Message<MP_UserLoggingInResp> loginResponseMessage = new MessageCoder<>(MP_UserLoggingInResp.class)
                    .decodeResponse(loginResponse);

            logger.trace("Got message: {}",
                    new GsonBuilder().setPrettyPrinting().create().toJson(loginResponseMessage));

            uid = loginResponseMessage.getBody().getUid();
            token = loginResponseMessage.getApplicationData().getToken();
            vinList = loginResponseMessage.getApplicationData().getVinList();

            // register for all known alarm types (not all might be actually delivered)
            for (MP_AlarmSettingType.EnumType type : MP_AlarmSettingType.EnumType.values()) {
                registerAlarmMessage(loginResponseMessage.getBody().getUid(),
                        loginResponseMessage.getApplicationData().getToken(), type);
            }

            updateStatus(ThingStatus.ONLINE);
        } catch (TimeoutException | URISyntaxException | ExecutionException | InterruptedException
                | NoSuchAlgorithmException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void registerForMessages() {
        MessageCoder<MessageListReq> messageListReqMessageCoder = new MessageCoder<>(MessageListReq.class);
        Message<MessageListReq> messageListRequestMessage = messageListReqMessageCoder.initializeMessage(uid, token,
                null, "531", 513, 1, new MessageListReq());

        messageListRequestMessage.getHeader().setProtocolVersion(18);

        // We currently assume that the newest message is the first.
        messageListRequestMessage.getApplicationData().setStartEndNumber(new StartEndNumber());
        messageListRequestMessage.getApplicationData().getStartEndNumber().setStartNumber(1L);
        messageListRequestMessage.getApplicationData().getStartEndNumber().setEndNumber(5L);
        messageListRequestMessage.getApplicationData().setMessageGroup("ALARM");

        String messageListRequest = messageListReqMessageCoder.encodeRequest(messageListRequestMessage);

        try {
            String messageListResponse = sendRequest(messageListRequest, API_ENDPOINT_V11);

            Message<MessageListResp> messageListResponseMessage = new MessageCoder<>(MessageListResp.class)
                    .decodeResponse(messageListResponse);

            logger.trace("Got message: {}",
                    new GsonBuilder().setPrettyPrinting().create().toJson(messageListResponseMessage));

            if (messageListResponseMessage.getApplicationData() != null
                    && messageListResponseMessage.getApplicationData().getMessages() != null) {
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
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (TimeoutException | URISyntaxException | ExecutionException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void registerAlarmMessage(String uid, String token, MP_AlarmSettingType.EnumType type)
            throws NoSuchAlgorithmException, IOException, URISyntaxException, ExecutionException, InterruptedException,
            TimeoutException {
        MessageCoder<AlarmSwitchReq> alarmSwitchReqMessageCoder = new MessageCoder<>(AlarmSwitchReq.class);

        AlarmSwitchReq alarmSwitchReq = new AlarmSwitchReq();
        alarmSwitchReq
                .setAlarmSwitchList(Stream.of(type).map(v -> createAlarmSwitch(v, true)).collect(Collectors.toList()));
        alarmSwitchReq.setPin(hashMD5("123456"));

        Message<AlarmSwitchReq> alarmSwitchMessage = alarmSwitchReqMessageCoder.initializeMessage(uid, token, null,
                "521", 513, 1, alarmSwitchReq);
        String alarmSwitchRequest = alarmSwitchReqMessageCoder.encodeRequest(alarmSwitchMessage);
        String alarmSwitchResponse = sendRequest(alarmSwitchRequest, API_ENDPOINT_V11);
        final MessageCoder<IASN1PreparedElement> alarmSwitchResMessageCoder = new MessageCoder<>(
                IASN1PreparedElement.class);
        Message<IASN1PreparedElement> alarmSwitchResponseMessage = alarmSwitchResMessageCoder
                .decodeResponse(alarmSwitchResponse);

        logger.trace("Got message: {}",
                new GsonBuilder().setPrettyPrinting().create().toJson(alarmSwitchResponseMessage));

        if (alarmSwitchResponseMessage.getBody().getErrorMessage() != null) {
            logger.debug("Could not register for {} messages: {}", type,
                    new String(alarmSwitchResponseMessage.getBody().getErrorMessage(), StandardCharsets.UTF_8));
        } else {
            logger.debug("Registered for {} messages", type);
        }
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
        Collection<VinInfo> vinList = this.vinList;
        return vinList != null ? vinList : Collections.emptyList();
    }

    public String sendRequest(String request, String endpoint)
            throws URISyntaxException, ExecutionException, InterruptedException, TimeoutException {
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
