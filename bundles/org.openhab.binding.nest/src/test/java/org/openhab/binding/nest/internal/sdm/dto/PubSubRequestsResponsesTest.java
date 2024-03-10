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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.sdm.dto.SDMDataUtil.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubAcknowledgeRequest;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubCreateRequest;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubMessage;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubPullRequest;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubPullResponse;
import org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses.PubSubReceivedMessage;

/**
 * Tests (de)serialization of {@link
 * org.openhab.binding.nest.internal.sdm.dto.PubSubRequestsResponses} from/to JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PubSubRequestsResponsesTest {

    @Test
    public void deserializePullSubscriptionResponse() throws IOException {
        PubSubPullResponse response = fromJson("pull-subscription-response.json", PubSubPullResponse.class);
        assertThat(response, is(notNullValue()));

        List<PubSubReceivedMessage> receivedMessages = response.receivedMessages;
        assertThat(receivedMessages, is(notNullValue()));
        assertThat(receivedMessages, hasSize(3));

        PubSubReceivedMessage receivedMessage = receivedMessages.get(0);
        assertThat(receivedMessage, is(notNullValue()));
        assertThat(receivedMessage.ackId, is("AID1"));
        PubSubMessage message = receivedMessage.message;
        assertThat(message, is(notNullValue()));
        assertThat(message.data, is("ZGF0YTE="));
        assertThat(message.messageId, is("1000000000000001"));
        assertThat(message.publishTime, is(ZonedDateTime.parse("2021-01-01T01:00:00.000Z")));

        receivedMessage = receivedMessages.get(1);
        assertThat(receivedMessage, is(notNullValue()));
        assertThat(receivedMessage.ackId, is("AID2"));
        message = receivedMessage.message;
        assertThat(message, is(notNullValue()));
        assertThat(message.data, is("ZGF0YTI="));
        assertThat(message.messageId, is("2000000000000002"));
        assertThat(message.publishTime, is(ZonedDateTime.parse("2021-02-02T02:00:00.000Z")));

        receivedMessage = receivedMessages.get(2);
        assertThat(receivedMessage, is(notNullValue()));
        assertThat(receivedMessage.ackId, is("AID3"));
        message = receivedMessage.message;
        assertThat(message, is(notNullValue()));
        assertThat(message.data, is("ZGF0YTM="));
        assertThat(message.messageId, is("3000000000000003"));
        assertThat(message.publishTime, is(ZonedDateTime.parse("2021-03-03T03:00:00.000Z")));
    }

    @Test
    public void serializeAcknowledgeSubscriptionRequest() throws IOException {
        String json = toJson(new PubSubAcknowledgeRequest(List.of("AID1", "AID2", "AID3")));
        assertThat(json, is(fromFile("acknowledge-subscription-request.json")));
    }

    @Test
    public void serializeCreateSubscriptionRequest() throws IOException {
        String json = toJson(new PubSubCreateRequest("projects/sdm-prod/topics/enterprise-project-id", true));
        assertThat(json, is(fromFile("create-subscription-request.json")));
    }

    @Test
    public void serializePullSubscriptionRequest() throws IOException {
        String json = toJson(new PubSubPullRequest(123));
        assertThat(json, is(fromFile("pull-subscription-request.json")));
    }
}
