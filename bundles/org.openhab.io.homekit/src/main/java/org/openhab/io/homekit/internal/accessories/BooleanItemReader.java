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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.io.homekit.internal.HomekitOHItemProxy;
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
    private final @Nullable BigDecimal trueThreshold;
    private final boolean invertThreshold;

    /**
     *
     * @param item The item to read
     * @param trueOnOffValue If OnOffType, then consider true if this value
     * @param trueOpenClosedValue if OpenClosedType, then consider true if this value
     */
    BooleanItemReader(Item item, OnOffType trueOnOffValue, OpenClosedType trueOpenClosedValue) {
        this(item, trueOnOffValue, trueOpenClosedValue, null, false);
    }

    /**
     *
     * @param item The item to read
     * @param trueOnOffValue If OnOffType, then consider true if this value
     * @param trueOpenClosedValue if OpenClosedType, then consider true if this value
     * @param trueThreshold If the state is numeric, and this param is given, return true if the value is above this
     *            threshold
     * @param invertThreshold Invert threshold to be true if below, not above
     */
    BooleanItemReader(Item item, OnOffType trueOnOffValue, OpenClosedType trueOpenClosedValue,
            @Nullable BigDecimal trueThreshold, boolean invertThreshold) {
        this.item = item;
        this.trueOnOffValue = trueOnOffValue;
        this.trueOpenClosedValue = trueOpenClosedValue;
        final Item baseItem = HomekitOHItemProxy.getBaseItem(item);
        this.trueThreshold = trueThreshold;
        this.invertThreshold = invertThreshold;
        if (!(baseItem instanceof SwitchItem || baseItem instanceof ContactItem || baseItem instanceof StringItem
                || (trueThreshold != null && baseItem instanceof NumberItem))) {
            if (trueThreshold != null) {
                logger.warn("Item {} is a {} instead of the expected SwitchItem, ContactItem, NumberItem or StringItem",
                        item.getName(), item.getClass().getSimpleName());
            } else {
                logger.warn("Item {} is a {} instead of the expected SwitchItem, ContactItem or StringItem",
                        item.getName(), item.getClass().getSimpleName());
            }
        }
    }

    boolean getValue() {
        State state = item.getState();
        final BigDecimal localTrueThresheold = trueThreshold;
        if (state instanceof PercentType) {
            state = state.as(OnOffType.class);
        }
        if (state instanceof OnOffType) {
            return state.equals(trueOnOffValue);
        } else if (state instanceof OpenClosedType) {
            return state.equals(trueOpenClosedValue);
        } else if (state instanceof StringType) {
            return state.toString().equalsIgnoreCase("Open") || state.toString().equalsIgnoreCase("Opened");
        } else if (localTrueThresheold != null) {
            if (state instanceof DecimalType) {
                final boolean result = ((DecimalType) state).toBigDecimal().compareTo(localTrueThresheold) > 0;
                return result ^ invertThreshold;
            } else if (state instanceof QuantityType) {
                final boolean result = ((QuantityType<?>) state).toBigDecimal().compareTo(localTrueThresheold) > 0;
                return result ^ invertThreshold;
            }
        }
        logger.debug("Unexpected item state,  returning false. Item {}, State {} ({})", item.getName(), state,
                state.getClass().getSimpleName());
        return false;
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
