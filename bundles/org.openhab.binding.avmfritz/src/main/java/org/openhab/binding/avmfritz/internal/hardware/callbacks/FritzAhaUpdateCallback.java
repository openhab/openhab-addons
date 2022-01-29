/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.avmfritz.internal.dto.DeviceListModel;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzBaseBridgeHandler;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.openhab.binding.avmfritz.internal.util.JAXBUtils;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating multiple numbers decoded from a xml
 * response. Supports reauthorization.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@NonNullByDefault
public class FritzAhaUpdateCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaUpdateCallback.class);

    private static final String WEBSERVICE_COMMAND = "switchcmd=getdevicelistinfos";

    private final AVMFritzBaseBridgeHandler handler;

    /**
     * Constructor
     *
     * @param webIface Webinterface to FRITZ!Box
     * @param handler Bridge handler that will update things.
     */
    public FritzAhaUpdateCallback(FritzAhaWebInterface webIface, AVMFritzBaseBridgeHandler handler) {
        super(WEBSERVICE_PATH, WEBSERVICE_COMMAND, webIface, GET, 1);
        this.handler = handler;
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        logger.trace("Received State response {}", response);
        if (isValidRequest()) {
            try {
                XMLStreamReader xsr = JAXBUtils.XMLINPUTFACTORY.createXMLStreamReader(new StringReader(response));
                Unmarshaller unmarshaller = JAXBUtils.JAXBCONTEXT_DEVICES.createUnmarshaller();
                DeviceListModel model = unmarshaller.unmarshal(xsr, DeviceListModel.class).getValue();
                if (model != null) {
                    handler.onDeviceListAdded(model.getDevicelist());
                } else {
                    logger.debug("no model in response");
                }
                handler.setStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
            } catch (UnmarshalException e) {
                logger.debug("Failed to unmarshal XML document: {}", e.getMessage());
                handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            } catch (JAXBException | XMLStreamException e) {
                logger.error("Exception creating Unmarshaller: {}", e.getLocalizedMessage(), e);
                handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        e.getLocalizedMessage());
            }
        } else {
            logger.debug("request is invalid: {}", status);
            handler.setStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Request is invalid");
        }
    }
}
