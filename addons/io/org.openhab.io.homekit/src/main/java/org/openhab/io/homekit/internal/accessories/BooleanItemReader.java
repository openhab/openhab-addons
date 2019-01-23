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
 * @author Tim Harper
 *
 */
public class BooleanItemReader {
    private final Item item;
    private final OnOffType trueOnOffValue;
    private final OpenClosedType trueOpenClosedValue;

    private static Logger logger = LoggerFactory.getLogger(BooleanItemReader.class);

    /**
     *
     * @param item                The item to read
     * @param trueOnOffValue      If OnOffType, then consider true if this value
     * @param trueOpenClosedValue if OpenClosedType, then consider true if this value
     */
    BooleanItemReader(Item item, OnOffType trueOnOffValue, OpenClosedType trueOpenClosedValue) {
        this.item = item;
        this.trueOnOffValue = trueOnOffValue;
        this.trueOpenClosedValue = trueOpenClosedValue;
        if (!(item instanceof SwitchItem) && !(item instanceof ContactItem)) {
            logger.error("Item {} is a {} instead of the expected SwitchItem or ContactItem", item.getName(),
                    item.getClass().getName());
        }
    }

    Boolean getValue() {
        State state = item.getState();
        if (state instanceof OnOffType) {
            return state == trueOnOffValue;
        } else if (state instanceof OpenClosedType) {
            return state == trueOpenClosedValue;
        } else {
            return null;
        }
    }
}
