/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.cloud.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CommandRequest} represents a request to the cloud
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class CommandRequest {
    public List<Command<?>> commands;

    public CommandRequest(List<Command<?>> commands) {
        this.commands = commands;
    }

    public static class Command<T> {
        public String code;
        public T value;

        public Command(String code, T value) {
            this.code = code;
            this.value = value;
        }
    }
}
