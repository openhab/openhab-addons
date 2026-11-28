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
 * A proposed Thing of type {@code knx:device}; also what the page sends back to the preview, of which
 * only {@code id}, {@code label}, {@code channels} and {@code target} are read. {@code target} is the UID of an
 * existing device Thing, or its id below this bridge, to write the channel lines for; never part of an answer.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProposedThingDTO {
    public @Nullable String id;
    public String uid;
    public @Nullable String label;
    public String source;
    public String sourceId;
    public @Nullable String roomId;
    public @Nullable String room;
    public @Nullable List<ProposedChannelDTO> channels;
    public @Nullable String target;
}
