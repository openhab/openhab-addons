/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.action;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;

/**
 * Action setting a choice from a selection list, e.g. MultiSwitch.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SetChoiceAction extends Action {

    public SetChoiceAction(EventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        Map<String, String> mapping = device.getMapping();
        return mapping != null && !mapping.isEmpty() && item.getAcceptedCommandTypes().contains(DecimalType.class);
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        String newValue = null;

        for (Entry<String, String> entry : device.getMapping().entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                newValue = entry.getKey();
                break;
            }
        }

        if (newValue == null) {
            logger.warn("Could not find selection '{}' in mapping {} of device {}", value, device.getMapping(), device);
            return;
        }

        ItemCommandEvent event = ItemEventFactory.createCommandEvent(item.getName(), new DecimalType(newValue),
                COMMAND_SOURCE);
        eventPublisher.post(event);
    }

}
