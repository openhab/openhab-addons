/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import java.util.List;

/**
 * Class to hold all known and described Netatmo API request responses
 * 
 * @author Gaël L'hopital
 * 
 */
public class NetatmoResponses {
	public static class User extends NetatmoResponse<UserBody> {
	};

	public static class DeviceList extends NetatmoResponse<DeviceListBody> {
	};

	public static class Measurement extends
			NetatmoResponse<List<MeasurementBody>> {
	};
}
