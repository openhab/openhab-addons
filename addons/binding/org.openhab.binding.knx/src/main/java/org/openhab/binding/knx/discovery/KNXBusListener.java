/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.discovery;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;

public interface KNXBusListener {

	/**
	 * 
	 * Called when a KNX telegram is seen on the KNX bus
	 * 
	 * @param source - the KNX source sending the telegram
	 * @param destination - the destination group address
	 * @param asdu - the telegram payload
	 */
	public void onActivity(IndividualAddress source, GroupAddress destination, byte[] asdu);
	
}
