/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.openhab.binding.avmfritz.internal.discovery.AvmDiscoveryService;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback for discovering SmartHome devices connected to a FRITZ!Box
 * 
 * @author Robert Bausdorf
 * 
 */
public class FritzAhaDiscoveryCallback extends FritzAhaReauthCallback {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private AvmDiscoveryService service;

	/**
	 * Constructor
	 * @param webIface Webinterface to FRITZ!Box
	 * @param service Discovery service to call with result.
	 */
	public FritzAhaDiscoveryCallback(FritzahaWebInterface webIface, AvmDiscoveryService service) {
		super("webservices/homeautoswitch.lua", "switchcmd=getdevicelistinfos", webIface, Method.GET, 1);
		this.service = service;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(int status, String response) {
		super.execute(status, response);
		if (this.isValidRequest()) {
			logger.debug("discovery callback response " + response);
			try {
				JAXBContext jaxbContext = JAXBContext
						.newInstance(DevicelistModel.class);
				Unmarshaller jaxbUM = jaxbContext.createUnmarshaller();

				DevicelistModel model = (DevicelistModel) jaxbUM
						.unmarshal(new StringReader(response));
				if( model != null ) {
					for( DeviceModel device : model.getDevicelist() )
					{
						this.service.onDeviceAddedInternal(device);
					}
				} else {
					logger.warn("no model in response");
				}
			} catch (JAXBException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
	}
}
