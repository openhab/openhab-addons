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
package org.openhab.io.imperihome.internal.action;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.StringType;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.openhab.io.imperihome.internal.model.device.DeviceType;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;
import org.openhab.io.imperihome.internal.processor.ItemProcessor;

/**
 * Action setting a thermostat mode.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SetModeAction extends Action {

    private final DeviceRegistry deviceRegistry;

    public SetModeAction(EventPublisher eventPublisher, DeviceRegistry deviceRegistry) {
        super(eventPublisher);
        this.deviceRegistry = deviceRegistry;
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        if (device.getType() != DeviceType.THERMOSTAT) {
            return false;
        }
        String curmode = device.getLinks().get("curmode");
        return curmode != null && !curmode.isBlank();
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        String modeDeviceName = device.getLinks().get("curmode");
        AbstractDevice modeDevice = deviceRegistry.getDevice(ItemProcessor.getDeviceId(modeDeviceName));
        if (modeDevice == null) {
            logger.error("Couldn't resolve linked CurMode device '{}', make sure the Item has iss tags",
                    modeDeviceName);
            return;
        }

        Item modeItem = modeDevice.getItem();

        ItemCommandEvent event = ItemEventFactory.createCommandEvent(modeItem.getName(), new StringType(value),
                COMMAND_SOURCE);
        eventPublisher.post(event);
    }
}
