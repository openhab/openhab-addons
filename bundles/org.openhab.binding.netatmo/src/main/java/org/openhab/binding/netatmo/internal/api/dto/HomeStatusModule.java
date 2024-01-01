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
package org.openhab.binding.netatmo.internal.api.dto;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.AlimentationStatus;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.BatteryState;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SdCardStatus;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SirenStatus;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link HomeStatusModule} holds module informations returned by getHomeData endpoint
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class HomeStatusModule extends NAThing {
    private @Nullable String firmwareName;
    private @Nullable String wifiState;
    private @Nullable String status;
    private @Nullable OnOffType monitoring;
    private FloodLightMode floodlight = FloodLightMode.UNKNOWN;
    private SdCardStatus sdStatus = SdCardStatus.UNKNOWN;
    private AlimentationStatus alimStatus = AlimentationStatus.UNKNOWN;
    private SirenStatus sirenStatus = SirenStatus.UNKNOWN;
    private @Nullable String vpnUrl;
    private boolean isLocal;
    private BatteryState batteryState = BatteryState.UNKNOWN;
    private int batteryLevel;

    private @Nullable OpenClosedType boilerStatus;
    private boolean boilerValveComfortBoost;

    public State getBoilerStatus() {
        OpenClosedType status = boilerStatus;
        return status != null ? status : UnDefType.NULL;
    }

    public boolean getBoilerValveComfortBoost() {
        return boilerValveComfortBoost;
    }

    public Optional<String> getFirmwareName() {
        return Optional.ofNullable(firmwareName);
    }

    public Optional<String> getWifiState() {
        return Optional.ofNullable(wifiState);
    }

    public Optional<String> getStatus() {
        return Optional.ofNullable(status);
    }

    public State getMonitoring() {
        OnOffType localStatus = monitoring;
        return localStatus != null ? localStatus // Monitoring is always active on Doorbell
                : getType().equals(ModuleType.DOORBELL) ? OnOffType.ON : UnDefType.NULL;
    }

    public FloodLightMode getFloodlight() {
        return floodlight;
    }

    public SdCardStatus getSdStatus() {
        return sdStatus;
    }

    public AlimentationStatus getAlimStatus() {
        return alimStatus;
    }

    public SirenStatus getSirenStatus() {
        return sirenStatus;
    }

    public @Nullable String getVpnUrl() {
        return vpnUrl;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public BatteryState getBatteryState() {
        return batteryState;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }
}
