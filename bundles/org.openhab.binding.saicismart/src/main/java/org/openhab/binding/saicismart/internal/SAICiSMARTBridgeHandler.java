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
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.saicismart.internal.asn1.v1_1.MP_DispatcherBody;
import org.openhab.binding.saicismart.internal.asn1.v1_1.MP_DispatcherHeader;
import org.openhab.binding.saicismart.internal.asn1.v1_1.MP_UserLoggingInReq;
import org.openhab.binding.saicismart.internal.asn1.v1_1.MP_UserLoggingInResp;
import org.openhab.binding.saicismart.internal.asn1.v1_1.Message;
import org.openhab.binding.saicismart.internal.asn1.v1_1.MessageCoder;
import org.openhab.binding.saicismart.internal.asn1.v1_1.MessageCounter;
import org.openhab.binding.saicismart.internal.asn1.v1_1.VinInfo;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

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

        scheduler.execute(() -> {
            Message<MP_UserLoggingInReq> loginRequestMessage = new Message<>(new MP_DispatcherHeader(),
                    new MP_DispatcherBody(), new MP_UserLoggingInReq());

            MessageCounter messageCounter = new MessageCounter();
            messageCounter.setDownlinkCounter(0);
            messageCounter.setUplinkCounter(1);
            loginRequestMessage.getBody().setMessageCounter(messageCounter);

            loginRequestMessage.getBody().setMessageID(1);
            loginRequestMessage.getBody().setIccID("12345678901234567890");
            loginRequestMessage.getBody().setSimInfo("1234567890987654321");
            loginRequestMessage.getBody().setEventCreationTime(Instant.now().getEpochSecond());
            loginRequestMessage.getBody().setApplicationID("501");
            loginRequestMessage.getBody().setApplicationDataProtocolVersion(513);
            loginRequestMessage.getBody().setTestFlag(2);

            loginRequestMessage.getBody()
                    .setUid("0000000000000000000000000000000000000000000000000#".substring(config.username.length())
                            + config.username);

            loginRequestMessage.getApplicationData().setPassword(config.password);

            String loginRequest = new MessageCoder<>(MP_UserLoggingInReq.class).encodeRequest(loginRequestMessage);

            try {
                String loginResponse = sendRequest(loginRequest, "https://tap-eu.soimt.com/TAP.Web/ota.mp");

                Message<MP_UserLoggingInResp> loginResponseMessage = new MessageCoder<>(MP_UserLoggingInResp.class)
                        .decodeResponse(loginResponse);

                logger.info("Got message: {}", new GsonBuilder().setPrettyPrinting().create()
                        .toJson(loginResponseMessage.getApplicationData()));

                uid = loginResponseMessage.getBody().getUid();
                token = loginResponseMessage.getApplicationData().getToken();
                vinList = loginResponseMessage.getApplicationData().getVinList();
                updateStatus(ThingStatus.ONLINE);

            } catch (TimeoutException | URISyntaxException | ExecutionException | InterruptedException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.error("Could not login to SAIC iSMART account", e);
            }
        });
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
        initialize();
    }
}
