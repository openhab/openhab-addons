/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCChannelRegistry;
import org.openhab.binding.e3dc.internal.E3DCChannelSpec;
import org.openhab.binding.e3dc.internal.E3DCCommandRegistry;
import org.openhab.binding.e3dc.internal.E3DCCommandSpec;
import org.openhab.binding.e3dc.internal.config.E3DCWallboxConfiguration;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * One wallbox attached to the power plant.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCWallboxHandler extends E3DCThingHandler {

    static final String CHANNEL_TOGGLE = "toggle";
    private static final String CHANNEL_SUSPENDED = "suspended";
    private static final E3DCChannelSpec SUSPENDED = E3DCChannelSpec.in(RscpTag.WB_DATA, RscpTag.WB_ABORT_CHARGING);

    /** Whether the charging was suspended at the last poll, or {@code null} before the first answer. */
    private volatile @Nullable Boolean suspended;

    private final E3DCChannelRegistry registry = E3DCChannelSpec.wallbox();
    private final E3DCCommandRegistry commandRegistry = E3DCCommandSpec.wallbox();
    private final E3DCWallboxConfiguration config;

    public E3DCWallboxHandler(Thing thing) {
        super(thing);
        this.config = thing.getConfiguration().as(E3DCWallboxConfiguration.class);
    }

    @Override
    protected E3DCChannelRegistry getRegistry() {
        return registry;
    }

    @Override
    protected @Nullable E3DCCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    protected @Nullable Integer getDeviceIndex() {
        return config.wallboxIndex;
    }

    @Override
    public void onData(RscpValue response) {
        super.onData(response);
        RscpValue value = findValue(response, SUSPENDED, getDeviceIndex());
        if (value != null && value.getValue() instanceof Number number) {
            suspended = number.intValue() != 0;
        } else if (value != null && value.getValue() instanceof Boolean flag) {
            suspended = flag;
        }
    }

    /**
     * Handles the toggle of the charging, which suspends a running session and resumes a suspended one.
     *
     * <p>
     * The toggle is the opposite of the last reported suspended state, written as that state, so it works
     * like the toggle of rscp2mqtt without a second way to write to the wallbox.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!CHANNEL_TOGGLE.equals(channelUID.getIdWithoutGroup())) {
            super.handleCommand(channelUID, command);
            return;
        }
        if (command instanceof RefreshType || !OnOffType.ON.equals(command)) {
            return;
        }
        Boolean current = suspended;
        if (current == null) {
            logger.warn("The charging was not toggled because the suspended state is not known yet");
        } else {
            ChannelUID suspendedChannel = new ChannelUID(getThing().getUID(),
                    E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL, CHANNEL_SUSPENDED);
            super.handleCommand(suspendedChannel, OnOffType.from(!current));
        }
        // the toggle is a button, so it goes back to off whatever happened
        updateState(channelUID, OnOffType.OFF);
    }
}
