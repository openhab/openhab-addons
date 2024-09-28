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
package org.openhab.binding.chatgpt.internal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatTools {

    private String type;
    private ChatFunction function;

    public ChatTools() {
    }

    public String getType() {
        return type;
    }

    public ChatFunction getFunction() {
        return function;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFunction(ChatFunction function) {
        this.function = function;
    }
}
