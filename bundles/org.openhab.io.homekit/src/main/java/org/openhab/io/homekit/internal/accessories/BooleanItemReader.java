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
package org.openhab.io.homekit.internal.accessories;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps either a SwitchItem or a ContactItem, interpreting the open / closed states accordingly.
 *
 * @author Tim Harper - Initial contribution
 *
 */
@NonNullByDefault
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
        if (!(item instanceof SwitchItem) && !(item instanceof ContactItem) && !(item instanceof StringItem)) {
            logger.warn("Item {} is a {} instead of the expected SwitchItem, ContactItem or StringItem", item.getName(),
                    item.getClass().getName());
        }
    }

    boolean getValue() {
        final State state = item.getState();
        if (state instanceof OnOffType) {
            return state.equals(trueOnOffValue);
        } else if (state instanceof OpenClosedType) {
            return state.equals(trueOpenClosedValue);
        } else if (state instanceof StringType) {
            return state.toString().equalsIgnoreCase("Open") || state.toString().equalsIgnoreCase("Opened");
        } else {
            logger.debug("Unexpected item state,  returning false. Item {}, State {}", item.getName(), state);
            return false;
        }
    }

    private OnOffType getOffValue(OnOffType onValue) {
        return onValue == OnOffType.ON ? OnOffType.OFF : OnOffType.ON;
    }

    void setValue(Boolean value) {
        if (item instanceof SwitchItem) {
            ((SwitchItem) item).send(value ? trueOnOffValue : getOffValue(trueOnOffValue));
        } else if (item instanceof GroupItem) {
            ((GroupItem) item).send(value ? trueOnOffValue : getOffValue(trueOnOffValue));
        } else {
            logger.debug("Cannot set value {} for item {}. Only Switch and Group items are supported.", value, item);
        }
    }
}
