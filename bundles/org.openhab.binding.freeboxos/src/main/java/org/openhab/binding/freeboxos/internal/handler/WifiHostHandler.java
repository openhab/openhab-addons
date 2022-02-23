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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.LanAccessPoint;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost;
import org.openhab.binding.freeboxos.internal.api.repeater.RepeaterManager;
import org.openhab.binding.freeboxos.internal.api.wifi.APManager;
import org.openhab.binding.freeboxos.internal.api.wifi.AccessPointHost;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;

/**
 * The {@link WifiHostHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiHostHandler extends HostHandler {
    private static final String SERVEUR_HOST = "Server";

    public WifiHostHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        Optional<AccessPointHost> host = getManager(APManager.class).getHost(getMac());
        if (host.isPresent()) {
            AccessPointHost wifiHost = host.get();
            updateChannels(wifiHost.getSignal(), SERVEUR_HOST, wifiHost.getSsid());
        } else {
            Map<String, @Nullable LanHost> map = getManager(RepeaterManager.class).getHostsMap();
            LanHost wifiHost = map.get(getMac());
            LanAccessPoint accessPoint = null;
            if (wifiHost != null) {
                accessPoint = wifiHost.getAccessPoint();
            }
            if (accessPoint != null) {
                updateChannels(accessPoint.getSignal(),
                        String.format("%s-%s", accessPoint.getType(), accessPoint.getId()), accessPoint.getSsid());

            } else {
                // Not found a wifi repeater/host, so update all wifi channels to NULL
                getThing().getChannelsOfGroup(GROUP_WIFI).stream().map(Channel::getUID).filter(uid -> isLinked(uid))
                        .forEach(uid -> updateState(uid, UnDefType.NULL));
            }
        }
    }

    private void updateChannels(int rssi, String host, @Nullable String ssid) {
        updateChannelString(GROUP_WIFI, SSID, ssid);
        updateChannelString(GROUP_WIFI, WIFI_HOST, host);
        updateChannelQuantity(GROUP_WIFI, RSSI, rssi <= 0 ? new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS) : null);
        updateChannelDecimal(GROUP_WIFI, WIFI_QUALITY, rssi <= 0 ? toQoS(rssi) : null);
    }

    private int toQoS(int rssi) {
        return rssi > -50 ? 4 : rssi > -60 ? 3 : rssi > -70 ? 2 : rssi > -85 ? 1 : 0;
    }
}
