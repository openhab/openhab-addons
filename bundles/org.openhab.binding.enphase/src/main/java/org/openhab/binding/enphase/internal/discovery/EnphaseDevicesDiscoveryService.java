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
package org.openhab.binding.enphase.internal.discovery;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.CONFIG_SERIAL_NUMBER;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.THING_TYPE_ENPHASE_INVERTER;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.THING_TYPE_ENPHASE_RELAY;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enphase.internal.EnphaseBindingConstants;
import org.openhab.binding.enphase.internal.EnphaseBindingConstants.EnphaseDeviceType;
import org.openhab.binding.enphase.internal.dto.InventoryJsonDTO.DeviceDTO;
import org.openhab.binding.enphase.internal.dto.InverterDTO;
import org.openhab.binding.enphase.internal.handler.EnvoyBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service to discovery Enphase inverters connected to an Envoy gateway.
 *
 * @author Thomas Hentschel - Initial contribution
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = EnphaseDevicesDiscoveryService.class)
@NonNullByDefault
public class EnphaseDevicesDiscoveryService extends AbstractThingHandlerDiscoveryService<EnvoyBridgeHandler> {
    private static final int TIMEOUT_SECONDS = 20;

    private final Logger logger = LoggerFactory.getLogger(EnphaseDevicesDiscoveryService.class);

    public EnphaseDevicesDiscoveryService() {
        super(EnvoyBridgeHandler.class, Set.of(THING_TYPE_ENPHASE_INVERTER), TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        removeOlderResults(getTimestampOfLastScan());
        if (!thingHandler.isOnline()) {
            logger.debug("Envoy handler not available or online: {}", thingHandler);
            return;
        }
        final ThingUID uid = thingHandler.getThing().getUID();

        scanForInverterThings(thingHandler, uid);
        scanForDeviceThings(thingHandler, uid);
    }

    private void scanForInverterThings(final EnvoyBridgeHandler envoyHandler, final ThingUID bridgeID) {
        final Map<String, @Nullable InverterDTO> inverters = envoyHandler.getInvertersData(true);

        if (inverters == null) {
            logger.debug("No inverter data for Enphase inverters in discovery for Envoy {}.", bridgeID);
        } else {
            for (final Entry<String, @Nullable InverterDTO> entry : inverters.entrySet()) {
                discover(bridgeID, entry.getKey(), THING_TYPE_ENPHASE_INVERTER, "Inverter ");
            }
        }
    }

    /**
     * Scans for other device things ('other' as in: no inverters).
     *
     * @param envoyHandler
     * @param bridgeID
     */
    private void scanForDeviceThings(final EnvoyBridgeHandler envoyHandler, final ThingUID bridgeID) {
        final Map<String, @Nullable DeviceDTO> devices = envoyHandler.getDevices(true);

        if (devices == null) {
            logger.debug("No device data for Enphase devices in discovery for Envoy {}.", bridgeID);
        } else {
            for (final Entry<String, @Nullable DeviceDTO> entry : devices.entrySet()) {
                final DeviceDTO dto = entry.getValue();
                final EnphaseDeviceType type = dto == null ? null : EnphaseDeviceType.safeValueOf(dto.type);

                if (type == EnphaseDeviceType.NSRB) {
                    discover(bridgeID, entry.getKey(), THING_TYPE_ENPHASE_RELAY, "Relay ");
                }
            }
        }
    }

    private void discover(final ThingUID bridgeID, final String serialNumber, final ThingTypeUID typeUID,
            final String label) {
        final String shortSerialNumber = EnphaseBindingConstants.defaultPassword(serialNumber);
        final ThingUID thingUID = new ThingUID(typeUID, bridgeID, shortSerialNumber);
        final Map<String, Object> properties = new HashMap<>(1);

        properties.put(CONFIG_SERIAL_NUMBER, serialNumber);
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeID)
                .withRepresentationProperty(CONFIG_SERIAL_NUMBER).withProperties(properties)
                .withLabel("Enphase " + label + shortSerialNumber).build();
        thingDiscovered(discoveryResult);
    }
}
