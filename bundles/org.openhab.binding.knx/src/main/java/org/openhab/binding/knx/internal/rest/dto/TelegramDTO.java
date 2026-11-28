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
 * One row of the group monitor.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class TelegramDTO {
    public long number;
    public String time;
    public String source;
    public @Nullable String sourceName;
    public String destination;
    public @Nullable String destinationName;
    public String type;
    public String direction;
    public @Nullable String payload;
    public @Nullable String dataType;
    public @Nullable String value;
    public @Nullable String unit;
    public String security;
    public @Nullable String reason;
    public @Nullable String outcome;
    public boolean plainOnSecuredAddress;
}
