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
package org.openhab.binding.atv.internal.client.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.FeatureName;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * Contract returned by each protocol's {@code setup()}: how to connect and close the
 * protocol, and what it contributes to the device relay.
 *
 * @param protocol protocol this setup data belongs to
 * @param connect starts the protocol connection; the future resolves to {@code true} when
 *            the connection was established
 * @param close closes the protocol connection and releases its resources
 * @param deviceInfo returns protocol-specific device information fields, gathered after
 *            connect
 * @param interfaces capability interface implementations keyed by interface class (e.g.
 *            {@code RemoteControl.class} → the protocol's remote control implementation)
 * @param features features this protocol instance supports
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record SetupData(Protocol protocol, Supplier<CompletableFuture<Boolean>> connect, Runnable close,
        Supplier<Map<String, Object>> deviceInfo, Map<Class<?>, Object> interfaces, Set<FeatureName> features) {
}
