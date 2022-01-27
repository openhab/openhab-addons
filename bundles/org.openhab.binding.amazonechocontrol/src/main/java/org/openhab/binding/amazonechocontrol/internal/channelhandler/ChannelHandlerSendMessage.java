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
package org.openhab.binding.amazonechocontrol.internal.channelhandler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

import com.google.gson.Gson;

/**
 * The {@link ChannelHandlerSendMessage} is responsible for the announcement
 * channel
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class ChannelHandlerSendMessage extends ChannelHandler {

    private static final String CHANNEL_NAME = "sendMessage";
    private @Nullable AccountJson accountJson;
    private int lastMessageId = 1000;

    public ChannelHandlerSendMessage(IAmazonThingHandler thingHandler, Gson gson) {
        super(thingHandler, gson);
    }

    @Override
    public boolean tryHandleCommand(Device device, Connection connection, String channelId, Command command)
            throws IOException, URISyntaxException, InterruptedException {
        if (channelId.equals(CHANNEL_NAME)) {
            if (command instanceof StringType) {
                String commandValue = ((StringType) command).toFullString();
                String baseUrl = "https://alexa-comms-mobile-service." + connection.getAmazonSite();

                AccountJson currentAccountJson = this.accountJson;
                if (currentAccountJson == null) {
                    String accountResult = connection.makeRequestAndReturnString(baseUrl + "/accounts");
                    AccountJson @Nullable [] accountsJson = gson.fromJson(accountResult, AccountJson[].class);
                    if (accountsJson == null) {
                        return false;
                    }
                    for (AccountJson accountJson : accountsJson) {
                        Boolean signedInUser = accountJson.signedInUser;
                        if (signedInUser != null && signedInUser) {
                            this.accountJson = accountJson;
                            currentAccountJson = accountJson;
                            break;
                        }
                    }
                }
                if (currentAccountJson == null) {
                    return false;
                }
                String commsId = currentAccountJson.commsId;
                if (commsId == null) {
                    return false;
                }
                String senderCommsId = commsId;
                String receiverCommsId = commsId;

                SendConversationJson conversationJson = new SendConversationJson();
                conversationJson.conversationId = "amzn1.comms.messaging.id.conversationV2~31e6fe8f-8b0c-4e84-a1e4-80030a09009b";
                conversationJson.clientMessageId = java.util.UUID.randomUUID().toString();
                conversationJson.messageId = lastMessageId++;
                conversationJson.sender = senderCommsId;
                conversationJson.time = LocalDateTime.now().toString();
                conversationJson.payload.text = commandValue;

                String sendConversationBody = this.gson.toJson(new SendConversationJson[] { conversationJson });
                String sendUrl = baseUrl + "/users/" + senderCommsId + "/conversations/" + receiverCommsId
                        + "/messages";
                connection.makeRequestAndReturnString("POST", sendUrl, sendConversationBody, true, null);
            }
            refreshChannel();
        }
        return false;
    }

    private void refreshChannel() {
        thingHandler.updateChannelState(CHANNEL_NAME, new StringType(""));
    }

    @SuppressWarnings("unused")
    private static class AccountJson {
        public @Nullable String commsId;
        public @Nullable String directedId;
        public @Nullable String phoneCountryCode;
        public @Nullable String phoneNumber;
        public @Nullable String firstName;
        public @Nullable String lastName;
        public @Nullable String phoneticFirstName;
        public @Nullable String phoneticLastName;
        public @Nullable String commsProvisionStatus;
        public @Nullable Boolean isChild;
        public @Nullable Boolean signedInUser;
        public @Nullable Boolean commsProvisioned;
        public @Nullable Boolean speakerProvisioned;
    }

    @SuppressWarnings("unused")
    private static class SendConversationJson {
        public @Nullable String conversationId;
        public @Nullable String clientMessageId;
        public @Nullable Integer messageId;
        public @Nullable String time;
        public @Nullable String sender;
        public String type = "message/text";
        public Payload payload = new Payload();
        public Integer status = 1;

        private static class Payload {
            public @Nullable String text;
        }
    }
}
