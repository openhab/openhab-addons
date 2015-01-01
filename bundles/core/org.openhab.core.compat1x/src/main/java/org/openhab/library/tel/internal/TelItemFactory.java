/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.library.tel.internal;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.openhab.library.tel.items.ESHCallItem;


/**
 * {@link ItemFactory}-Implementation for this library's ItemTypes
 * 
 * @author Thomas.Eichstaedt-Engelen
 * @author Kai Kreuzer
 * @since 0.9.0
 */
public class TelItemFactory implements org.eclipse.smarthome.core.items.ItemFactory {
	
	public static final String ITEM_TYPE = "Call";

	/**
	 * @{inheritDoc}
	 */
	public GenericItem createItem(String itemTypeName, String itemName) {
		if (itemTypeName.equals(ITEM_TYPE))
			return new ESHCallItem(itemName);
		else {
			return null;
		}
	}
	
	/**
	 * @{inheritDoc}
	 */
	public String[] getSupportedItemTypes() {
		return new String[] { ITEM_TYPE };
	}

}
