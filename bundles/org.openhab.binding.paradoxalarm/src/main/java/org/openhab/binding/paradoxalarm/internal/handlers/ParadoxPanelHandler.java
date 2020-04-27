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

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxInformation;
import org.openhab.binding.paradoxalarm.internal.model.ParadoxPanel;
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
    protected void updateEntity() {
        ParadoxPanel panel = ParadoxPanel.getInstance();
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
        }
    }
}
