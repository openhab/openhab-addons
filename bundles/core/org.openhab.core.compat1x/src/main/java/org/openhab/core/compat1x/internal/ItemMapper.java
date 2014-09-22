package org.openhab.core.compat1x.internal;

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

public class ItemMapper {

	public static org.openhab.core.items.Item mapToOpenHABItem(Item item) {
		org.openhab.core.items.Item result = null;
		if(item instanceof StringItem) result = new org.openhab.core.library.items.StringItem(item.getName());
		if(item instanceof SwitchItem) result = new org.openhab.core.library.items.SwitchItem(item.getName());
		if(item instanceof ContactItem) result = new org.openhab.core.library.items.ContactItem(item.getName());
		if(item instanceof NumberItem) result = new org.openhab.core.library.items.NumberItem(item.getName());
		if(item instanceof RollershutterItem) result = new org.openhab.core.library.items.RollershutterItem(item.getName());
		if(item instanceof DimmerItem) result = new org.openhab.core.library.items.DimmerItem(item.getName());
		if(item instanceof ColorItem) result = new org.openhab.core.library.items.ColorItem(item.getName());
		if(item instanceof DateTimeItem) result = new org.openhab.core.library.items.DateTimeItem(item.getName());
		
		if(result instanceof org.openhab.core.items.GenericItem) {
			org.openhab.core.items.GenericItem genericItem = (GenericItem) result;
			genericItem.setState(TypeParser.parseState(genericItem.getAcceptedDataTypes(), item.getState().toString()));
		}
		return result;
	}
	
}
