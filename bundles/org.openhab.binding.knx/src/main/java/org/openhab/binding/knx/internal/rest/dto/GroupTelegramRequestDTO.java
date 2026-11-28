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
package org.openhab.binding.knx.internal.rest.dto;

import org.eclipse.jdt.annotation.Nullable;

/**
 * One group telegram to send: {@code action} is {@code read} or {@code write}; a write needs {@code dpt} with a
 * sub number and {@code value}, written as a channel takes a command.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class GroupTelegramRequestDTO {
    public @Nullable String address;
    public @Nullable String action;
    public @Nullable String dpt;
    public @Nullable String value;
}
