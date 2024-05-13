package org.openhab.binding.onecta.internal.service;

import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;
import static org.openhab.binding.onecta.internal.OnectaGatewayConstants.PROPERTY_GW_DISCOVERED;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.api.dto.units.Unit;
import org.openhab.binding.onecta.internal.handler.OnectaBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(OnectaBridgeHandler.class);
    @Nullable
    private OnectaBridgeHandler bridgeHandler = null;
    private final OnectaConnectionClient onectaConnectionClient = new OnectaConnectionClient();

    public DeviceDiscoveryService(OnectaBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(20);
        this.bridgeHandler = bridgeHandler;
    }

    public void discoverDevices() {
        startScan();
    }

    @Override
    protected void startScan() throws IllegalArgumentException {

        if (bridgeHandler == null) {
            return;
        }
        // Trigger no scan if offline
        if (bridgeHandler.getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        try {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            Map<String, Object> properties;
            String unitId;
            String unitName;
            onectaConnectionClient.refreshUnitsData(bridgeHandler.getThing());
            List<Unit> units = onectaConnectionClient.getUnits().getAll();
            for (int i = 0; i < units.size(); i++) {
                unitId = units.get(i).getId().toString();
                unitName = units.get(i).findManagementPointsByType(CLIMATECONTROL).getNameValue();
                unitName = !unitName.isEmpty() ? unitName : unitId;
                properties = new LinkedHashMap<>();
                properties.put("unitID", unitId);

                ThingUID thingUID = new ThingUID(DEVICE_THING_TYPE, bridgeUID, unitId);
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withBridge(bridgeHandler.getThing().getUID())
                        .withLabel(String.format("Daikin Onecta Unit ClimateControl (%s)", unitName)).build();

                thingDiscovered(discoveryResult);
                logger.info("Discovered a onecta unit thing with ID '{}' '{}'", unitId, unitName);
                bridgeHandler.getThing().setProperty(PROPERTY_GW_DISCOVERED + " Unit (" + unitName + ")", unitId);

                if (units.get(i).findManagementPointsByType(GATEWAY) != null) {
                    thingUID = new ThingUID(GATEWAY_THING_TYPE, bridgeUID, unitId);
                    discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeHandler.getThing().getUID())
                            .withLabel(String.format("Daikin Onecta (%s) (%s)", GATEWAY, unitName)).build();

                    thingDiscovered(discoveryResult);
                    logger.info("Discovered a onecta gateway thing with ID '{}' '{}'", unitId, unitName);
                    bridgeHandler.getThing().setProperty(PROPERTY_GW_DISCOVERED + " Gateway (" + unitName + ")",
                            unitId);

                }
                if (units.get(i).findManagementPointsByType("domesticHotWaterTank") != null) {
                    thingUID = new ThingUID(WATERTANK_THING_TYPE, bridgeUID, unitId);
                    discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeHandler.getThing().getUID())
                            .withLabel(String.format("Daikin Onecta (%s) (%s)", WATERTANK, unitName)).build();

                    thingDiscovered(discoveryResult);
                    logger.info("Discovered a onecta watertank thing with ID '{}' '{}'", unitId, unitName);
                    bridgeHandler.getThing().setProperty(PROPERTY_GW_DISCOVERED + " Hotwatertank (" + unitName + ")",
                            unitId);

                }
                if (units.get(i).findManagementPointsByType(INDOORUNIT) != null) {
                    thingUID = new ThingUID(INDOORUNIT_THING_TYPE, bridgeUID, unitId);
                    discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeHandler.getThing().getUID())
                            .withLabel(String.format("Daikin Onecta (%s) (%s)", INDOORUNIT, unitName)).build();

                    thingDiscovered(discoveryResult);
                    logger.info("Discovered a onecta indoorunit thing with ID '{}' '{}'", unitId, unitName);
                    bridgeHandler.getThing().setProperty(PROPERTY_GW_DISCOVERED + " Indoorunit (" + unitName + ")",
                            unitId);

                }
            }
        } catch (Exception e) {
            logger.error("Error in DiscoveryService", e);
        }
    }
}
