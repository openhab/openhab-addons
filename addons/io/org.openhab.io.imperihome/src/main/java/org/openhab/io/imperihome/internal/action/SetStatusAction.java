/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.action;

import java.util.Objects;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;

/**
 * Action setting device status to 1 or 0.
 * @author Pepijn de Geus - Initial contribution
 */
public class SetStatusAction extends Action {

    public SetStatusAction(EventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        return item.getAcceptedCommandTypes().contains(OnOffType.class);
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        OnOffType cmdValue = OnOffType.OFF;
        if (Objects.equals("1", value)) {
            cmdValue = OnOffType.ON;
        }

        ItemCommandEvent event = ItemEventFactory.createCommandEvent(item.getName(), cmdValue, COMMAND_SOURCE);
        eventPublisher.post(event);
    }

}
