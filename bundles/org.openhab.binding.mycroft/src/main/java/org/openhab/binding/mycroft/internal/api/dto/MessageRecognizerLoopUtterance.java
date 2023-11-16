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

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.mycroft.internal.api.MessageType;

import com.google.gson.annotations.SerializedName;

/**
 * This message is sent to the skills
 * module to trigger an intent from a text.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
public class MessageRecognizerLoopUtterance extends BaseMessage {

    public Data data = new Data();

    public Context context = new Context();

    public MessageRecognizerLoopUtterance() {
        this.type = MessageType.recognizer_loop__utterance;
    }

    public MessageRecognizerLoopUtterance(String utterance) {
        this();
        this.data.utterances.add(utterance);
        this.context.clientName = "java_api";
        this.context.source = "audio";
        this.context.destination.add("skills");
    }

    public static class Data {
        public List<String> utterances = new ArrayList<>();
    }

    public static class Context {
        @SerializedName("client_name")
        public String clientName = "";
        public String source = "";
        public List<String> destination = new ArrayList<>();
    }
}
