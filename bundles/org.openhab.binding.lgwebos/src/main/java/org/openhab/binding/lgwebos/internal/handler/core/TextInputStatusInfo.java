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
/* This file is based on:
 *
 * TextInputStatusInfo
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

package org.openhab.binding.lgwebos.internal.handler.core;

import com.google.gson.JsonObject;

/**
 * Normalized reference object for information about a text input event.
 *
 * @author Hyun Kook Khang - Connect SDK initial contribution
 * @author Sebastian Prehn - Adoption for openHAB
 */
public class TextInputStatusInfo {
    // @cond INTERNAL
    public enum TextInputType {
        DEFAULT,
        URL,
        NUMBER,
        PHONE_NUMBER,
        EMAIL
    }

    boolean focused = false;
    String contentType = null;
    boolean predictionEnabled = false;
    boolean correctionEnabled = false;
    boolean autoCapitalization = false;
    boolean hiddenText = false;
    boolean focusChanged = false;

    JsonObject rawData;
    // @endcond

    public TextInputStatusInfo() {
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    /**
     * Gets the type of keyboard that should be displayed to the user.
     *
     * @return the keyboard type
     */
    public TextInputType getTextInputType() {
        TextInputType textInputType = TextInputType.DEFAULT;

        if ("number".equals(contentType)) {
            textInputType = TextInputType.NUMBER;
        } else if ("phonenumber".equals(contentType)) {
            textInputType = TextInputType.PHONE_NUMBER;
        } else if ("url".equals(contentType)) {
            textInputType = TextInputType.URL;
        } else if ("email".equals(contentType)) {
            textInputType = TextInputType.EMAIL;
        }

        return textInputType;
    }

    /**
     * Sets the type of keyboard that should be displayed to the user.
     *
     * @param textInputType the keyboard type
     */
    public void setTextInputType(TextInputType textInputType) {
        switch (textInputType) {
            case NUMBER:
                contentType = "number";
                break;
            case PHONE_NUMBER:
                contentType = "phonenumber";
                break;
            case URL:
                contentType = "url";
                break;
            case EMAIL:
                contentType = "number";
                break;
            case DEFAULT:
            default:
                contentType = "email";
                break;
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isPredictionEnabled() {
        return predictionEnabled;
    }

    public void setPredictionEnabled(boolean predictionEnabled) {
        this.predictionEnabled = predictionEnabled;
    }

    public boolean isCorrectionEnabled() {
        return correctionEnabled;
    }

    public void setCorrectionEnabled(boolean correctionEnabled) {
        this.correctionEnabled = correctionEnabled;
    }

    public boolean isAutoCapitalization() {
        return autoCapitalization;
    }

    public void setAutoCapitalization(boolean autoCapitalization) {
        this.autoCapitalization = autoCapitalization;
    }

    public boolean isHiddenText() {
        return hiddenText;
    }

    public void setHiddenText(boolean hiddenText) {
        this.hiddenText = hiddenText;
    }

    /** @return the raw data from the first screen device about the text input status. */
    public JsonObject getRawData() {
        return rawData;
    }

    /** @param data the raw data from the first screen device about the text input status. */
    public void setRawData(JsonObject data) {
        rawData = data;
    }

    public boolean isFocusChanged() {
        return focusChanged;
    }

    public void setFocusChanged(boolean focusChanged) {
        this.focusChanged = focusChanged;
    }
}
