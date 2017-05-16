/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Type;

/**
 * Provides mappings from {@link Type} classes to {@link Item} classes.
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class TypeItemMap {

    private static final Map<Class<? extends Type>, Class<? extends GenericItem>> TYPE_ITEM_MAP;

    static {
        Map<Class<? extends Type>, Class<? extends GenericItem>> typeItemMap = new HashMap<>();
        typeItemMap.put(DateTimeType.class, DateTimeItem.class);
        typeItemMap.put(DecimalType.class, NumberItem.class);
        typeItemMap.put(HSBType.class, ColorItem.class);
        typeItemMap.put(IncreaseDecreaseType.class, DimmerItem.class);
        typeItemMap.put(OnOffType.class, SwitchItem.class);
        typeItemMap.put(OpenClosedType.class, ContactItem.class);
        typeItemMap.put(PercentType.class, NumberItem.class);
        typeItemMap.put(StopMoveType.class, RollershutterItem.class);
        typeItemMap.put(StringType.class, StringItem.class);
        typeItemMap.put(UpDownType.class, RollershutterItem.class);
        TYPE_ITEM_MAP = Collections.unmodifiableMap(typeItemMap);
    }

    public static Class<? extends GenericItem> get(Class<? extends Type> type) {
        return TYPE_ITEM_MAP.get(type);
    }

}
