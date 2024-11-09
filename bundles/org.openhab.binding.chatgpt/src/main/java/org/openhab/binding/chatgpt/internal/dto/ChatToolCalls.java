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
public class ChatToolCalls {

    String id;
    ChatFunctionCall function;
    String type;

    public String getId() {
        return id;
    }

    public ChatFunctionCall getFunction() {
        return function;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFunction(ChatFunctionCall function) {
        this.function = function;
    }

    public void setType(String type) {
        this.type = type;
    }
}
