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

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.avmfritz.handler.IFritzHandler;
import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.openhab.binding.avmfritz.internal.util.JAXBtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating multiple numbers decoded from a xml
 * response. Supports reauthorization.
 *
 * @author Robert Bausdorf
 * @author Christoph Weitkamp
 *
 */
public class FritzAhaUpdateXmlCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaUpdateXmlCallback.class);

    /**
     * Handler to update
     */
    private IFritzHandler handler;

    /**
     * Constructor
     *
     * @param webIface Webinterface to FRITZ!Box
     * @param handler Bridge handler that will update things.
     */
    public FritzAhaUpdateXmlCallback(FritzahaWebInterface webIface, IFritzHandler handler) {
        super(WEBSERVICE_PATH, "switchcmd=getdevicelistinfos", webIface, Method.GET, 1);
        this.handler = handler;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        logger.trace("Received State response {}", response);
        if (isValidRequest()) {
            try {
                final Unmarshaller jaxbUnmarshaller = JAXBtUtils.JAXBCONTEXT.createUnmarshaller();
                final DevicelistModel model = (DevicelistModel) jaxbUnmarshaller.unmarshal(new StringReader(response));
                if (model != null) {
                    for (final DeviceModel device : model.getDevicelist()) {
                        handler.addDeviceList(device);
                    }
                    handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "FRITZ!Box online");
                } else {
                    logger.warn("no model in response");
                }
            } catch (JAXBException e) {
                logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
            }
        } else {
            logger.info("request is invalid: {}", status);
        }
    }
}
