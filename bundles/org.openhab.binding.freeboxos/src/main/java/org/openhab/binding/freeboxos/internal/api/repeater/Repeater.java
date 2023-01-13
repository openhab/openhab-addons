/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ModelInfo;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.RepeaterConnection;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.RepeaterStatus;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Repeater {
    private boolean ledActivated;
    private boolean enabled;
    private @Nullable String name;
    private RepeaterConnection connection = RepeaterConnection.UNKNOWN;
    private @Nullable ZonedDateTime bootTime;
    private RepeaterStatus status = RepeaterStatus.UNKNOWN;
    private @Nullable String mainMac;
    private @Nullable String sn;
    private int id;
    private @Nullable String apiVer;
    private @Nullable ZonedDateTime lastSeen;
    private ModelInfo model = ModelInfo.UNKNOWN;
    private @Nullable String firmwareVersion;

    public RepeaterConnection getConnection() {
        return connection;
    }

    private ZonedDateTime getBootTime() {
        return Objects.requireNonNull(bootTime);
    }

    public long getUptimeVal() {
        return Duration.between(getBootTime(), ZonedDateTime.now()).toSeconds();
    }

    public boolean isLedActivated() {
        return ledActivated;
    }

    public void setLedActivated(boolean ledActivated) {
        this.ledActivated = ledActivated;
    }

    public RepeaterStatus getStatus() {
        return status;
    }

    public String getMainMac() {
        return Objects.requireNonNull(mainMac);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSn() {
        return Objects.requireNonNull(sn);
    }

    public int getId() {
        return id;
    }

    public String getApiVer() {
        return Objects.requireNonNull(apiVer);
    }

    public ZonedDateTime getLastSeen() {
        return Objects.requireNonNull(lastSeen);
    }

    public String getName() {
        return Objects.requireNonNull(name);
    }

    public ModelInfo getModel() {
        return model;
    }

    public String getFirmwareVersion() {
        return Objects.requireNonNull(firmwareVersion);
    }

}
