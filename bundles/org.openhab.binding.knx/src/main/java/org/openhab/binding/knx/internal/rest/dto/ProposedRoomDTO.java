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
 * A space of the proposal and the location group the page may create for it. Of a request only {@code groupName},
 * {@code label}, {@code tag}, {@code category} and {@code parent} are read: {@code parent} is the
 * {@code groupName} of another entry, {@code tag} is {@code ""} for none and, when left out, {@code Room}.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProposedRoomDTO {
    public @Nullable String id;
    public String name;
    public @Nullable String groupName;
    public @Nullable String label;
    public @Nullable String type;
    public @Nullable String parentId;
    public @Nullable String parent;
    public @Nullable String tag;
    public @Nullable List<String> allowedTags;
    public @Nullable String category;
    public @Nullable Boolean exists;
}
