/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.FbxDevice;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Repeater extends FbxDevice {
    public static class RepeatersResponse extends Response<List<Repeater>> {
    }

    public static class RepeaterResponse extends Response<Repeater> {
    }

    private @Nullable String connection;
    private @Nullable ZonedDateTime bootTime;
    private boolean ledActivated;
    private @NonNullByDefault({}) String sn;
    private @NonNullByDefault({}) String firmwareVersion;

    public @Nullable ZonedDateTime getBootTime() {
        return bootTime;
    }

    public @Nullable String getConnection() {
        return connection;
    }

    public boolean getLedActivated() {
        return ledActivated;
    }

    public String getSerial() {
        return sn;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }
}
