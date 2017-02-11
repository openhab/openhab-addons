/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaUpdateXmlCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polling worker class.
 */
public class DeviceListPolling implements Runnable {
	/**
	 * Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * Handler for delegation to callbacks.
	 */
	private IFritzHandler handler;
	/**
	 * Constructor.
	 * @param handler
	 */
	public DeviceListPolling(IFritzHandler handler) {
		this.handler = handler;
	}
	/**
	 * Poll the FRITZ!Box websevice one time. 
	 */
	@Override
	public void run() {
		if (handler.getWebInterface() != null) {
			logger.debug("polling fritzbox "
					+ handler.getWebInterface().getConfig().toString());
			FritzAhaUpdateXmlCallback callback = new FritzAhaUpdateXmlCallback(
					handler.getWebInterface(), this.handler);
			handler.getWebInterface().asyncGet(callback);
		}
	}
}
