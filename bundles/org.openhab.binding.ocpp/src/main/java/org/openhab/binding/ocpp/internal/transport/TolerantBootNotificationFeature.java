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
package org.openhab.binding.ocpp.internal.transport;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.chargetime.ocpp.feature.Feature;
import eu.chargetime.ocpp.feature.profile.ServerCoreEventHandler;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.BootNotificationConfirmation;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;

/**
 * A BootNotification {@link Feature} that accepts a payload the library's strict CiString20 validation
 * would reject (see {@link TolerantBootNotificationRequest}).
 *
 * <p>
 * Registering it with {@code FeatureRepository.addFeature(...)} AFTER the core profile overrides the
 * strict feature: the repository keys features by action, so the later put wins and the inbound path
 * deserializes into {@link #getRequestType()}. A charger whose model or vendor exceeds 20 characters,
 * or omits one, can therefore still boot. The boot itself is handled identically — this delegates to
 * the same {@link ServerCoreEventHandler} the core feature uses.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class TolerantBootNotificationFeature implements Feature {

    private final ServerCoreEventHandler handler;

    public TolerantBootNotificationFeature(ServerCoreEventHandler handler) {
        this.handler = handler;
    }

    @Override
    @NonNullByDefault({})
    public Confirmation handleRequest(UUID sessionIndex, Request request) {
        return handler.handleBootNotificationRequest(sessionIndex, (BootNotificationRequest) request);
    }

    @Override
    public Class<? extends Request> getRequestType() {
        return TolerantBootNotificationRequest.class;
    }

    @Override
    public Class<? extends Confirmation> getConfirmationType() {
        return BootNotificationConfirmation.class;
    }

    @Override
    public String getAction() {
        return "BootNotification";
    }
}
