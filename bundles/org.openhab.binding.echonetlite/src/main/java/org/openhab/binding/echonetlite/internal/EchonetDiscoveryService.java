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
package org.openhab.binding.echonetlite.internal;

import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.PROPERTY_NAME_CLASS_CODE;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.PROPERTY_NAME_GROUP_CODE;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.PROPERTY_NAME_HOSTNAME;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.PROPERTY_NAME_INSTANCE;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.PROPERTY_NAME_INSTANCE_KEY;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.PROPERTY_NAME_PORT;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.THING_TYPE_ECHONET_DEVICE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Barker - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = EchonetDiscoveryService.class)
@NonNullByDefault
public class EchonetDiscoveryService extends AbstractThingHandlerDiscoveryService<EchonetLiteBridgeHandler>
        implements EchonetDiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(EchonetDiscoveryService.class);

    public EchonetDiscoveryService() {
        super(EchonetLiteBridgeHandler.class, Set.of(THING_TYPE_ECHONET_DEVICE), 10);
    }

    @Override
    protected void startScan() {
        logger.debug("startScan: {}", thingHandler);
        thingHandler.startDiscovery(this);
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("stopScan: {}", thingHandler);
        thingHandler.stopDiscovery();
    }

    @Override
    public void onDeviceFound(String identifier, InstanceKey instanceKey) {
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_ECHONET_DEVICE, thingHandler.getThing().getUID(), identifier))
                .withProperty(PROPERTY_NAME_INSTANCE_KEY, instanceKey.representationProperty())
                .withProperty(PROPERTY_NAME_HOSTNAME, instanceKey.address.getAddress().getHostAddress())
                .withProperty(PROPERTY_NAME_PORT, instanceKey.address.getPort())
                .withProperty(PROPERTY_NAME_GROUP_CODE, instanceKey.klass.groupCode())
                .withProperty(PROPERTY_NAME_CLASS_CODE, instanceKey.klass.classCode())
                .withProperty(PROPERTY_NAME_INSTANCE, instanceKey.instance).withBridge(thingHandler.getThing().getUID())
                .withRepresentationProperty(PROPERTY_NAME_INSTANCE_KEY).build();
        thingDiscovered(discoveryResult);
    }
}
