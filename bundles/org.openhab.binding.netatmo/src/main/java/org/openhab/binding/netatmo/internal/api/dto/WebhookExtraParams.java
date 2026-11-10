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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

/**
 * The {@link WebhookExtraParams} holds
 * extra data provided in a webhook event
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class WebhookExtraParams {
    int correlationId;
    int sequenceId;
    String source = "";
    public ModuleType deviceType = ModuleType.UNKNOWN;
    public EventType eventType = EventType.UNKNOWN;
    String mode;
    String roomId = "";
    public Double setpoint;
    public Double temperature;
    public String boilerStatus;

    @Nullable
    ZonedDateTime ts;
    // To bo continued
}
