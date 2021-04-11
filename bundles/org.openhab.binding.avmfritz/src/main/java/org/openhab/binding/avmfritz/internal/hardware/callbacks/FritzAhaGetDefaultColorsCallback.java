/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import static org.eclipse.jetty.http.HttpMethod.GET;

import java.io.StringReader;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.dto.ColorDefaultsModel;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for retrieving default colors for DECT!500 bulb
 * TODO: This callback is rubbish. Using async get and writing everything to a callback execute() seems not the
 * correct way. we need to request the default colors once and store them (we don't expect the defaults to change,
 * once the bulb is registered.
 *
 * @author Joshua Bacher
 */
public class FritzAhaGetDefaultColorsCallback extends FritzAhaReauthCallback {

    private static final String WEBSERVICE_COMMAND = "switchcmd=getcolordefaults";
    private final Logger logger = LoggerFactory.getLogger(FritzAhaGetDefaultColorsCallback.class);
    private final String ain;
    private ColorDefaultsModel colorDefaultsModel;

    /**
     * Constructor
     *
     * @param webIface Interface to FRITZ!Box
     * @param ain AIN of the device that should be switched
     */
    public FritzAhaGetDefaultColorsCallback(FritzAhaWebInterface webIface, String ain) {
        super(WEBSERVICE_PATH, WEBSERVICE_COMMAND + "&ain=" + ain, webIface, GET, 1);
        this.ain = ain;
    }

    public ColorDefaultsModel getColorDefaultsModel() {
        return colorDefaultsModel;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        logger.trace("Received color default response {}", response);
        if (isValidRequest()) {
            try {
                Unmarshaller unmarshaller = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
                colorDefaultsModel = (ColorDefaultsModel) unmarshaller.unmarshal(new StringReader(response));
            } catch (JAXBException e) {
                logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
            }
        } else {
            logger.debug("request is invalid: {}", status);
        }
    }
}
