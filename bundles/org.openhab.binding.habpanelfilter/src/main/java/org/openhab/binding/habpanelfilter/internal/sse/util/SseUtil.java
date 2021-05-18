/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.habpanelfilter.internal.sse.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.habpanelfilter.internal.sse.dto.EventDTO;
import org.openhab.core.events.Event;

/**
 * @author Unspecified
 */
@NonNullByDefault
public class SseUtil {
    static final String TOPIC_VALIDATE_PATTERN = "(\\w*\\*?\\/?,?:?-?\\s*)*";

    public SseUtil() {
    }

    public static EventDTO buildDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.topic = event.getTopic();
        dto.type = event.getType();
        dto.payload = event.getPayload();
        return dto;
    }

    public static OutboundSseEvent buildEvent(OutboundSseEvent.Builder eventBuilder, EventDTO event) {
        OutboundSseEvent sseEvent = eventBuilder.name("message").mediaType(MediaType.APPLICATION_JSON_TYPE).data(event)
                .build();
        return sseEvent;
    }

    public static boolean isValidTopicFilter(@Nullable String topicFilter) {
        return topicFilter == null || topicFilter.isEmpty() || topicFilter.matches("(\\w*\\*?\\/?,?:?-?\\s*)*");
    }

    public static List<String> convertToRegex(@Nullable String topicFilter) {
        List<String> filters = new ArrayList();
        if (topicFilter != null && !topicFilter.isEmpty()) {
            StringTokenizer tokenizer = new StringTokenizer(topicFilter, ",");

            while (tokenizer.hasMoreElements()) {
                String regex = tokenizer.nextToken().trim().replace("*", ".*") + ".*";
                filters.add(regex);
            }
        } else {
            filters.add(".*");
        }

        return filters;
    }
}
