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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A configured channel that holds a group address, with the Items linked to it.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ChannelUseDTO {
    public String thingUID;
    public @Nullable String thingLabel;
    public String channelId;
    public @Nullable String channelLabel;
    public @Nullable String channelType;
    /** {@code main}, {@code status} or {@code listening}. */
    public String role;
    public List<String> items;
}
