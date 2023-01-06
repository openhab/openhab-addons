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
package org.openhab.binding.boschshc.internal.devices.smartbulb;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCDeviceHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.binaryswitch.BinarySwitchService;
import org.openhab.binding.boschshc.internal.services.binaryswitch.dto.BinarySwitchServiceState;
import org.openhab.binding.boschshc.internal.services.hsbcoloractuator.HSBColorActuatorService;
import org.openhab.binding.boschshc.internal.services.hsbcoloractuator.dto.HSBColorActuatorServiceState;
import org.openhab.binding.boschshc.internal.services.multilevelswitch.MultiLevelSwitchService;
import org.openhab.binding.boschshc.internal.services.multilevelswitch.dto.MultiLevelSwitchServiceState;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for smart light bulbs connected via Zigbee, e.g. Ledvance Smart+ bulbs
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class SmartBulbHandler extends BoschSHCDeviceHandler {

    private BinarySwitchService binarySwitchService;
    private HSBColorActuatorService hsbColorActuatorService;
    private MultiLevelSwitchService multiLevelSwitchService;

    public SmartBulbHandler(Thing thing) {
        super(thing);

        this.binarySwitchService = new BinarySwitchService();
        this.multiLevelSwitchService = new MultiLevelSwitchService();
        this.hsbColorActuatorService = new HSBColorActuatorService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(binarySwitchService, this::updateChannels, List.of(CHANNEL_POWER_SWITCH), true);
        this.registerService(multiLevelSwitchService, this::updateChannels, List.of(CHANNEL_BRIGHTNESS), true);
        this.registerService(hsbColorActuatorService, this::updateChannels, List.of(CHANNEL_COLOR), true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_POWER_SWITCH:
                if (command instanceof OnOffType) {
                    updateBinarySwitchState((OnOffType) command);
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType) {
                    updateMultiLevelSwitchState((PercentType) command);
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    updateColorState((HSBType) command);
                }
                break;
        }
    }

    private void updateBinarySwitchState(OnOffType command) {
        BinarySwitchServiceState serviceState = new BinarySwitchServiceState();
        serviceState.on = command == OnOffType.ON;
        this.updateServiceState(binarySwitchService, serviceState);
    }

    private void updateMultiLevelSwitchState(PercentType command) {
        MultiLevelSwitchServiceState serviceState = new MultiLevelSwitchServiceState();
        serviceState.level = command.intValue();
        this.updateServiceState(multiLevelSwitchService, serviceState);
    }

    private void updateColorState(HSBType command) {
        HSBColorActuatorServiceState serviceState = new HSBColorActuatorServiceState();
        serviceState.rgb = command.getRGB();
        this.updateServiceState(hsbColorActuatorService, serviceState);
    }

    private void updateChannels(BinarySwitchServiceState serviceState) {
        super.updateState(CHANNEL_POWER_SWITCH, serviceState.toOnOffType());
    }

    private void updateChannels(MultiLevelSwitchServiceState serviceState) {
        super.updateState(CHANNEL_BRIGHTNESS, serviceState.toPercentType());
    }

    private void updateChannels(HSBColorActuatorServiceState serviceState) {
        super.updateState(CHANNEL_COLOR, serviceState.toHSBType());
    }
}
