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
package org.openhab.binding.homeconnectdirect.internal.servlet.model;

import java.time.OffsetDateTime;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.handler.model.MessageType;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Action;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;

/**
 * Message filter model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record MessageFilter(@Nullable OffsetDateTime start, @Nullable OffsetDateTime end, @Nullable MessageType type,
        @Nullable Set<Resource> resources, @Nullable Set<Action> actions, @Nullable Set<String> valueKeys,
        @Nullable Set<String> descriptionChangeKeys) {
}
