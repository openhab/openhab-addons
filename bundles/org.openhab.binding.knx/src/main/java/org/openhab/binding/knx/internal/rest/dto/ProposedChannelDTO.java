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
 * A proposed channel with its Item; of a request only {@code id}, {@code label}, {@code channelType},
 * {@code selected}, {@code parts} and {@code item} are read.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProposedChannelDTO {
    public @Nullable String id;
    public @Nullable String channelType;
    public List<String> allowedChannelTypes;
    public @Nullable String label;
    public boolean selected;
    public boolean statusOnly;
    public boolean combined;
    public @Nullable List<ProposedPartDTO> parts;
    public @Nullable ProposedItemDTO item;
    public @Nullable List<ProposedChannelDTO> split;
    public List<String> alreadyConfigured;
    public List<String> notes;
}
