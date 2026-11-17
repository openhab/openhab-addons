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
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The Item of a proposed channel; of a request {@code allowedTypes} and {@code allowedItemTypes} are not read.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProposedItemDTO {
    public @Nullable String name;
    public @Nullable String type;
    public List<String> allowedTypes;
    /** Per channel type of the channel's {@code allowedChannelTypes} its Item types, the first one proposed. */
    public Map<String, List<String>> allowedItemTypes;
    public @Nullable String label;
    public @Nullable String category;
    public @Nullable List<String> tags;
    public @Nullable List<String> groups;
}
