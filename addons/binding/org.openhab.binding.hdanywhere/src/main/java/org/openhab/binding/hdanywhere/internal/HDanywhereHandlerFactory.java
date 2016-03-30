/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hdanywhere.internal;

import static org.openhab.binding.hdanywhere.HDanywhereBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import org.openhab.binding.hdanywhere.HDanywhereBindingConstants;
import org.openhab.binding.hdanywhere.handler.HDanywhereHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * The {@link HDanywhereHandlerFactory} is responsible for creating things and
 * thing handlers.
 * 
 * @author Karel Goderis - Initial contribution
 */
public class HDanywhereHandlerFactory extends BaseThingHandlerFactory {

	private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
			.singleton(THING_TYPE_MATRIX);

	@Override
	public boolean supportsThingType(ThingTypeUID thingTypeUID) {
		return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
	}

	@Override
	protected ThingHandler createHandler(Thing thing) {

		ThingTypeUID thingTypeUID = thing.getThingTypeUID();

		if (thingTypeUID.equals(THING_TYPE_MATRIX)) {
			return new HDanywhereHandler(thing);
		}

		return null;
	}

	@Override
	public Thing createThing(ThingTypeUID thingTypeUID,
			Configuration configuration, ThingUID thingUID, ThingUID bridgeUID) {
		if (HDanywhereBindingConstants.THING_TYPE_MATRIX.equals(thingTypeUID)) {
			ThingUID matrixUID = getMatrixUID(thingTypeUID, thingUID,
					configuration);
			return super.createThing(thingTypeUID, configuration, matrixUID,
					null);
		}
		throw new IllegalArgumentException("The thing type " + thingTypeUID
				+ " is not supported by the HDanywhere binding.");
	}

	private ThingUID getMatrixUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
			Configuration configuration) {
		String ipAddress = (String) configuration
				.get(HDanywhereHandler.IP_ADDRESS);

		if (thingUID == null) {
			thingUID = new ThingUID(thingTypeUID,
					ipAddress.replaceAll("[^A-Za-z0-9_]", "_"));
		}
		return thingUID;
	}
}
