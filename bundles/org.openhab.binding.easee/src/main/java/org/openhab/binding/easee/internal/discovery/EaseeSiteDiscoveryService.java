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
package org.openhab.binding.easee.internal.discovery;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easee.internal.EaseeBindingConstants;
import org.openhab.binding.easee.internal.UtilsTrait;
import org.openhab.binding.easee.internal.command.site.GetSite;
import org.openhab.binding.easee.internal.connector.CommunicationStatus;
import org.openhab.binding.easee.internal.handler.EaseeSiteHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * this class will handle discovery of wallboxes and circuits within the site configured.
 *
 * @author Alexander Friese - initial contribution
 *
 */
@NonNullByDefault
public class EaseeSiteDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService, UtilsTrait {

    private final Logger logger = LoggerFactory.getLogger(EaseeSiteDiscoveryService.class);
    private @NonNullByDefault({}) EaseeSiteHandler bridgeHandler;

    public EaseeSiteDiscoveryService() throws IllegalArgumentException {
        super(EaseeBindingConstants.SUPPORTED_THING_TYPES_UIDS, 300, false);
    }

    @Override
    protected void startScan() {
        bridgeHandler.enqueueCommand(new GetSite(bridgeHandler, this::processSiteDiscoveryResult));
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof EaseeSiteHandler) {
            this.bridgeHandler = (EaseeSiteHandler) handler;
            this.bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    // method is defined in both implemented interface and inherited class, thus we must define a behaviour here.
    @Override
    public void deactivate() {
        super.deactivate();
    }

    /**
     * callback that handles json result data to provide discovery result.
     *
     * @param site
     */
    private void processSiteDiscoveryResult(CommunicationStatus status, JsonObject site) {
        logger.debug("processDiscoveryResult {}", site);

        JsonArray circuits = site.getAsJsonArray(JSON_KEY_CIRCUITS);
        if (circuits == null) {
            logger.info("Site discovery failed, no circuits found.");
        } else {
            circuits.forEach(this::handleCircuitDiscovery);
        }
    }

    /**
     * handles each circuit discovery result.
     *
     * @param circuit
     */
    private void handleCircuitDiscovery(JsonElement json) {
        logger.debug("handleCircuitDiscovery {}", json);

        JsonObject circuit = json.getAsJsonObject();
        String circuitId = getAsString(circuit, JSON_KEY_GENERIC_ID);
        String circuitName = getAsString(circuit, JSON_KEY_CIRCUIT_NAME);

        if (circuitId != null) {
            DiscoveryResultBuilder builder = initDiscoveryResultBuilder(DEVICE_CIRCUIT, circuitId, circuitName);
            thingDiscovered(builder.build());

            // handle contained chargers
            JsonArray chargers = circuit.getAsJsonArray(JSON_KEY_CHARGERS);
            if (chargers == null) {
                logger.info("Site discovery failed, no chargers found.");
            } else {
                chargers.forEach(this::handleChargerDiscovery);
            }
        }
    }

    /**
     * handles each charger discovery result.
     *
     * @param charger
     */
    private void handleChargerDiscovery(JsonElement json) {
        logger.debug("handleChargerDiscovery {}", json);

        JsonObject charger = json.getAsJsonObject();
        String chargerId = getAsString(charger, JSON_KEY_GENERIC_ID);
        String backPlateId = getAsString(charger.getAsJsonObject(JSON_KEY_BACKPLATE), JSON_KEY_GENERIC_ID);
        String chargerName = getAsString(charger, JSON_KEY_GENERIC_NAME);

        if (chargerId != null && backPlateId != null) {
            DiscoveryResultBuilder builder = initDiscoveryResultBuilder(DEVICE_CHARGER, chargerId, chargerName)
                    .withProperty(Thing.PROPERTY_SERIAL_NUMBER, backPlateId);
            thingDiscovered(builder.build());
        }
    }

    /**
     * sends discovery notification to the framework.
     *
     * @param deviceType
     * @param deviceId
     * @param deviceName
     */
    private DiscoveryResultBuilder initDiscoveryResultBuilder(String deviceType, String deviceId,
            @Nullable String deviceName) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID typeUid = new ThingTypeUID(BINDING_ID, deviceType);

        ThingUID thingUID = new ThingUID(typeUid, bridgeUID, deviceId);
        String label = deviceName != null ? deviceName : deviceId;

        return DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                .withProperty(Thing.PROPERTY_SERIAL_NUMBER, deviceId).withProperty(THING_CONFIG_IDENTIFIER, deviceId)
                .withRepresentationProperty(THING_CONFIG_IDENTIFIER);
    }
}
