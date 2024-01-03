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
package org.openhab.binding.mynice.internal.discovery;

import static org.openhab.binding.mynice.internal.MyNiceBindingConstants.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.handler.It4WifiHandler;
import org.openhab.binding.mynice.internal.handler.MyNiceDataListener;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
import org.openhab.binding.mynice.internal.xml.dto.Device;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyNiceDiscoveryService} is responsible for discovering all things
 * except the It4Wifi bridge itself
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MyNiceDiscoveryService.class)
@NonNullByDefault
public class MyNiceDiscoveryService extends AbstractThingHandlerDiscoveryService<It4WifiHandler>
        implements MyNiceDataListener {

    private static final int SEARCH_TIME = 5;
    private final Logger logger = LoggerFactory.getLogger(MyNiceDiscoveryService.class);

    /**
     * Creates a MyNiceDiscoveryService with background discovery disabled.
     */
    public MyNiceDiscoveryService() {
        super(It4WifiHandler.class, Set.of(THING_TYPE_SWING, THING_TYPE_SLIDING), SEARCH_TIME, false);
    }

    @Override
    public void initialize() {
        thingHandler.registerDataListener(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.unregisterDataListener(this);
    }

    @Override
    public void onDataFetched(List<Device> devices) {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
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

    @Override
    protected void startScan() {
        thingHandler.sendCommand(CommandType.INFO);
    }
}
