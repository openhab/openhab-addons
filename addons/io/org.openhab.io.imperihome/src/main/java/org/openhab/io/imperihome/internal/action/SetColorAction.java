/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.eclipse.smarthome.core.library.types.HSBType;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Items setting RGB color value.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class SetColorAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(SetColorAction.class);

    public SetColorAction(EventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @Override
    public boolean supports(AbstractDevice device, Item item) {
        return item.getAcceptedCommandTypes().contains(HSBType.class);
    }

    @Override
    public void perform(AbstractDevice device, Item item, String value) {
        if (value == null || value.length() != 8) {
            logger.error("Invalid parameter: '{}'. Format must be 'aarrggbb'.", value);
            return;
        }

        int r = Integer.parseInt(value.substring(2, 4), 16);
        int g = Integer.parseInt(value.substring(4, 6), 16);
        int b = Integer.parseInt(value.substring(6, 8), 16);

        HSBType hsbValue = HSBType.fromRGB(r, g, b);

        ItemCommandEvent event = ItemEventFactory.createCommandEvent(item.getName(), hsbValue, COMMAND_SOURCE);
        eventPublisher.post(event);
    }

}
