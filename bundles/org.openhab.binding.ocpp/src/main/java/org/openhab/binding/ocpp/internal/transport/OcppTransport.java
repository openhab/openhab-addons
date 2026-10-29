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
import java.util.concurrent.CompletionStage;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;

/**
 * The seam between this binding and the underlying OCPP protocol library.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public interface OcppTransport {

    void start(String host, int port);

    void stop();

    boolean isRunning();

    void closeSession(UUID session);

    CompletionStage<Confirmation> send(UUID session, Request request);
}
