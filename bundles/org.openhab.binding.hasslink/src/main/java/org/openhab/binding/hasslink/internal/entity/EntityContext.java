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
package org.openhab.binding.hasslink.internal.entity;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.HomeAssistantConfig;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;

/**
 * Provides shared bridge, attribute-linking, and state-update context for Home Assistant entities.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record EntityContext(@Nullable HassLinkBridgeHandler bridgeHandler, Predicate<String> isAttributeLinked,
        @Nullable BiConsumer<String, ParsedData> asyncStateConsumer) {

    public static final EntityContext EMPTY = new EntityContext(null, attr -> true, null);

    public boolean isLinked(String attribute) {
        return isAttributeLinked.test(attribute);
    }

    public @Nullable HomeAssistantConfig getHaConfig() {
        HassLinkBridgeHandler bridgeHandler = this.bridgeHandler;
        return bridgeHandler != null ? bridgeHandler.getHaConfig() : null;
    }
}
