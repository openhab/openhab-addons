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
package org.openhab.binding.mycroft.internal.api.dto;

import org.openhab.binding.mycroft.internal.api.MessageType;

import com.google.gson.annotations.SerializedName;

/**
 * This message informs the bus clients that Mycroft
 * finished listening to the mic.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
public class MessageRecognizerLoopRecordEnd extends BaseMessage {

    public Context context = new Context();

    public MessageRecognizerLoopRecordEnd() {
        this.type = MessageType.recognizer_loop__record_end;
    }

    public static class Context {
        @SerializedName("client_name")
        public String clientName = "";
        public String source = "";
        public String destination = "";
    }
}
