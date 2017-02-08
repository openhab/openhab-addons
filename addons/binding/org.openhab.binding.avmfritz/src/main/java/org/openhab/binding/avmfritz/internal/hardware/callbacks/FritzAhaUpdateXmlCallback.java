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

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.avmfritz.handler.IFritzHandler;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating multiple numbers decoded from a xml
 * response. Supports reauthorization.
 * 
 * @author Robert Bausdorf
 * 
 */
public class FritzAhaUpdateXmlCallback extends FritzAhaReauthCallback {
	/**
	 * logger
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Handler to update
	 */
	private IFritzHandler handler;

	/**
	 * Constructor
	 * @param webIface Webinterface to FRITZ!Box
	 * @param handler Bridge handler taht will update things.
	 */
	public FritzAhaUpdateXmlCallback(FritzahaWebInterface webIface, IFritzHandler handler) {
		super(WEBSERVICE_PATH, "switchcmd=getdevicelistinfos", webIface, Method.GET, 1);
		this.handler = handler;
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute(int status, String response) {
		super.execute(status, response);
		if (this.isValidRequest()) {
			logger.trace("Received State response " + response);
			try {
				JAXBContext jaxbContext = JAXBContext
						.newInstance(DevicelistModel.class);
				Unmarshaller jaxbUM = jaxbContext.createUnmarshaller();

				DevicelistModel model = (DevicelistModel) jaxbUM
						.unmarshal(new StringReader(response));
				if( model != null ) {
					for( DeviceModel device : model.getDevicelist() ) {
						handler.addDeviceList(device);
					}
					handler.setStatusInfo(ThingStatus.ONLINE, 
							ThingStatusDetail.NONE, "FritzBox online");
				} else {
					logger.warn("no model in response");
				}
			} catch (JAXBException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		} else {
			logger.info("request is invalid: " + status);
		}
	}
}
