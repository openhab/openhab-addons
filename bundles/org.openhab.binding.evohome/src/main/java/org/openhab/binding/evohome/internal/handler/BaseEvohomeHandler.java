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
package org.openhab.binding.evohome.internal.handler;

import org.openhab.binding.evohome.internal.api.models.v2.response.Locations;
import org.openhab.binding.evohome.internal.configuration.EvohomeThingConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * Base class for an evohome handler
 *
 * @author Jasper van Zuijlen - Initial contribution
 */
public abstract class BaseEvohomeHandler extends BaseThingHandler {
    private EvohomeThingConfiguration configuration;

    public BaseEvohomeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(EvohomeThingConfiguration.class);
        checkConfig();
    }

    @Override
    public void dispose() {
        configuration = null;
    }

    public String getId() {
        if (configuration != null) {
            return configuration.id;
        }
        return null;
    }

    /**
     * Returns the configuration of the Thing
     *
     * @return The parsed configuration or null
     */
    protected EvohomeThingConfiguration getEvohomeThingConfig() {
        return configuration;
    }

    /**
     * Retrieves the bridge
     *
     * @return The evohome brdige
     */
    protected EvohomeAccountBridgeHandler getEvohomeBridge() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (EvohomeAccountBridgeHandler) bridge.getHandler();
        }

        return null;
    }

    /**
     * Retrieves the evohome configuration from the bridge
     *
     * @return The current evohome configuration
     */
    protected Locations getEvohomeConfig() {
        EvohomeAccountBridgeHandler bridge = getEvohomeBridge();
        if (bridge != null) {
            return bridge.getEvohomeConfig();
        }

        return null;
    }

    /**
     * Retrieves the evohome configuration from the bridge
     *
     * @return The current evohome configuration
     */
    protected void requestUpdate() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ((EvohomeAccountBridgeHandler) bridge).getEvohomeConfig();
        }
    }

    /**
     * Updates the status of the evohome thing when it changes
     *
     * @param newStatus The new status to update to
     */
    protected void updateEvohomeThingStatus(ThingStatus newStatus) {
        updateEvohomeThingStatus(newStatus, ThingStatusDetail.NONE, null);
    }

    /**
     * Updates the status of the evohome thing when it changes
     *
     * @param newStatus The new status to update to
     * @param detail The status detail value
     * @param message The message to show with the status
     */
    protected void updateEvohomeThingStatus(ThingStatus newStatus, ThingStatusDetail detail, String message) {
        // Prevent spamming the log file
        if (!newStatus.equals(getThing().getStatus())) {
            updateStatus(newStatus, detail, message);
        }
    }

    /**
     * Checks the configuration for validity, result is reflected in the status of the Thing
     *
     * @param configuration The configuration to check
     */
    private void checkConfig() {
        if (configuration == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configuration is missing or corrupted");
        } else if (configuration.id == null || configuration.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Id not configured");
        }
    }
}
