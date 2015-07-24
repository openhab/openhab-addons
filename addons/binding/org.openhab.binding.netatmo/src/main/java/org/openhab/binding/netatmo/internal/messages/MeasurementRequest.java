/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openhab.binding.netatmo.handler.AbstractEquipment;
import org.openhab.binding.netatmo.handler.NetatmoDeviceHandler;
import org.openhab.binding.netatmo.handler.NetatmoModuleHandler;
import org.openhab.binding.netatmo.internal.OAuthCredentials;

/**
 * Queries the Netatmo API for the measures of a single device or module.
 * 
 * @author Andreas Brenk - Initial OH1 version
 * @author Gaël L'hopital - Port to OH2
 * 
 * @see <a href="http://dev.netatmo.com/doc/restapi/getmeasure">getmeasure</a>
 */
public class MeasurementRequest extends AbstractTokenedRequest {

	private final AbstractEquipment equipment;
	private final SortedSet<String> measures = new TreeSet<String>();

	/**
	 * Creates a request for the measurements of a device or module.
	 * 
	 * If you don't specify a moduleId you will retrieve the device's
	 * measurements. If you do specify a moduleId you will retrieve the module's
	 * measurements.
	 * 
	 * @param credentials
	 * @param equipment
	 */
	public MeasurementRequest(OAuthCredentials credentials,
			AbstractEquipment equipment) {
		super("api/getmeasure", credentials);
		this.equipment = equipment;
	}

	/**
	 * @param measure
	 *            the name of a supported measure, e.g. "Temperature" or
	 *            "Humidity"
	 */
	public void addMeasure(String measureType) {
		this.measures.add(measureType);
	}

	public SortedSet<String> getMeasures() {
		return this.measures;
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = createToStringBuilder();
		builder.appendSuper(super.toString());

		if (equipment instanceof NetatmoDeviceHandler) {
			NetatmoDevice device = (NetatmoDevice) equipment.getEquipment();
			builder.append("deviceId", device.getId());
		} else if (equipment instanceof NetatmoModuleHandler) {
			NetatmoModule module = (NetatmoModule) equipment.getEquipment();
			builder.append("deviceId", module.getMainDevice());
			builder.append("moduleId", module.getId());
		}

		builder.append("measures", this.measures);

		return builder.toString();
	}

	@Override
	protected StringBuilder getUrlBuilder() {
		StringBuilder urlBuilder = super.getUrlBuilder();
		urlBuilder.append("&scale=max");
		urlBuilder.append("&date_end=last");
		urlBuilder.append("&device_id=");
		if (equipment instanceof NetatmoDeviceHandler) {
			NetatmoDevice device = (NetatmoDevice) equipment.getEquipment();
			urlBuilder.append(device.getId());
		} else if (equipment instanceof NetatmoModuleHandler) {
			NetatmoModule module = (NetatmoModule) equipment.getEquipment();
			urlBuilder.append(module.getMainDevice());
			urlBuilder.append("&module_id=");
			urlBuilder.append(module.getId());
		}

		urlBuilder.append("&type=");
		for (final Iterator<String> i = this.measures.iterator(); i.hasNext();) {
			urlBuilder.append(i.next());
			if (i.hasNext()) {
				urlBuilder.append(",");
			}
		}
		return urlBuilder;
	}
}
