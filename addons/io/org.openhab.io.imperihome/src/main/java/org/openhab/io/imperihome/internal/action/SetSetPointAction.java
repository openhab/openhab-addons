/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.action;

import java.util.List;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.openhab.io.imperihome.internal.model.device.DeviceType;

/**
 * Action setting a thermostat setpoint.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SetSetPointAction extends Action {

    public SetSetPointAction(EventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        if (device.getType() != DeviceType.THERMOSTAT) {
            return false;
        }
        List<Class<? extends Command>> acceptedCommandTypes = item.getAcceptedCommandTypes();
        return acceptedCommandTypes.contains(DecimalType.class) || acceptedCommandTypes.contains(StringType.class);
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        List<Class<? extends Command>> acceptedCommandTypes = item.getAcceptedCommandTypes();

        Command command;
        if (acceptedCommandTypes.contains(DecimalType.class)) {
            command = new DecimalType(value);
        } else if (acceptedCommandTypes.contains(StringType.class)) {
            command = new StringType(value);
        } else {
            logger.error("Item {} doesn't support Decimal or String type", item);
            return;
        }

        ItemCommandEvent event = ItemEventFactory.createCommandEvent(item.getName(), command, COMMAND_SOURCE);
        eventPublisher.post(event);
    }

}
