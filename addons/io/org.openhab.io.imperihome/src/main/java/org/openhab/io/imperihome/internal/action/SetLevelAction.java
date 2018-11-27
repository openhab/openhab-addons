/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.action;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemCommandEvent;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;

/**
 * Action setting percentage level, e.g. dimmer.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SetLevelAction extends Action {

    public SetLevelAction(EventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        return item.getAcceptedCommandTypes().contains(PercentType.class);
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        ItemCommandEvent event = ItemEventFactory.createCommandEvent(item.getName(), new PercentType(value),
                COMMAND_SOURCE);
        eventPublisher.post(event);
    }

}
