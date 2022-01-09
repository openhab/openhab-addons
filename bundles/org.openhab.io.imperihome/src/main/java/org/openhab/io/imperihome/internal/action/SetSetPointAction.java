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

import java.util.List;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
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
