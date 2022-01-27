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
package org.openhab.binding.helios.internal.ws.soap;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.openhab.binding.helios.internal.handler.HeliosHandler27;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SOAPSubscriptionActionHandler} is a custom SOAP handler that modifies
 * some SOAP headers in order to get the Helios comm. working
 *
 * @author Karel Goderis - Initial contribution
 */
public class SOAPSubscriptionActionHandler extends SOAPActionHandler {

    private Logger logger = LoggerFactory.getLogger(SOAPSubscriptionActionHandler.class);

    private HeliosHandler27 handler;

    public SOAPSubscriptionActionHandler(HeliosHandler27 heliosHandler) {
        this.handler = heliosHandler;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (isRequest) {
            try {
                SOAPMessage soapMsg = context.getMessage();
                SOAPEnvelope soapEnv = soapMsg.getSOAPPart().getEnvelope();
                SOAPHeader soapHeader = soapEnv.getHeader();

                if (soapHeader == null) {
                    soapHeader = soapEnv.addHeader();
                }

                QName qname = new QName("http://www.2n.cz/2013/event", "SubscriptionId");
                SOAPHeaderElement soapHeaderElement = soapHeader.addHeaderElement(qname);

                soapHeaderElement.addAttribute(
                        new QName("http://www.w3.org/2005/08/addressing", "IsReferenceParameter"), "true");
                if (handler.getSubscriptionID() != null) {
                    soapHeaderElement.addTextNode(handler.getSubscriptionID());
                }
                soapMsg.saveChanges();
            } catch (Exception e) {
                logger.debug("An exception occurred while formatting a SOAP header : '{}'", e.getMessage());
            }
        }

        return true;
    }
}
