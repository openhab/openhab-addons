/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.action;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.StringType;
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
        return StringUtils.isNotBlank(device.getLinks().get("curmode"));
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        String modeDeviceName = device.getLinks().get("curmode");
        AbstractDevice modeDevice = deviceRegistry.getDevice(ItemProcessor.getDeviceId(modeDeviceName));
        if (modeDevice == null) {
            logger.error("Couldn't resolve linked CurMode device '{}', make sure the Item has iss tags", modeDeviceName);
            return;
        }

        Item modeItem = modeDevice.getItem();

        ItemCommandEvent event = ItemEventFactory.createCommandEvent(modeItem.getName(), new StringType(value), COMMAND_SOURCE);
        eventPublisher.post(event);
    }

}
