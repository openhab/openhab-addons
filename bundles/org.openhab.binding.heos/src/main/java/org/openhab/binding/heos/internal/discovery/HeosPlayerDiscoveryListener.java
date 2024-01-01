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
package org.openhab.binding.heos.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.heos.internal.handler.HeosBridgeHandler;

/**
 * The {@link HeosPlayerDiscoveryListener } is an Event Listener
 * for the HEOS network. Handler which wants the get informed
 * if the player or groups within the HEOS network have changed has to
 * implement this class and register itself at the {@link HeosBridgeHandler}
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public interface HeosPlayerDiscoveryListener {
    void playerChanged();
}
