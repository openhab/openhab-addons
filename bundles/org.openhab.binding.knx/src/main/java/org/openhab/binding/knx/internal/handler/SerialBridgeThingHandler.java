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
package org.openhab.binding.knx.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.client.AbstractKNXClient;
import org.openhab.binding.knx.internal.client.SerialClient;
import org.openhab.binding.knx.internal.config.SerialBridgeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX Serial/USB Gateway, that either acts a a
 * conduit for other {@link DeviceThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 * @author Simon Kaufmann - Refactoring & cleanup
 */
@NonNullByDefault
public class SerialBridgeThingHandler extends KNXBridgeBaseThingHandler {

    private final SerialClient client;

    public SerialBridgeThingHandler(Bridge bridge) {
        super(bridge);
        SerialBridgeConfiguration config = getConfigAs(SerialBridgeConfiguration.class);
        client = new SerialClient(config.getAutoReconnectPeriod(), thing.getUID(),
                config.getResponseTimeout().intValue(), config.getReadingPause().intValue(),
                config.getReadRetriesLimit().intValue(), getScheduler(), config.getSerialPort(), this);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        client.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        client.dispose();
    }

    @Override
    protected AbstractKNXClient getClient() {
        return client;
    }
}
