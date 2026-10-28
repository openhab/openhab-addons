/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.transit.internal.handler;

import static org.openhab.binding.transit.internal.TransitBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transit.internal.config.TransitNetworkAlertsConfiguration;
import org.openhab.binding.transit.internal.net.dto.NetworkAlertsResult;
import org.openhab.core.thing.Thing;

/**
 * The {@link TransitNetworkAlertsHandler} shows the active service alerts of one or more transit networks.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitNetworkAlertsHandler extends TransitPollingHandler<NetworkAlertsResult> {

    private TransitNetworkAlertsConfiguration config = new TransitNetworkAlertsConfiguration();
    private String networkIds = "";

    public TransitNetworkAlertsHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected @Nullable String loadConfiguration() {
        config = getConfigAs(TransitNetworkAlertsConfiguration.class);
        // network IDs may contain blanks (e.g. "BC Transit West Kootenay|West Kootenay"), only the entries are trimmed
        networkIds = normalizeList(config.networkIds);
        if (networkIds.isEmpty()) {
            return "@text/offline.conf-error-no-network-id";
        }
        return null;
    }

    @Override
    protected long getRefreshInterval() {
        return config.refreshInterval;
    }

    @Override
    protected NetworkAlertsResult fetch(TransitBridgeHandler bridgeHandler) throws IOException, InterruptedException {
        return bridgeHandler.getNetworkAlerts(networkIds);
    }

    @Override
    protected void publish(NetworkAlertsResult result, long nowSeconds) {
        publishAlerts(CHANNEL_GROUP_ALERTS, result.getAlerts(), nowSeconds);
    }
}
