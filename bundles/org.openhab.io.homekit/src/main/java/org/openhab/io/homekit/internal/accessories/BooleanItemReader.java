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
package org.openhab.io.homekit.internal.accessories;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps either a SwitchItem or a ContactItem, interpretting the open / closed states accordingly.
 *
 * @author Tim Harper - Initial contribution
 *
 */
public class BooleanItemReader {
    private final Item item;
    private final OnOffType trueOnOffValue;
    private final OpenClosedType trueOpenClosedValue;

    private final Logger logger = LoggerFactory.getLogger(BooleanItemReader.class);

    /**
     *
     * @param item The item to read
     * @param trueOnOffValue If OnOffType, then consider true if this value
     * @param trueOpenClosedValue if OpenClosedType, then consider true if this value
     */
    BooleanItemReader(Item item, OnOffType trueOnOffValue, OpenClosedType trueOpenClosedValue) {
        this.item = item;
        this.trueOnOffValue = trueOnOffValue;
        this.trueOpenClosedValue = trueOpenClosedValue;
        if (!(item instanceof SwitchItem) && !(item instanceof ContactItem)) {
            logger.warn("Item {} is a {} instead of the expected SwitchItem or ContactItem", item.getName(),
                    item.getClass().getName());
        }
    }

    Boolean getValue() {
        State state = item.getState();
        if (state instanceof OnOffType) {
            return state.equals(trueOnOffValue);
        } else if (state instanceof OpenClosedType) {
            return state.equals(trueOpenClosedValue);
        } else {
            return null;
        }
    }
}
