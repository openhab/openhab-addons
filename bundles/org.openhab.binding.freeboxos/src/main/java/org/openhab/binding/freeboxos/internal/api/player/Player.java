/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.player;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Player {
    private @NonNullByDefault({}) String mac;
    private int id;
    private @NonNullByDefault({}) String deviceName;
    private @NonNullByDefault({}) String deviceModel;
    private @NonNullByDefault({}) String apiVersion;

    public String getMac() {
        return mac.toLowerCase();
    }

    public int getId() {
        return id;
    }

    public String getModel() {
        return deviceModel;
    }

    public String getName() {
        return deviceName;
    }

    public String getApiVersion() {
        return apiVersion;
    }
}
