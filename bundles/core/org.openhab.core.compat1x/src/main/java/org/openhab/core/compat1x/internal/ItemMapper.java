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
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.core.items.GenericItem;
import org.openhab.core.types.TypeParser;
import org.openhab.library.tel.items.ESHCallItem;

public class ItemMapper {

	public static org.openhab.core.items.Item mapToOpenHABItem(Item item) {
		if (item==null) {
		    return null;
		}
	    
	    org.openhab.core.items.Item result = null;
		Class<? extends Item> itemClass = item.getClass();
		
		if (itemClass.equals(StringItem.class))
			result = new org.openhab.core.library.items.StringItem(item.getName());
		else if (itemClass.equals(SwitchItem.class))
			result = new org.openhab.core.library.items.SwitchItem(item.getName());
		else if (itemClass.equals(ContactItem.class))
			result = new org.openhab.core.library.items.ContactItem(item.getName());
		else if (itemClass.equals(NumberItem.class))
			result = new org.openhab.core.library.items.NumberItem(item.getName());
		else if (itemClass.equals(RollershutterItem.class))
			result = new org.openhab.core.library.items.RollershutterItem(item.getName());
		else if (itemClass.equals(DimmerItem.class))
			result = new org.openhab.core.library.items.DimmerItem(item.getName());
		else if (itemClass.equals(ColorItem.class))
			result = new org.openhab.core.library.items.ColorItem(item.getName());
		else if (itemClass.equals(DateTimeItem.class))
			result = new org.openhab.core.library.items.DateTimeItem(item.getName());
		else if (itemClass.equals(ESHCallItem.class))
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
			State state = item.getState();
			if (state != null) {
				org.openhab.core.types.State ohState = null;
				if (state==UnDefType.NULL) {
					ohState = org.openhab.core.types.UnDefType.NULL;
				} else if (state==UnDefType.UNDEF) {
					ohState = org.openhab.core.types.UnDefType.UNDEF;
				} else {
					ohState = TypeParser.parseState(genericItem.getAcceptedDataTypes(), state.toString());
				}
				if (ohState != null) {
					genericItem.setState(ohState);
				}
			}
		}
		
		return result;
	}

}
