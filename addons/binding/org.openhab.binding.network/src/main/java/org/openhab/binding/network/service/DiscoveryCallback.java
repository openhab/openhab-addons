/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network.service;

/**
 * Callback for a new Device to be committed to Homematic
 * @author Marc Mettke - Initial contribution
 */
public interface DiscoveryCallback {
	public void newDevice(String ip);
}
