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

import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatFunctionCall {

    private @Nullable String name;
    private @Nullable String arguments;

    public String getName() {
        return name;
    }

    public String getArguments() {
        return arguments;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}
