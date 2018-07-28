package org.openhab.binding.gruenbecksoftener.handler;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.StringItem;

public enum SoftenerDataType {
    STRING(StringItem.class),
    NUMBER(NumberItem.class);

    private Class<? extends Item> type;

    private SoftenerDataType(Class<? extends Item> type) {
        this.type = type;
    }

    public static SoftenerDataType fromItemType(String itemType, ItemFactory factory) {
        Item item = factory.createItem(itemType, "");

        for (SoftenerDataType type : values()) {
            if (type.type.isAssignableFrom(item.getClass())) {
                return type;
            }
        }
        return null;
    }
}
