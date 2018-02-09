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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;

/**
 * Action performed on a DevScene. Sends an {@link OnOffType#ON} or {@link DecimalType} 1 to the Item.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class LaunchSceneAction extends Action {

    public LaunchSceneAction(EventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        List<Class<? extends Command>> acceptedCommandTypes = item.getAcceptedCommandTypes();
        return acceptedCommandTypes.contains(OnOffType.class) || acceptedCommandTypes.contains(DecimalType.class);
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        ItemCommandEvent event = null;

        List<Class<? extends Command>> acceptedCommandTypes = item.getAcceptedCommandTypes();
        if (acceptedCommandTypes.contains(OnOffType.class)) {
            event = ItemEventFactory.createCommandEvent(item.getName(), OnOffType.ON, COMMAND_SOURCE);
        } else if (acceptedCommandTypes.contains(DecimalType.class)) {
            event = ItemEventFactory.createCommandEvent(item.getName(), new DecimalType(1), COMMAND_SOURCE);
        }

        if (event != null) {
            eventPublisher.post(event);
        }
    }

}
