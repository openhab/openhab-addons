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
package org.openhab.binding.mycroft.internal.api.dto;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.mycroft.internal.api.MessageType;

import com.google.gson.annotations.SerializedName;

/**
 * This message is sent to the Mycroft audio module
 * to trigger a TTS action.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
public class MessageSpeak extends BaseMessage {

    public Data data = new Data();

    public Context context = new Context();

    public MessageSpeak() {
        this.type = MessageType.speak;
    }

    public MessageSpeak(String textToSay) {
        this();
        this.data = new Data();
        this.data.utterance = textToSay;
    }

    public static class Data {
        public String utterance = "";
        @SerializedName("expect_response")
        public String expectResponse = "";
    }

    public static class Context {
        @SerializedName("client_name")
        public String clientName = "";
        public List<String> source = new ArrayList<>();
        public String destination = "";
    }
}
