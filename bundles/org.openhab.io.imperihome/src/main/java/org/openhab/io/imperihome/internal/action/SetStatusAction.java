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

import java.util.Objects;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.io.imperihome.internal.model.device.AbstractDevice;

/**
 * Action setting device status to 1 or 0.
 * 
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
