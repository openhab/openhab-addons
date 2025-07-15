/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ondilo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OndiloBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloBridgeConfiguration {

    public String url = "http://localhost:8080";
    public int refreshInterval = 900; // Default to 15 minutes

    public String getURL() {
        return url;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }
}
