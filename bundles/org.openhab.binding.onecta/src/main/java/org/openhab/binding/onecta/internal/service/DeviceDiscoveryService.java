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
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
public class DeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(OnectaBridgeHandler.class);
    @Nullable
    private OnectaBridgeHandler bridgeHandler = null;
    private final OnectaConnectionClient onectaConnectionClient = new OnectaConnectionClient();

    public DeviceDiscoveryService(OnectaBridgeHandler bridgeHandler) throws IllegalArgumentException {
        super(20);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void startScan() throws IllegalArgumentException {

        if (bridgeHandler == null) {
            return;
        }
        // Trigger no scan if offline
        if (bridgeHandler.getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        try {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            onectaConnectionClient.refreshUnitsData(bridgeHandler.getThing());
            List<Unit> units = onectaConnectionClient.getUnits().getAll();
            for (Unit unit : units) {
                thingDiscover(unit, CLIMATECONTROL, DEVICE_THING_TYPE);
                thingDiscover(unit, GATEWAY, GATEWAY_THING_TYPE);
                thingDiscover(unit, WATERTANK, WATERTANK_THING_TYPE);
                thingDiscover(unit, INDOORUNIT, INDOORUNIT_THING_TYPE);
            }
        } catch (Exception e) {
            logger.error("Error in DiscoveryService", e);
        }
    }

    protected void thingDiscover(Unit unit, String unitSourceType, ThingTypeUID thingTypeUID) {

        if (unit.findManagementPointsByType(unitSourceType) != null) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            String unitId = unit.getId().toString();
            String unitName = unit.findManagementPointsByType(CLIMATECONTROL).getNameValue();
            unitName = !unitName.isEmpty() ? unitName : unitId;
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("unitID", unitId);

            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, unitId);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridgeHandler.getThing().getUID())
                    .withLabel(String.format("Daikin Onecta (%s) (%s)", unitSourceType, unitName)).build();

            thingDiscovered(discoveryResult);
            logger.info("Discovered a onecta {} thing with ID '{}' '{}'", unitSourceType, unitId, unitName);
            bridgeHandler.getThing().setProperty(PROPERTY_GW_DISCOVERED + " " + unitSourceType + " (" + unitName + ")",
                    unitId);
        }
    }

    @Override
    protected void thingDiscovered(DiscoveryResult discoveryResult) {
        super.thingDiscovered(discoveryResult);
    }
}
