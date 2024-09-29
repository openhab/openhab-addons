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
package org.openhab.binding.easee.internal.handler;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easee.internal.EaseeBindingConstants;
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.command.circuit.CircuitSettings;
import org.openhab.binding.easee.internal.command.circuit.DynamicCircuitCurrent;
import org.openhab.binding.easee.internal.command.circuit.SetCircuitSettings;
import org.openhab.binding.easee.internal.command.circuit.SetDynamicCircuitCurrents;
import org.openhab.binding.easee.internal.command.circuit.SetMaxCircuitCurrents;
import org.openhab.binding.easee.internal.command.circuit.SetOfflineMaxCircuitCurrents;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EaseeMasterChargerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class EaseeMasterChargerHandler extends EaseeChargerHandler {
    private final Logger logger = LoggerFactory.getLogger(EaseeMasterChargerHandler.class);

    public EaseeMasterChargerHandler(Thing thing) {
        super(thing);
    }

    /**
     * Poll the Easee Cloud API one time.
     */
    @Override
    void pollingRun() {
        super.pollingRun();

        // proceed if charger is online, otherwise circuit data is not in sync.
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            String circuitId = getConfig().get(EaseeBindingConstants.THING_CONFIG_CIRCUIT_ID).toString();
            logger.debug("polling circuit data for {}", circuitId);

            enqueueCommand(new DynamicCircuitCurrent(this, circuitId, this::updateOnlineStatus));
            enqueueCommand(new CircuitSettings(this, circuitId, this::updateOnlineStatus));
        }
    }

    @Override
    public EaseeCommand buildEaseeCommand(Command command, Channel channel) {
        String circuitId = getConfig().get(EaseeBindingConstants.THING_CONFIG_CIRCUIT_ID).toString();

        switch (Utils.getWriteCommand(channel)) {
            case COMMAND_SET_CIRCUIT_SETTINGS:
                return new SetCircuitSettings(this, channel, command, circuitId, this::updateOnlineStatus);
            case COMMAND_SET_DYNAMIC_CIRCUIT_CURRENTS:
                return new SetDynamicCircuitCurrents(this, channel, command, circuitId, this::updateOnlineStatus);
            case COMMAND_SET_MAX_CIRCUIT_CURRENTS:
                return new SetMaxCircuitCurrents(this, channel, command, circuitId, this::updateOnlineStatus);
            case COMMAND_SET_OFFLINE_MAX_CIRCUIT_CURRENTS:
                return new SetOfflineMaxCircuitCurrents(this, channel, command, circuitId, this::updateOnlineStatus);
            default:
                return super.buildEaseeCommand(command, channel);
        }
    }
}
