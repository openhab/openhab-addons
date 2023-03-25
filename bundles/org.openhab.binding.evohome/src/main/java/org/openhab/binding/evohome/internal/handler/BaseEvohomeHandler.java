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
package org.openhab.binding.evohome.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.Locations;
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
@NonNullByDefault
public abstract class BaseEvohomeHandler extends BaseThingHandler {
    private EvohomeThingConfiguration configuration = new EvohomeThingConfiguration();

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
    }

    public String getId() {
        return configuration.id;
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
    protected @Nullable EvohomeAccountBridgeHandler getEvohomeBridge() {
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
    protected @Nullable Locations getEvohomeConfig() {
        EvohomeAccountBridgeHandler bridgeAccountHandler = getEvohomeBridge();
        if (bridgeAccountHandler != null) {
            return bridgeAccountHandler.getEvohomeConfig();
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
    protected void updateEvohomeThingStatus(ThingStatus newStatus, ThingStatusDetail detail, @Nullable String message) {
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
        if (configuration.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Id not configured");
        }
    }
}
