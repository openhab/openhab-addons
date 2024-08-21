/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.model.Event;

/**
 * The {@link EventHandler} is responsible for handling events, which were sent via Server-Sent event interface.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public interface EventHandler {
    void handle(Event event);
}
