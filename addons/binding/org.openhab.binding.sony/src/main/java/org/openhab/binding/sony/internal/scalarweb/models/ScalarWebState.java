/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpStatus;
import org.openhab.binding.sony.internal.ircc.models.IrccState;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebState.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ScalarWebState implements AutoCloseable {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebState.class);

    /** The Constant SONY_AV_NS. */
    public final static String SONY_AV_NS = "urn:schemas-sony-com:av";

    /** The device info. */
    private final ScalarWebDeviceInfo _deviceInfo;

    /** The ircc state. */
    private final IrccState _irccState;

    /**
     * Instantiates a new scalar web state.
     *
     * @param scalarWebUri the scalar web uri
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws URISyntaxException the URI syntax exception
     */
    public ScalarWebState(String scalarWebUri)
            throws IOException, ParserConfigurationException, SAXException, URISyntaxException {

        try (HttpRequest requestor = NetUtilities.createHttpRequest()) {
            final HttpResponse resp = requestor.sendGetCommand(scalarWebUri);
            if (resp.getHttpCode() != HttpStatus.SC_OK) {
                throw resp.createException();
            }

            final Document scalarWebDocument = resp.getContentAsXml();

            final NodeList deviceInfos = scalarWebDocument.getElementsByTagNameNS(SONY_AV_NS,
                    "X_ScalarWebAPI_DeviceInfo");
            if (deviceInfos.getLength() > 1) {
                logger.debug("More than one X_ScalarWebAPI_DeviceInfo found - using the first valid one");

            }

            // Use the first valid one
            ScalarWebDeviceInfo myDevice = null;
            for (int i = deviceInfos.getLength() - 1; i >= 0; i--) {
                final Node deviceInfo = deviceInfos.item(0);

                try {
                    myDevice = new ScalarWebDeviceInfo(deviceInfo);
                    break;
                } catch (IOException | DOMException | URISyntaxException e) {
                    logger.debug("Exception getting creating scalarwebapi device for {}[{}]: {}",
                            deviceInfo.getNodeName(), i, e.getMessage(), e);
                }
            }
            _deviceInfo = myDevice;

            if (_deviceInfo == null) {
                throw new IOException("No valid scalar web devices found");
            }
        }

        // The scalar descriptor probably includes IRCC state info...
        _irccState = new IrccState(NetUtilities.createHttpRequest(), scalarWebUri);
    }

    /**
     * Gets the device.
     *
     * @return the device
     */
    public ScalarWebDeviceInfo getDevice() {
        return _deviceInfo;
    }

    /**
     * Gets the ircc state.
     *
     * @return the ircc state
     */
    public IrccState getIrccState() {
        return _irccState;
    }

    /**
     * Gets the service.
     *
     * @param serviceName the service name
     * @return the service
     */
    public ScalarWebService getService(String serviceName) {
        return _deviceInfo.getService(serviceName);
    }

    /**
     * Post authentication.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws SAXException the SAX exception
     * @throws ParserConfigurationException the parser configuration exception
     */
    public void postAuthentication() throws IOException, SAXException, ParserConfigurationException {
        if (_irccState != null) {
            _irccState.postAuthentication();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return _deviceInfo.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        if (_irccState != null) {
            _irccState.close();
        }
        _deviceInfo.close();
    }
}
