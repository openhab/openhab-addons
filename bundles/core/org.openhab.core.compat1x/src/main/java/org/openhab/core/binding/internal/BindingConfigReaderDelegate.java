/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.binding.internal;

import org.eclipse.smarthome.model.item.BindingConfigParseException;
import org.eclipse.smarthome.model.item.BindingConfigReader;

/**
 * This class serves as a mapping from the "old" org.openhab namespace to the new org.eclipse.smarthome
 * namespace for the binding config readers. It wraps an instance with the old interface
 * into a class with the new interface. 
 * 
 * @author Kai Kreuzer - Initial contribution and API
 */
public class BindingConfigReaderDelegate implements BindingConfigReader {

	private org.openhab.model.item.binding.BindingConfigReader reader;

	public BindingConfigReaderDelegate(
			org.openhab.model.item.binding.BindingConfigReader reader) {
		this.reader = reader;
	}

	@Override
	public String getBindingType() {
		return reader.getBindingType();
	}

	@Override
	public void validateItemType(String itemType, String bindingConfig)
			throws BindingConfigParseException {
		try {
			reader.validateItemType(getOpenHABItem(itemType), bindingConfig);
		} catch (org.openhab.model.item.binding.BindingConfigParseException e) {
			throw new BindingConfigParseException(e.getMessage());
		}
		
	}

	@Override
	public void processBindingConfiguration(String context, String itemType, String itemName,
			String bindingConfig) throws BindingConfigParseException {
		try {
			reader.processBindingConfiguration(context, getOpenHABItem(itemType, itemName), bindingConfig);
		} catch (org.openhab.model.item.binding.BindingConfigParseException e) {
			throw new BindingConfigParseException(e.getMessage());
		}
		
	}

	private org.openhab.core.items.Item getOpenHABItem(String itemType) throws BindingConfigParseException {
		return getOpenHABItem(itemType, "itemName");
	}

	private org.openhab.core.items.Item getOpenHABItem(String itemType,
			String itemName) throws BindingConfigParseException {

		switch(itemType) {
		case "Switch" : return new org.openhab.core.library.items.SwitchItem(itemName);
		case "Dimmer" : return new org.openhab.core.library.items.DimmerItem(itemName);
		case "Color" : return new org.openhab.core.library.items.ColorItem(itemName);
		case "String" : return new org.openhab.core.library.items.StringItem(itemName);
		case "Number" : return new org.openhab.core.library.items.NumberItem(itemName);
		case "Contact" : return new org.openhab.core.library.items.ContactItem(itemName);
		case "Rollershutter" : return new org.openhab.core.library.items.RollershutterItem(itemName);
		case "DateTime" : return new org.openhab.core.library.items.DateTimeItem(itemName);
		case "Call" : return new org.openhab.library.tel.items.CallItem(itemName);
		}
		throw new BindingConfigParseException("cannot process unknown item type " + itemType);
	}

    @Override
    public void startConfigurationUpdate(String context) {
        reader.removeConfigurations(context);        
    }

    @Override
    public void stopConfigurationUpdate(String context) {
    }

}
