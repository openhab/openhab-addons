/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

/**
 * Java Bean to represent a JSON response to a <code>devicelist</code> API
 * method call.
 * 
 * @author Andreas Brenk
 * @author Gaël L'hopital
 * 
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class DeviceListBody extends AbstractMessage {

	protected List<NetatmoDevice> devices;
	protected List<NetatmoModule> modules;

	public List<NetatmoDevice> getDevices() {
		return this.devices;
	}

	public List<NetatmoModule> getModules() {
		return this.modules;
	}

	public List<AbstractDevice> getAllEquipments() {
		List<AbstractDevice> result = new ArrayList<AbstractDevice>(
				getDevices());
		result.addAll(getModules());

		return result;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = createToStringBuilder();
		builder.appendSuper(super.toString());

		builder.append("devices", getDevices());
		builder.append("modules", getModules());

		return builder.toString();
	}
}
