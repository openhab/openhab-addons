/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.compat1x.internal;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.StringItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.openhab.core.items.GenericItem;
import org.openhab.core.types.TypeParser;
import org.openhab.library.tel.items.ESHCallItem;

public class ItemMapper {

	public static org.openhab.core.items.Item mapToOpenHABItem(Item item) {
		org.openhab.core.items.Item result = null;

		if (item instanceof StringItem)
			result = new org.openhab.core.library.items.StringItem(item.getName());
		else if (item instanceof SwitchItem)
			result = new org.openhab.core.library.items.SwitchItem(item.getName());
		else if (item instanceof ContactItem)
			result = new org.openhab.core.library.items.ContactItem(item.getName());
		else if (item instanceof NumberItem)
			result = new org.openhab.core.library.items.NumberItem(item.getName());
		else if (item instanceof RollershutterItem)
			result = new org.openhab.core.library.items.RollershutterItem(item.getName());
		else if (item instanceof DimmerItem)
			result = new org.openhab.core.library.items.DimmerItem(item.getName());
		else if (item instanceof ColorItem)
			result = new org.openhab.core.library.items.ColorItem(item.getName());
		else if (item instanceof DateTimeItem)
			result = new org.openhab.core.library.items.DateTimeItem(item.getName());
		else if (item instanceof ESHCallItem)
			result = new org.openhab.library.tel.items.CallItem(item.getName());

		if (item instanceof GroupItem) {
			GroupItem gItem = (GroupItem) item;
			
			org.openhab.core.items.Item baseItem = ItemMapper.mapToOpenHABItem(gItem.getBaseItem());
			org.openhab.core.items.GroupItem ohgItem;
			
			if (baseItem instanceof GenericItem) {
				ohgItem = new org.openhab.core.items.GroupItem(item.getName(), (GenericItem) baseItem);
			} else {
				ohgItem = new org.openhab.core.items.GroupItem(item.getName());
			}
			
			for (Item member : gItem.getMembers()) {
				org.openhab.core.items.Item ohMember = ItemMapper.mapToOpenHABItem(member);
				if (ohMember != null) {
					ohgItem.addMember(ohMember);
				}
			}
			result = ohgItem;
		}

		if (result instanceof org.openhab.core.items.GenericItem) {
			org.openhab.core.items.GenericItem genericItem = (GenericItem) result;
			if (item.getState() != null) {
				genericItem.setState(TypeParser.parseState(genericItem.getAcceptedDataTypes(), item.getState().toString()));
			}
		}
		
		return result;
	}

}
