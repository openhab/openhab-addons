/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ntfy.internal.network;

import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;

/**
 * The {@link NtfyMessageHeaderBuilder} builds the headers of the request
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class NtfyMessageHeaderBuilder {

    private static final String X_SEQUENCE_ID = "X-Sequence-ID";
    private static final String X_ACTIONS = "X-Actions";
    private static final String X_FILENAME = "X-Filename";
    private static final String X_ATTACH = "X-Attach";
    private static final String X_ICON = "X-Icon";
    private static final String X_CLICK = "X-Click";
    private static final String X_TAGS = "X-Tags";
    private static final String X_PRIORITY = "X-Priority";
    private static final String X_DELAY = "X-Delay";

    private NtfyMessage ntfyMessage;
    private Request request;

    /**
     * Creates a new header builder for the given message and request.
     *
     * @param ntfyMessage the message containing header information
     * @param request the Jetty {@link Request} to which headers will be added
     */
    public NtfyMessageHeaderBuilder(NtfyMessage ntfyMessage, Request request) {
        this.ntfyMessage = ntfyMessage;
        this.request = request;
    }

    /**
     * Builds and applies all relevant ntfy HTTP headers to the wrapped request
     * (priority, click/action headers, attachment, icon, tags, delay and sequence id).
     */
    public void build() {
        request.header(X_PRIORITY, Integer.toString(ntfyMessage.getPriority()));

        if (ntfyMessage.hasClickAction()) {
            request.header(X_CLICK, ntfyMessage.getClickAction().toString());
        }

        String tags = String.join(",", ntfyMessage.getTags());
        if (!tags.isEmpty()) {
            request.header(X_TAGS, tags);
        }

        if (ntfyMessage.hasIcon()) {
            request.header(X_ICON, ntfyMessage.getIcon().toString());
        }

        if (ntfyMessage.hasAttachment()) {
            request.header(X_ATTACH, ntfyMessage.getAttachment().toString());
            final @Nullable String filename = ntfyMessage.getAttachmentFilename();
            if (filename != null && !filename.isBlank()) {
                request.header(X_FILENAME, filename);
            }
        }

        if (ntfyMessage.hasDelay()) {
            final String delay = ntfyMessage.getDelay();
            if (!delay.isBlank()) {
                request.header(X_DELAY, delay);
            }
        }

        String actions = String.join(";",
                ntfyMessage.getActions().stream().map(action -> action.getHeader()).collect(Collectors.toList()));
        if (!actions.isBlank()) {
            request.header(X_ACTIONS, actions);
        }

        if (ntfyMessage.hasSequenceId()) {
            String sequenceId = ntfyMessage.getSequenceId();
            if (!sequenceId.isBlank()) {
                request.header(X_SEQUENCE_ID, sequenceId);
            }
        }
    }
}
