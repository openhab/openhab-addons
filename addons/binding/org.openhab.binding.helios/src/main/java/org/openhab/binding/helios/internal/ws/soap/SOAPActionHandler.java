/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.helios.internal.ws.soap;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * The {@link SOAPActionHandler} is a custom SOAP handler that modifies some SOAP
 * headers in order to get the Helios comm. working
 *
 * @author Karel Goderis - Initial contribution
 */
public class SOAPActionHandler implements SOAPHandler<SOAPMessageContext> {
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        // Nothing to do here
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        // Nothing to do here
        return false;
    }

    @Override
    public void close(MessageContext context) {
        // Nothing to do here
    }

    @Override
    public Set<QName> getHeaders() {
        Set<QName> set = new HashSet<>();
        // Make sure the '[{http://www.w3.org/2005/08/addressing}]Action' header
        // is handled in case the device set the 'MustUnderstand' attribute to
        // '1'
        set.add(new QName("http://www.w3.org/2005/08/addressing", "Action"));
        return set;
    }
}
