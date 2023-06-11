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
package org.openhab.binding.paradoxalarm.internal.discovery;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.paradoxalarm.internal.communication.IParadoxCommunicator;
import org.openhab.binding.paradoxalarm.internal.exceptions.ParadoxRuntimeException;
import org.openhab.binding.paradoxalarm.internal.handlers.ParadoxIP150BridgeHandler;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxInformation;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.openhab.binding.paradoxalarm.internal.model.Partition;
import org.openhab.binding.paradoxalarm.internal.model.Zone;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxDiscoveryService} is responsible for discovery of partitions, zones and the panel once bridge is
 * created.
 *
 * @author Konstnatin Polihronov - Initial Contribution
 */
public class ParadoxDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(ParadoxDiscoveryService.class);

    private ParadoxIP150BridgeHandler ip150BridgeHandler;

    public ParadoxDiscoveryService(ParadoxIP150BridgeHandler ip150BridgeHandler) {
        super(SUPPORTED_THING_TYPES_UIDS, 15, false);
        this.ip150BridgeHandler = ip150BridgeHandler;
    }

    @Override
    protected void startScan() {
        IParadoxCommunicator communicator = ip150BridgeHandler.getCommunicator();
        if (communicator != null && communicator.isOnline()) {
            ParadoxPanel panel = ip150BridgeHandler.getPanel();
            discoverPanel(panel.getPanelInformation());
            discoverPartitions(panel.getPartitions());
            discoverZones(panel.getZones());
        } else {
            logger.debug("Communicator null or not online. Trace:", new ParadoxRuntimeException());
        }
    }

    private void discoverPanel(ParadoxInformation panelInformation) {
        if (panelInformation != null) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(PANEL_TYPE_PROPERTY_NAME, panelInformation.getPanelType().name());
            properties.put(PANEL_SERIAL_NUMBER_PROPERTY_NAME, panelInformation.getSerialNumber());
            properties.put(PANEL_APPLICATION_VERSION_PROPERTY_NAME, panelInformation.getApplicationVersion());
            properties.put(PANEL_BOOTLOADER_VERSION_PROPERTY_NAME, panelInformation.getBootLoaderVersion());
            properties.put(PANEL_HARDWARE_VERSION_PROPERTY_NAME, panelInformation.getHardwareVersion());

            ThingUID bridgeUid = ip150BridgeHandler.getThing().getUID();
            ThingUID thingUID = new ThingUID(PANEL_THING_TYPE_UID, bridgeUid, PARADOX_PANEL_THING_TYPE_ID);
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeUid).withLabel("Paradox panel - " + panelInformation.getPanelType()).build();
            logger.debug("Panel DiscoveryResult={}", result);
            thingDiscovered(result);
        }
    }

    private void discoverPartitions(List<Partition> partitions) {
        partitions.stream().forEach(partition -> {
            String thingId = PARTITION_THING_TYPE_ID + partition.getId();
            String label = partition.getLabel();
            ThingUID bridgeUid = ip150BridgeHandler.getThing().getUID();

            ThingUID thingUID = new ThingUID(PARTITION_THING_TYPE_UID, bridgeUid, thingId);
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUid)
                    .withLabel("Partition " + label).withProperty(PARTITION_THING_TYPE_ID, thingId)
                    .withProperty("id", partition.getId()).build();
            logger.debug("Partition DiscoveryResult={}", result);

            thingDiscovered(result);
        });
    }

    private void discoverZones(List<Zone> zones) {
        zones.stream().forEach(zone -> {
            String thingId = zone.getLabel().replaceAll(" ", "_");
            String label = zone.getLabel();
            ThingUID bridgeUid = ip150BridgeHandler.getThing().getUID();

            ThingUID thingUID = new ThingUID(ZONE_THING_TYPE_UID, bridgeUid, thingId);
            DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUid)
                    .withLabel("Zone " + label).withProperty(ZONE_THING_TYPE_ID, thingId)
                    .withProperty("id", zone.getId()).build();
            logger.debug("Zone DiscoveryResult={}", result);

            thingDiscovered(result);
        });
    }
}
