/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import javax.xml.bind.JAXBException;

import org.openhab.binding.avmfritz.internal.ahamodel.DeviceModel;
import org.openhab.binding.avmfritz.internal.ahamodel.DevicelistModel;
import org.openhab.binding.avmfritz.internal.discovery.AVMFritzDiscoveryService;
import org.openhab.binding.avmfritz.internal.hardware.FritzahaWebInterface;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback for discovering SmartHome devices connected to a FRITZ!Box
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 *
 */
public class FritzAhaDiscoveryCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaDiscoveryCallback.class);

    /**
     * Handler to update
     */
    private AVMFritzDiscoveryService service;

    /**
     * Constructor
     *
     * @param webIface Webinterface to FRITZ!Box
     * @param service Discovery service to call with result.
     */
    public FritzAhaDiscoveryCallback(FritzahaWebInterface webIface, AVMFritzDiscoveryService service) {
        super(WEBSERVICE_PATH, "switchcmd=getdevicelistinfos", webIface, Method.GET, 1);
        this.service = service;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        logger.trace("Received discovery callback response: {}", response);
        if (isValidRequest()) {
            try {
                DevicelistModel model = JAXBUtils.buildResult(response);
                if (model != null) {
                    for (DeviceModel device : model.getDevicelist()) {
                        service.onDeviceAddedInternal(device);
                    }
                } else {
                    logger.warn("no model in response");
                }
            } catch (JAXBException e) {
                logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
            }
        } else {
            logger.debug("request is invalid: {}", status);
        }
    }
}
