/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.handlers;

import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EntityBaseHandler} abstract handler class that contains common logic for entities.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public abstract class EntityBaseHandler extends BaseThingHandler {

    private static final long INITIAL_DELAY_SECONDS = 20;

    private final Logger logger = LoggerFactory.getLogger(EntityBaseHandler.class);

    protected EntityConfiguration config;

    public EntityBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing. {}", thing.getLabel());
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(EntityConfiguration.class);

        scheduler.schedule(this::initializeDelayed, INITIAL_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void initializeDelayed() {
        logger.trace("Start initializeDelayed() in {}", getThing().getUID());
        ParadoxPanel panel = ParadoxPanel.getInstance();
        if (!panel.isPanelSupported()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Panel " + panel.getPanelInformation().getPanelType().name() + " is not supported.");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            if (ThingStatus.ONLINE == getThing().getStatus()) {
                updateEntity();
            } else {
                logger.debug("Received REFRESH command but {} has the following detailed status {}",
                        getThing().getUID(), getThing().getStatusInfo());
            }
        }
    }

    protected abstract void updateEntity();

    protected int calculateEntityIndex() {
        return Math.max(0, config.getId() - 1);
    }
}
