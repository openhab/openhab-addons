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
package org.openhab.io.imperihome.internal.action;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.openhab.io.imperihome.internal.model.device.DeviceType;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;
import org.openhab.io.imperihome.internal.processor.ItemProcessor;

/**
 * Action to stop a shutter. Actually just sends an ON command to a linked switch device, to be handled by rules.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class StopShutterAction extends Action {

    private final DeviceRegistry deviceRegistry;

    public StopShutterAction(EventPublisher eventPublisher, DeviceRegistry deviceRegistry) {
        super(eventPublisher);
        this.deviceRegistry = deviceRegistry;
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        String stopper = device.getLinks().get("stopper");
        return device.getType() == DeviceType.SHUTTER && stopper != null && !stopper.isBlank();
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        String modeDeviceName = device.getLinks().get("stopper");
        AbstractDevice modeDevice = deviceRegistry.getDevice(ItemProcessor.getDeviceId(modeDeviceName));
        if (modeDevice == null) {
            logger.error("Couldn't resolve linked Stopper device '{}', make sure the Item has iss tags",
                    modeDeviceName);
            return;
        }

        Item modeItem = modeDevice.getItem();

        ItemCommandEvent event = ItemEventFactory.createCommandEvent(modeItem.getName(), OnOffType.ON, COMMAND_SOURCE);
        eventPublisher.post(event);
    }
}
