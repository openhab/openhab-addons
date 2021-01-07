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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.LanAccessPoint;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost;
import org.openhab.binding.freeboxos.internal.api.wifi.AccessPointHost;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link WifiHostHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiHostHandler extends HostHandler {
    public WifiHostHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        Optional<AccessPointHost> host = bridgeHandler.getWifiManager().getHost(getMac());
        if (host.isPresent()) {
            AccessPointHost wifiHost = host.get();
            int rssi = wifiHost.getSignal();
            updateChannels(rssi, "server", wifiHost.getSsid());
            return;
        }

        Map<String, @Nullable LanHost> map = bridgeHandler.getRepeaterManager().getHostsMap();
        LanHost wifiHost = map.get(getMac());
        if (wifiHost != null) {
            LanAccessPoint accessPoint = wifiHost.getAccessPoint();
            if (accessPoint != null) {
                int rssi = accessPoint.getSignal();
                updateChannels(rssi, String.format("%s-%s", accessPoint.getType(), accessPoint.getId()),
                        accessPoint.getSsid());
                return;
            }
        }
        updateChannels(0, null, null);
    }

    private void updateChannels(int rssi, @Nullable String host, @Nullable String ssid) {
        if (rssi != 0) {
            updateChannelDecimal(WIFI, WIFI_QUALITY, toQoS(rssi));
            updateChannelString(WIFI, SSID, ssid);
            updateChannelString(WIFI, WIFI_HOST, host);
            updateChannelQuantity(WIFI, RSSI, new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS));
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private int toQoS(int rssi) {
        if (rssi > -50) {
            return 4;
        } else if (rssi > -60) {
            return 3;
        } else if (rssi > -70) {
            return 2;
        } else if (rssi > -85) {
            return 1;
        }
        return 0;
    }
}
