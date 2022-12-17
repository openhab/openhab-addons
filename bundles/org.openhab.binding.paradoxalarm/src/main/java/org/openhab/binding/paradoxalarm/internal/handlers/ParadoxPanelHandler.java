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
package org.openhab.binding.paradoxalarm.internal.handlers;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import javax.measure.quantity.ElectricPotential;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxInformation;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxPanelHandler} This is the handler that takes care of the panel related stuff.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class ParadoxPanelHandler extends EntityBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(ParadoxPanelHandler.class);

    public ParadoxPanelHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        config = getConfigAs(PanelConfiguration.class);
    }

    @Override
    protected void updateEntity() {
        ParadoxIP150BridgeHandler bridge = (ParadoxIP150BridgeHandler) getBridge().getHandler();
        ParadoxPanel panel = bridge.getPanel();
        StringType panelState = panel.isOnline() ? STATE_ONLINE : STATE_OFFLINE;
        updateState(PANEL_STATE_CHANNEL_UID, panelState);
        ParadoxInformation panelInformation = panel.getPanelInformation();
        if (panelInformation != null) {
            updateProperty(PANEL_SERIAL_NUMBER_PROPERTY_NAME, panelInformation.getSerialNumber());
            updateProperty(PANEL_TYPE_PROPERTY_NAME, panelInformation.getPanelType().name());
            updateProperty(PANEL_HARDWARE_VERSION_PROPERTY_NAME, panelInformation.getHardwareVersion().toString());
            updateProperty(PANEL_APPLICATION_VERSION_PROPERTY_NAME,
                    panelInformation.getApplicationVersion().toString());
            updateProperty(PANEL_BOOTLOADER_VERSION_PROPERTY_NAME, panelInformation.getBootLoaderVersion().toString());

            updateState(PANEL_TIME, new DateTimeType(panel.getPanelTime()));
            updateState(PANEL_INPUT_VOLTAGE, new QuantityType<ElectricPotential>(panel.getVdcLevel(), Units.VOLT));
            updateState(PANEL_BOARD_VOLTAGE, new QuantityType<ElectricPotential>(panel.getDcLevel(), Units.VOLT));
            updateState(PANEL_BATTERY_VOLTAGE,
                    new QuantityType<ElectricPotential>(panel.getBatteryLevel(), Units.VOLT));
        }
    }
}
