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
package org.openhab.binding.mynice.internal.discovery;

import static org.openhab.binding.mynice.internal.MyNiceBindingConstants.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mynice.internal.handler.It4WifiHandler;
import org.openhab.binding.mynice.internal.handler.MyNiceDataListener;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.Device;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyNiceDiscoveryService} is responsible for discovering all things
 * except the It4Wifi bridge itself
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MyNiceDiscoveryService extends AbstractDiscoveryService
        implements MyNiceDataListener, ThingHandlerService {

    private static final int SEARCH_TIME = 5;
    private final Logger logger = LoggerFactory.getLogger(MyNiceDiscoveryService.class);

    private @Nullable It4WifiHandler bridgeHandler;

    /**
     * Creates a MyNiceDiscoveryService with background discovery disabled.
     */
    public MyNiceDiscoveryService() {
        super(Set.of(THING_TYPE_SWING), SEARCH_TIME, false);
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof It4WifiHandler it4Handler) {
            bridgeHandler = it4Handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
        It4WifiHandler handler = bridgeHandler;
        if (handler != null) {
            handler.registerDataListener(this);
        }
    }

    @Override
    public void deactivate() {
        It4WifiHandler handler = bridgeHandler;
        if (handler != null) {
            handler.unregisterDataListener(this);
        }
        super.deactivate();
    }

    @Override
    public void onDataFetched(List<Device> devices) {
        It4WifiHandler handler = bridgeHandler;
        if (handler != null) {
            ThingUID bridgeUID = handler.getThing().getUID();
            devices.stream().filter(device -> device.type != null).forEach(device -> {
                ThingUID thingUID = switch (device.type) {
                    case SWING -> new ThingUID(THING_TYPE_SWING, bridgeUID, device.id);
                    case SLIDING -> new ThingUID(THING_TYPE_SLIDING, bridgeUID, device.id);
                    default -> null;
                };

                if (thingUID != null) {
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                            .withLabel(String.format("%s %s", device.manuf, device.prod))
                            .withRepresentationProperty(DEVICE_ID).withProperty(DEVICE_ID, device.id).build();
                    thingDiscovered(discoveryResult);
                } else {
                    logger.info("`{}` type of device is not yet supported", device.type);
                }
            });
        }
    }

    @Override
    protected void startScan() {
        It4WifiHandler handler = bridgeHandler;
        if (handler != null) {
            handler.sendCommand(CommandType.INFO);
        }
    }
}
