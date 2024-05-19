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
package org.openhab.binding.netatmo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NAThingConfiguration} is responsible for holding
 * configuration information for any Netatmo thing module or device
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NAThingConfiguration {
    public static final String ID = "id";

    protected String id = "";
    protected int refreshInterval = -1;

    public int getRefreshInterval() {
        int local = refreshInterval;
        return local == -1 ? 600 : local;
    }

    public String getId() {
        return id;
    }
}
