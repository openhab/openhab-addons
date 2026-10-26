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
package org.openhab.binding.awtrixng.internal.discovery;

import static org.openhab.binding.awtrixng.internal.AwtrixNgBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awtrixng.internal.handler.AwtrixNgBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * The {@link AwtrixNgBridgeDiscoveryService} offers the apps that the clock shows and that no thing represents yet. It
 * gets them from the bridge as the clock switches to them, so a scan has nothing to do.
 *
 * Adopting an app pushes a new, empty app under its name: NG has no way to read the content of an existing app, so
 * what another system pushed under that name is replaced.
 *
 * @author Thomas Lauterbach - Initial contribution (app discovery of the AWTRIX Light binding)
 * @author Keith T. Garner - Adapted to AWTRIX NG
 */
@NonNullByDefault
public class AwtrixNgBridgeDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private @Nullable AwtrixNgBridgeHandler bridgeHandler;

    public AwtrixNgBridgeDiscoveryService() {
        super(Set.of(THING_TYPE_APP), 3, true);
    }

    /** Offers the app, unless the clock has not told its unique ID yet. */
    public void appDiscovered(String appName) {
        AwtrixNgBridgeHandler localHandler = bridgeHandler;
        if (localHandler == null) {
            return;
        }
        String uniqueId = localHandler.getThing().getProperties().get(PROP_UNIQUEID);
        if (uniqueId == null) {
            return;
        }
        ThingUID bridgeUid = localHandler.getThing().getUID();
        thingDiscovered(DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_APP, bridgeUid, appName))
                .withBridge(bridgeUid).withProperty(PROP_APPNAME, appName)
                .withProperty(PROP_APPID, appId(uniqueId, appName)).withRepresentationProperty(PROP_APPID)
                .withLabel("AWTRIX NG App " + appName + " (replaces the existing app)").build());
    }

    /** The identity of an app over all clocks, as it is also the representation property of the thing. */
    public static String appId(String clockUniqueId, String appName) {
        return clockUniqueId + "-" + appName;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof AwtrixNgBridgeHandler bridge) {
            this.bridgeHandler = bridge;
            bridge.setAppDiscoveryCallback(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void deactivate() {
        AwtrixNgBridgeHandler localHandler = bridgeHandler;
        if (localHandler != null) {
            localHandler.removeAppDiscoveryCallback();
        }
        super.deactivate();
    }

    @Override
    protected void startScan() {
        // Nothing to do, the bridge reports the apps as the clock shows them
    }
}
