/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.dial.models;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpStatus;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class DialState.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class DialState implements AutoCloseable {

    /** The Constant SONY_AV_NS. */
    // private Logger logger = LoggerFactory.getLogger(DialState.class);
    public final static String SONY_AV_NS = "urn:schemas-sony-com:av";

    /** The device infos. */
    // private final Map<String, UpnpService> _services = new HashMap<String, UpnpService>();
    private final List<DialDeviceInfo> _deviceInfos = new ArrayList<DialDeviceInfo>();

    /** The app uri. */
    private final URI _appUri;

    /** The requestor. */
    private HttpRequest _requestor;

    /**
     * Instantiates a new dial state.
     *
     * @param requestor the requestor
     * @param dialUri the dial uri
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException the URI syntax exception
     */
    public DialState(HttpRequest requestor, String dialUri)
            throws ParserConfigurationException, SAXException, IOException, URISyntaxException {

        _requestor = requestor;

        final HttpResponse resp = requestor.sendGetCommand(dialUri);
        if (resp.getHttpCode() != HttpStatus.SC_OK) {
            throw resp.createException();
        }

        // final URI baseUri = new URI(dialUri);

        final Document dialDocument = resp.getContentAsXml();

        // final NodeList iconLists = irccDocument.getElementsByTagName("iconList");
        // for (int i = iconLists.getLength() - 1; i >= 0; i--) {
        // final Node iconList = iconLists.item(i);
        // final NodeList icons = iconList.getChildNodes();
        // for (int ii = icons.getLength() - 1; ii >= 0; ii--) {
        // final Node icon = icons.item(ii);
        // _icons.add(new IrccIcon(icon));
        // }
        // }

        final String appUrl = resp.getResponseHeader("Application-URL");
        _appUri = new URI(appUrl);

        // final NodeList serviceLists = dialDocument.getElementsByTagName("serviceList");
        // for (int i = serviceLists.getLength() - 1; i >= 0; i--) {
        // final Node serviceList = serviceLists.item(i);
        // final NodeList services = serviceList.getChildNodes();
        // for (int ii = services.getLength() - 1; ii >= 0; ii--) {
        // final Node service = services.item(ii);
        // final UpnpService newService = new UpnpService(requestor, baseUri, service);
        // _services.put(newService.getServiceId(), newService);
        // }
        // }

        final NodeList deviceInfos = dialDocument.getElementsByTagNameNS(SONY_AV_NS, "X_DIALEX_DeviceInfo");
        for (int i = deviceInfos.getLength() - 1; i >= 0; i--) {
            final Node deviceInfo = deviceInfos.item(i);
            _deviceInfos.add(new DialDeviceInfo(requestor, deviceInfo));
        }
    }

    /**
     * Checks for dial service.
     *
     * @return true, if successful
     */
    public boolean hasDialService() {
        return _deviceInfos.size() > 0;
    }

    /**
     * Gets the dial apps.
     *
     * @return the dial apps
     */
    public Map<String, DialApp> getDialApps() {
        final Map<String, DialApp> apps = new HashMap<String, DialApp>();
        for (DialDeviceInfo ddi : _deviceInfos) {
            for (DialApp app : ddi.getDialApps().getApps()) {
                if (apps.containsKey(app.getId())) {
                } else {
                    apps.put(app.getId(), app);
                }
            }
        }
        return apps;
    }

    /**
     * Gets the app uri.
     *
     * @return the app uri
     */
    public URI getAppUri() {
        return _appUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        _requestor.close();
    }
}
