/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.dto.templates.TemplateListModel;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzBaseBridgeHandler;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating templates from a xml response.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class FritzAhaUpdateTemplatesCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaUpdateTemplatesCallback.class);

    private static final String WEBSERVICE_COMMAND = "switchcmd=gettemplatelistinfos";

    private final AVMFritzBaseBridgeHandler handler;

    /**
     * Constructor
     *
     * @param webInterface web interface to FRITZ!Box
     * @param handler handler that will update things
     */
    public FritzAhaUpdateTemplatesCallback(FritzAhaWebInterface webInterface, AVMFritzBaseBridgeHandler handler) {
        super(WEBSERVICE_PATH, WEBSERVICE_COMMAND, webInterface, GET, 1);
        this.handler = handler;
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        logger.trace("Received response '{}'", response);
        if (isValidRequest()) {
            try {
                XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(response));
                Unmarshaller unmarshaller = JAXBUtils.JAXBCONTEXT_TEMPLATES.createUnmarshaller();
                TemplateListModel model = unmarshaller.unmarshal(xsr, TemplateListModel.class).getValue();
                if (model != null) {
                    handler.addTemplateList(model.getTemplates());
                } else {
                    logger.debug("no template in response");
                }
            } catch (UnmarshalException e) {
                logger.debug("Failed to unmarshal XML document: {}", e.getMessage());
            } catch (JAXBException | XMLStreamException e) {
                logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
            }
        } else {
            logger.debug("request is invalid: {}", status);
        }
    }
}
