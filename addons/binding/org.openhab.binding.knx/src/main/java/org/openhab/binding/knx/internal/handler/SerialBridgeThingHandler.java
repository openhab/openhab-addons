/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.client.AbstractKNXClient;
import org.openhab.binding.knx.internal.client.SerialClient;
import org.openhab.binding.knx.internal.config.SerialBridgeConfiguration;

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
        client = new SerialClient(config.getAutoReconnectPeriod().intValue(), thing.getUID(),
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
