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
package org.openhab.binding.paradoxalarm.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.paradoxalarm.internal.communication.IRequest;
import org.openhab.binding.paradoxalarm.internal.communication.messages.zone.ZoneCommand;
import org.openhab.binding.paradoxalarm.internal.handlers.Commandable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Zone} Paradox zone.
 * ID is always numeric (1-192 for Evo192)
 * States are taken from cached RAM memory map and parsed.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class Zone extends Entity implements Commandable {

    private final Logger logger = LoggerFactory.getLogger(Zone.class);

    private @Nullable ZoneState zoneState;

    public Zone(ParadoxPanel panel, int id, @Nullable String label) {
        super(panel, id, label);
    }

    public @Nullable ZoneState getZoneState() {
        return zoneState;
    }

    public void setZoneState(ZoneState zoneState) {
        this.zoneState = zoneState;
        logger.debug("Zone {} state updated to: {}", getLabel(), zoneState);
    }

    @Override
    public void handleCommand(@Nullable String command) {
        ZoneCommand zoneCommand = ZoneCommand.parse(command);
        if (zoneCommand == null) {
            logger.debug("Command {} is parsed to null. Skipping it", command);
            return;
        }

        logger.debug("Submitting command={} for partition=[{}]", zoneCommand, this);
        IRequest request = zoneCommand.getRequest(getId());
        getPanel().getCommunicator().submitRequest(request);
    }
}
