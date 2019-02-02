/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal.discovery;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.CONFIG_PANEL_ID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants;
import org.openhab.binding.nanoleaf.internal.NanoleafControllerListener;
import org.openhab.binding.nanoleaf.internal.NanoleafHandlerFactory;
import org.openhab.binding.nanoleaf.internal.config.NanoleafControllerConfig;
import org.openhab.binding.nanoleaf.internal.handler.NanoleafControllerHandler;
import org.openhab.binding.nanoleaf.internal.model.ControllerInfo;
import org.openhab.binding.nanoleaf.internal.model.PositionDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NanoleafPanelsDiscoveryService} is responsible for discovering the individual
 * panels connected to the controller.
 *
 * @author Martin Raepple - Initial contribution
 */
public class NanoleafPanelsDiscoveryService extends AbstractDiscoveryService implements NanoleafControllerListener {
    private final Logger logger = LoggerFactory.getLogger(NanoleafPanelsDiscoveryService.class);
    private static final int SEARCH_TIMEOUT = 10;

    private NanoleafControllerHandler bridgeHandler;
    private boolean discoverPanels;

    /**
     * Creates a NanoleafPanelsDiscoveryService with background discovery disabled.
     */
    public NanoleafPanelsDiscoveryService(NanoleafControllerHandler nanoleafControllerHandler) {
        super(NanoleafHandlerFactory.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIMEOUT, false);
        this.bridgeHandler = nanoleafControllerHandler;
    }

    /**
     * Applies the configuration from the controller configuration to enable/disable panel discovery
     */
    @Override
    public void applyConfig(Map<String, Object> configProperties) {
        if (configProperties != null) {
            this.discoverPanels = (boolean) configProperties.get(NanoleafControllerConfig.DISCOVER_PANELS);
            logger.debug("Panel discovery is {}", this.discoverPanels ? "enabled" : "disabled");
        }
    }

    /**
     * Called by the controller handler with bridge and panel data
     *
     * @param bridge         The controller
     * @param controllerInfo Panel data (and more)
     */
    @Override
    public void onControllerInfoFetched(ThingUID bridge, ControllerInfo controllerInfo) {
        if (discoverPanels) {
            logger.debug("Start adding panels connected to controller with id {} to inbox", bridge.getAsString());
            if (controllerInfo != null && controllerInfo.getPanelLayout() != null) {
                if (controllerInfo.getPanelLayout().getLayout().getNumPanels() > 0) {
                    Iterator<PositionDatum> iterator = controllerInfo.getPanelLayout().getLayout().getPositionData()
                            .iterator();
                    while (iterator.hasNext()) {
                        PositionDatum panel = iterator.next();
                        panel.getPanelId();
                        ThingUID newPanelThingUID = new ThingUID(NanoleafBindingConstants.THING_TYPE_LIGHT_PANEL,
                                bridge, panel.getPanelId().toString());

                        final Map<String, Object> properties = new HashMap<>(1);
                        properties.put(CONFIG_PANEL_ID, panel.getPanelId());

                        DiscoveryResult newPanel = DiscoveryResultBuilder.create(newPanelThingUID).withBridge(bridge)
                                .withProperties(properties).withLabel("Nanoleaf Light Panel").build();

                        logger.debug("Adding panel with id {} to inbox", panel.getPanelId());
                        thingDiscovered(newPanel);
                    }
                }
            } else {
                logger.info("Panel discovery enabled, but no panels found or connected to controller");
            }
        } else {
            logger.debug("Discovery of panels disabled in controller configuration");
        }
    }

    @Override
    public void activate(@Nullable Map<@NonNull String, @Nullable Object> configProperties) {
        super.activate(configProperties);
        applyConfig(configProperties);
        bridgeHandler.registerControllerListener(this);
        logger.debug("Panel discovery service activated");
    }

    @Override
    public void deactivate() {
        bridgeHandler.unregisterControllerListener(this);
        super.deactivate();
        logger.debug("Panel discovery service deactivated");
    }

    @Override
    protected void startScan() {
        // nothing to do
    }
}
