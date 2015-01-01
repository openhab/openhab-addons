/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.library.tel.items;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.library.tel.internal.TelItemFactory;
import org.openhab.library.tel.types.ESHCallType;

public class ESHCallItem extends GenericItem {
	
	private static List<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();
	private static List<Class<? extends Command>> acceptedCommandTypes = new ArrayList<Class<? extends Command>>();

	static {
		acceptedDataTypes.add(ESHCallType.class);
		acceptedDataTypes.add(UnDefType.class);
	}
	
	public ESHCallItem(String name) {
		super(TelItemFactory.ITEM_TYPE, name);
	}

	public List<Class<? extends State>> getAcceptedDataTypes() {
		return acceptedDataTypes;
	}

	public List<Class<? extends Command>> getAcceptedCommandTypes() {
		return acceptedCommandTypes;
	}
	
}
