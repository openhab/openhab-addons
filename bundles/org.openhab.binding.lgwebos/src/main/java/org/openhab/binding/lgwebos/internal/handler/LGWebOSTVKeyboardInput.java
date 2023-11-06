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
/*
 * WebOSTVKeyboardInput
 * Connect SDK
 *
 * Copyright (c) 2014 LG Electronics.
 * Created by Hyun Kook Khang on 19 Jan 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhab.binding.lgwebos.internal.handler;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.lgwebos.internal.handler.command.ServiceCommand;
import org.openhab.binding.lgwebos.internal.handler.command.ServiceSubscription;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.openhab.binding.lgwebos.internal.handler.core.TextInputStatusInfo;

import com.google.gson.JsonObject;

/**
 * {@link LGWebOSTVKeyboardInput} handles WebOSTV keyboard api.
 *
 * @author Hyun Kook Khang - Connect SDK initial contribution
 * @author Sebastian Prehn - Adoption for openHAB
 */
public class LGWebOSTVKeyboardInput {

    private LGWebOSTVSocket service;
    private boolean waiting;
    private final List<String> toSend;

    private static final String KEYBOARD_INPUT = "ssap://com.webos.service.ime/registerRemoteKeyboard";
    private static final String ENTER = "ENTER";
    private static final String DELETE = "DELETE";

    public LGWebOSTVKeyboardInput(LGWebOSTVSocket service) {
        this.service = service;
        waiting = false;
        toSend = new ArrayList<>();
    }

    public void sendText(String input) {
        toSend.add(input);
        if (!waiting) { // TODO: use a latch,and send in any case
            sendData();
        }
    }

    public void sendEnter() {
        sendText(ENTER);
    }

    public void sendDel() {
        if (toSend.isEmpty()) {
            toSend.add(DELETE);
            if (!waiting) {
                sendData();
            }
        } else {
            toSend.remove(toSend.size() - 1);
        }
    }

    private void sendData() {
        waiting = true;

        String uri;
        String typeTest = toSend.get(0);

        JsonObject payload = new JsonObject();

        if (typeTest.equals(ENTER)) {
            toSend.remove(0);
            uri = "ssap://com.webos.service.ime/sendEnterKey";
        } else if (typeTest.equals(DELETE)) {
            uri = "ssap://com.webos.service.ime/deleteCharacters";

            int count = 0;
            while (!toSend.isEmpty() && toSend.get(0).equals(DELETE)) {
                toSend.remove(0);
                count++;
            }

            payload.addProperty("count", count);
        } else {
            uri = "ssap://com.webos.service.ime/insertText";
            StringBuilder sb = new StringBuilder();

            while (!toSend.isEmpty() && !(toSend.get(0).equals(DELETE) || toSend.get(0).equals(ENTER))) {
                String text = toSend.get(0);
                sb.append(text);
                toSend.remove(0);
            }

            payload.addProperty("text", sb.toString());
            payload.addProperty("replace", 0);
        }

        ResponseListener<JsonObject> responseListener = new ResponseListener<JsonObject>() {

            @Override
            public void onSuccess(JsonObject response) {
                waiting = false;
                if (!toSend.isEmpty()) {
                    sendData();
                }
            }

            @Override
            public void onError(String error) {
                waiting = false;
                if (!toSend.isEmpty()) {
                    sendData();
                }
            }
        };

        ServiceCommand<JsonObject> request = new ServiceCommand<>(uri, payload, x -> x, responseListener);
        service.sendCommand(request);
    }

    public ServiceSubscription<TextInputStatusInfo> connect(final ResponseListener<TextInputStatusInfo> listener) {
        ServiceSubscription<TextInputStatusInfo> subscription = new ServiceSubscription<>(KEYBOARD_INPUT, null,
                rawData -> parseRawKeyboardData(rawData), listener);
        service.sendCommand(subscription);

        return subscription;
    }

    private TextInputStatusInfo parseRawKeyboardData(JsonObject rawData) {
        boolean focused = false;
        String contentType = null;
        boolean predictionEnabled = false;
        boolean correctionEnabled = false;
        boolean autoCapitalization = false;
        boolean hiddenText = false;
        boolean focusChanged = false;

        TextInputStatusInfo keyboard = new TextInputStatusInfo();
        keyboard.setRawData(rawData);

        if (rawData.has("currentWidget")) {
            JsonObject currentWidget = (JsonObject) rawData.get("currentWidget");
            focused = currentWidget.get("focus").getAsBoolean();

            if (currentWidget.has("contentType")) {
                contentType = currentWidget.get("contentType").getAsString();
            }
            if (currentWidget.has("predictionEnabled")) {
                predictionEnabled = currentWidget.get("predictionEnabled").getAsBoolean();
            }
            if (currentWidget.has("correctionEnabled")) {
                correctionEnabled = currentWidget.get("correctionEnabled").getAsBoolean();
            }
            if (currentWidget.has("autoCapitalization")) {
                autoCapitalization = currentWidget.get("autoCapitalization").getAsBoolean();
            }
            if (currentWidget.has("hiddenText")) {
                hiddenText = currentWidget.get("hiddenText").getAsBoolean();
            }
        }
        if (rawData.has("focusChanged")) {
            focusChanged = rawData.get("focusChanged").getAsBoolean();
        }

        keyboard.setFocused(focused);
        keyboard.setContentType(contentType);
        keyboard.setPredictionEnabled(predictionEnabled);
        keyboard.setCorrectionEnabled(correctionEnabled);
        keyboard.setAutoCapitalization(autoCapitalization);
        keyboard.setHiddenText(hiddenText);
        keyboard.setFocusChanged(focusChanged);

        return keyboard;
    }
}
