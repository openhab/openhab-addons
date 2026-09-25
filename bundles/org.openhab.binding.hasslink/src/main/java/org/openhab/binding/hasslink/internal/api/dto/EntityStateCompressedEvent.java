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
package org.openhab.binding.hasslink.internal.api.dto;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * DTO representing the root payload of compressed entity updates received from Home Assistant
 * over WebSocket when subscribed via {@code subscribe_entities}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class EntityStateCompressedEvent {

    /** Map of newly added or initialized entities (Entity ID -&gt; State). */
    public @Nullable Map<String, CompressedEntityState> a;

    /** Map of entity state diffs and attribute updates (Entity ID -&gt; Delta). */
    public @Nullable Map<String, CompressedEntityDiff> c;

    /** List of Entity IDs that were removed from Home Assistant. */
    public @Nullable List<String> r;
}
