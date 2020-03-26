package org.openhab.persistence.influxdb2.internal;

import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;

public class ItemTestHelper {

    public static NumberItem createNumberItem(String name, int value) {
        NumberItem numberItem = new NumberItem(name);
        numberItem.setState(new DecimalType(value));
        return numberItem;
    }
}
