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
package org.openhab.binding.nest.internal.sdm.dto;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * The {@link PubSubRequestsResponses} provides classes used for mapping Pub/Sub REST API requests and responses.
 * Only the subset of requests/responses and fields that are used by the binding are implemented.
 *
 * @author Wouter Born - Initial contribution
 *
 * @see <a href="https://cloud.google.com/pubsub/docs/reference/rest">
 *      https://cloud.google.com/pubsub/docs/reference/rest</a>
 */
public class PubSubRequestsResponses {

    // Method: projects.subscriptions.acknowledge

    /**
     * Acknowledges the messages associated with the ackIds in the AcknowledgeRequest. The Pub/Sub system can remove the
     * relevant messages from the subscription.
     *
     * Acknowledging a message whose ack deadline has expired may succeed, but such a message may be redelivered later.
     * Acknowledging a message more than once will not result in an error.
     */
    public static class PubSubAcknowledgeRequest {

        public List<String> ackIds;

        public PubSubAcknowledgeRequest(List<String> ackIds) {
            this.ackIds = ackIds;
        }
    }

    // Method: projects.subscriptions.create

    /**
     * Creates a subscription to a given topic. See the resource name rules. If the subscription already exists, returns
     * ALREADY_EXISTS. If the corresponding topic doesn't exist, returns NOT_FOUND.
     *
     * If the name is not provided in the request, the server will assign a random name for this subscription on the
     * same project as the topic, conforming to the resource name format. The generated name is populated in the
     * returned Subscription object. Note that for REST API requests, you must specify a name in the request.
     */
    public static class PubSubCreateRequest {

        public String topic;
        public boolean enableMessageOrdering;

        /**
         * @param topic The name of the topic from which this subscription is receiving messages. Format is
         *            <code>projects/{project}/topics/{topic}</code>.
         * @param enableMessageOrdering If true, messages published with the same orderingKey in the message will be
         *            delivered to the subscribers in the order in which they are received by the Pub/Sub system.
         *            Otherwise, they may be delivered in any order.
         */
        public PubSubCreateRequest(String topic, boolean enableMessageOrdering) {
            this.topic = topic;
            this.enableMessageOrdering = enableMessageOrdering;
        }
    }

    // Method: projects.subscriptions.pull

    /**
     * Pulls messages from the server. The server may return UNAVAILABLE if there are too many concurrent pull requests
     * pending for the given subscription.
     *
     * A {@link PubSubPullResponse} is returned when successful.
     */
    public static class PubSubPullRequest {

        public int maxMessages;

        /**
         * @param maxMessages The maximum number of messages to return for this request. Must be a positive integer. The
         *            Pub/Sub system may return fewer than the number specified.
         */
        public PubSubPullRequest(int maxMessages) {
            this.maxMessages = maxMessages;
        }
    }

    /**
     * A message that is published by publishers and consumed by subscribers.
     */
    public static class PubSubMessage {
        /**
         * The message data field. A base64-encoded string.
         */
        public String data;

        /**
         * ID of this message, assigned by the server when the message is published. Guaranteed to be unique within the
         * topic. This value may be read by a subscriber that receives a PubsubMessage via a
         * <code>subscriptions.pull</code> call or a push delivery. It must not be populated by the publisher in a
         * topics.publish call.
         */
        public String messageId;

        /**
         * The time at which the message was published, populated by the server when it receives the topics.publish
         * call. It must not be populated by the publisher in a topics publish call.
         *
         * A timestamp in RFC3339 UTC "Zulu" format, with nanosecond resolution and up to nine fractional digits.
         * Examples: "2014-10-02T15:01:23Z" and "2014-10-02T15:01:23.045123456Z".
         */
        public ZonedDateTime publishTime;
    }

    /**
     * A message and its corresponding acknowledgment ID.
     */
    public static class PubSubReceivedMessage {
        /**
         * This ID can be used to acknowledge the received message.
         */
        public String ackId;

        /**
         * The message.
         */
        public PubSubMessage message;
    }

    /**
     * Response to a {@link PubSubPullRequest}.
     */
    public class PubSubPullResponse {
        /**
         * Received Pub/Sub messages. The list will be empty if there are no more messages available in the backlog. For
         * JSON, the response can be entirely empty. The Pub/Sub system may return fewer than the maxMessages requested
         * even if there are more messages available in the backlog.
         */
        public List<PubSubReceivedMessage> receivedMessages;
    }
}
