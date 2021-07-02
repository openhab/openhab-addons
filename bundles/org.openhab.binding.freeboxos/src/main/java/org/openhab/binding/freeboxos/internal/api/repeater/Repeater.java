/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.repeater;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Repeater {
    private @Nullable String connection;
    private long bootTime;
    private boolean ledActivated;
    private @NonNullByDefault({}) String mainMac;
    private @NonNullByDefault({}) String sn;
    private int id;
    private @Nullable String name;
    private @NonNullByDefault({}) String model;
    private @NonNullByDefault({}) String firmwareVersion;

    public String getMac() {
        return mainMac.toLowerCase();
    }

    public int getId() {
        return id;
    }

    public @Nullable String getName() {
        return name;
    }

    public String getSerial() {
        return sn;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getModel() {
        return model;
    }

    public long getBootTime() {
        return bootTime;
    }

    public @Nullable String getConnection() {
        return connection;
    }

    public boolean getLedActivated() {
        return ledActivated;
    }
}
