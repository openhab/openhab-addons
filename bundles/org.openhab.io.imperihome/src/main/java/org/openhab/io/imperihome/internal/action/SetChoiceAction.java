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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DecimalType;
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
