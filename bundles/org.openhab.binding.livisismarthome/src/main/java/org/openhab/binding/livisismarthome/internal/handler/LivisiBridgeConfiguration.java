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
package org.openhab.binding.livisismarthome.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Hilbrand Bouwkamp - Initial contribution
 * @author Sven Strohschein - Renamed from Innogy to Livisi
 */
@NonNullByDefault
public class LivisiBridgeConfiguration {

    public String host = "";
    public String password = "";
    public int webSocketIdleTimeout = 900;
}
