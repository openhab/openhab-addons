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
package org.openhab.binding.unifiprotect.internal.api.dto.events;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiprotect.internal.api.dto.ModelKey;

/**
 * Base event fields common to all event subtypes.
 *
 * @author Dan Cunningham - Initial contribution
 */
public abstract class BaseEvent {
    public String id;
    public ModelKey modelKey = ModelKey.EVENT;
    public EventType type;
    public Long start;
    public @Nullable Long end;
    public String device;
}
