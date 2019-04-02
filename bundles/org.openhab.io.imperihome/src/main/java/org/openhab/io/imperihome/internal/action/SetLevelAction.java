/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
