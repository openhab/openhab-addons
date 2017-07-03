/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.openhab.binding.sony.internal.net.HttpRequest;
import org.openhab.binding.sony.internal.net.HttpResponse;
import org.openhab.binding.sony.internal.upnp.models.UpnpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccState.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccState implements AutoCloseable {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(IrccState.class);

    /** The Constant SONY_AV_NS. */
    public final static String SONY_AV_NS = "urn:schemas-sony-com:av";

    /** The Constant AN_REGISTER. */
    // Know action names
    public final static String AN_REGISTER = "register";

    /** The Constant AN_GETTEXT. */
    public final static String AN_GETTEXT = "getText";

    /** The Constant AN_SENDTEXT. */
    public final static String AN_SENDTEXT = "sendText";

    /** The Constant AN_GETCONTENTINFORMATION. */
    public final static String AN_GETCONTENTINFORMATION = "getContentInformation";

    /** The Constant AN_GETSYSTEMINFORMATION. */
    public final static String AN_GETSYSTEMINFORMATION = "getSystemInformation";

    /** The Constant AN_GETREMOTECOMMANDLIST. */
    public final static String AN_GETREMOTECOMMANDLIST = "getRemoteCommandList";

    /** The Constant AN_GETSTATUS. */
    public final static String AN_GETSTATUS = "getStatus";

    /** The Constant AN_GETHISTORYLIST. */
    public final static String AN_GETHISTORYLIST = "getHistoryList";

    /** The Constant AN_GETCONTENTURL. */
    public final static String AN_GETCONTENTURL = "getContentUrl";

    /** The Constant AN_SENDCONTENTURL. */
    public final static String AN_SENDCONTENTURL = "sendContentUrl";

    /** The Constant SRV_IRCC. */
    public final static String SRV_IRCC = "urn:schemas-sony-com:serviceId:IRCC";

    /** The Constant SRV_ACTION_SENDIRCC. */
    public final static String SRV_ACTION_SENDIRCC = "X_SendIRCC";

    /** The Constant SRV_ACTION_XGETSTATUS. */
    public final static String SRV_ACTION_XGETSTATUS = "X_GetStatus";

    /** The services. */
    // private List<IrccIcon> _icons = new ArrayList<IrccIcon>();
    private final Map<String, UpnpService> _services = new HashMap<String, UpnpService>();

    /** The device infos. */
    private final List<IrccDeviceInfo> _deviceInfos = new ArrayList<IrccDeviceInfo>();

    /** The unr device infos. */
    private final List<IrccUnrDeviceInfo> _unrDeviceInfos = new ArrayList<IrccUnrDeviceInfo>();

    /** The code lists. */
    private final List<IrccCodeList> _codeLists = new ArrayList<IrccCodeList>();

    /** The actions. */
    private IrccActions _actions;

    /** The sys info. */
    private IrccSystemInformation _sysInfo;

    /** The remote commands. */
    private IrccRemoteCommands _remoteCommands;

    /** The requestor. */
    private HttpRequest _requestor;

    /**
     * Instantiates a new ircc state.
     *
     * @param requestor the requestor
     * @param irccUri the ircc uri
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException the URI syntax exception
     */
    public IrccState(HttpRequest requestor, String irccUri)
            throws ParserConfigurationException, SAXException, IOException, URISyntaxException {

        _requestor = requestor;

        final HttpResponse resp = requestor.sendGetCommand(irccUri);
        if (resp.getHttpCode() != HttpStatus.SC_OK) {
            throw resp.createException();
        }

        final URI baseUri = new URI(irccUri);

        final Document irccDocument = resp.getContentAsXml();

        // final NodeList iconLists = irccDocument.getElementsByTagName("iconList");
        // for (int i = iconLists.getLength() - 1; i >= 0; i--) {
        // final Node iconList = iconLists.item(i);
        // final NodeList icons = iconList.getChildNodes();
        // for (int ii = icons.getLength() - 1; ii >= 0; ii--) {
        // final Node icon = icons.item(ii);
        // _icons.add(new IrccIcon(icon));
        // }
        // }

        final NodeList serviceLists = irccDocument.getElementsByTagName("serviceList");
        for (int i = serviceLists.getLength() - 1; i >= 0; i--) {
            final Node serviceList = serviceLists.item(i);
            final NodeList services = serviceList.getChildNodes();
            for (int ii = services.getLength() - 1; ii >= 0; ii--) {
                final Node service = services.item(ii);
                final UpnpService newService = new UpnpService(requestor, baseUri, service);
                _services.put(newService.getServiceId(), newService);
            }
        }

        final NodeList deviceInfos = irccDocument.getElementsByTagNameNS(SONY_AV_NS, "X_IRCC_DeviceInfo");
        for (int i = deviceInfos.getLength() - 1; i >= 0; i--) {
            final Node deviceInfo = deviceInfos.item(i);
            _deviceInfos.add(new IrccDeviceInfo(deviceInfo));
        }

        final NodeList unrDeviceInfos = irccDocument.getElementsByTagNameNS(SONY_AV_NS, "X_UNR_DeviceInfo");
        String actionsUrl = null;
        for (int i = unrDeviceInfos.getLength() - 1; i >= 0; i--) {
            final Node unrDeviceInfo = unrDeviceInfos.item(i);
            final IrccUnrDeviceInfo info = new IrccUnrDeviceInfo(unrDeviceInfo);
            _unrDeviceInfos.add(info);
            if (!StringUtils.isEmpty(info.getActionListUrl())) {
                if (StringUtils.isEmpty(actionsUrl)) {
                    actionsUrl = info.getActionListUrl();
                } else {
                    logger.debug("Multiple actions urls found - using: {}", actionsUrl);
                }
            }
        }

        final NodeList codeLists = irccDocument.getElementsByTagNameNS(SONY_AV_NS, "X_IRCCCodeList");
        for (int i = codeLists.getLength() - 1; i >= 0; i--) {
            final Node codeList = codeLists.item(i);
            final IrccCodeList irccCodeList = new IrccCodeList(codeList);
            _codeLists.add(irccCodeList);
        }

        // If empty - likely version 1.0 or 1.1
        if (StringUtils.isEmpty(actionsUrl)) {
            logger.debug("No actionsurl - generating default");
            _actions = new IrccActions(baseUri);
            _sysInfo = new IrccSystemInformation();
        } else {
            final HttpResponse actionsResp = requestor.sendGetCommand(actionsUrl);
            if (actionsResp.getHttpCode() == HttpStatus.SC_OK) {
                _actions = new IrccActions(actionsResp.getContentAsXml(), baseUri);
            } else {
                throw actionsResp.createException();
            }
            final String sysUrl = _actions.getUrlForAction(AN_GETSYSTEMINFORMATION);
            if (StringUtils.isEmpty(sysUrl)) {
                throw new NotImplementedException(AN_GETSYSTEMINFORMATION + " is not supported");
            }

            final HttpResponse sysResp = requestor.sendGetCommand(sysUrl);
            if (sysResp.getHttpCode() == HttpStatus.SC_OK) {
                _sysInfo = new IrccSystemInformation(sysResp.getContentAsXml());
            } else {
                throw sysResp.createException();
            }
        }

    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public HttpResponse getStatus() {
        final String statusUrl = getUrlForAction(IrccState.AN_GETSTATUS);
        if (statusUrl == null) {

            // final UpnpService service = getService(IrccState.SRV_IRCC);
            // if (service == null) {
            // return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, "IRCC Service was not found");
            // }
            //
            // final UpnpServiceDescriptor desc = service.getServiceDescriptor();
            // if (desc == null) {
            // return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503, "IRCC Service Descriptor was not found");
            // }
            // final UpnpServiceActionDescriptor actionDescriptor = desc
            // .getActionDescriptor(IrccState.SRV_ACTION_XGETSTATUS);
            // if (actionDescriptor == null) {
            // return new HttpResponse(HttpStatus.SERVICE_UNAVAILABLE_503,
            // "IRCC Service Action Descriptior was not found");
            // }
            //
            // IrccDeviceInfo di = getDeviceInformation();
            // for (String ctgy : di.getCategories()) {
            // final String soap = actionDescriptor.getSoap(ctgy);
            // final HttpResponse resp = _requestor.sendPostCommand(service.getControlUrl(), soap, new Header(
            // "SOAPACTION", "\"" + service.getServiceType() + "#" + IrccState.SRV_ACTION_XGETSTATUS + "\""));
            // if (resp.getHttpCode() == HttpStatus.SC_OK) {
            // return resp;
            // } else if (resp.getHttpCode() == HttpStatus.FORBIDDEN_403) {
            // return resp;
            // } else {
            // // ignore
            // }
            // }
            return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, "");
        } else {
            return _requestor.sendGetCommand(statusUrl);
        }
    }

    /**
     * Gets the url for action.
     *
     * @param actionName the action name
     * @return the url for action
     */
    public String getUrlForAction(String actionName) {
        return _actions.getUrlForAction(actionName);
    }

    /**
     * Gets the registration mode.
     *
     * @return the registration mode
     */
    public int getRegistrationMode() {
        return _actions.getRegistrationMode();
    }

    /**
     * Gets the service.
     *
     * @param serviceId the service id
     * @return the service
     */
    public UpnpService getService(String serviceId) {
        return _services.get(serviceId);
    }

    /**
     * Gets the system information.
     *
     * @return the system information
     */
    public IrccSystemInformation getSystemInformation() {
        return _sysInfo;
    }

    /**
     * Gets the device information.
     *
     * @return the device information
     */
    public IrccDeviceInfo getDeviceInformation() {
        return _deviceInfos.get(0);
    }

    /**
     * Gets the unr device information.
     *
     * @return the unr device information
     */
    public IrccUnrDeviceInfo getUnrDeviceInformation() {
        if (_unrDeviceInfos.size() == 0) {
            logger.debug("No UnrDeviceInformation - returning default version");
            return new IrccUnrDeviceInfo();
        }
        return _unrDeviceInfos.get(0);
    }

    /**
     * Gets the remote commands.
     *
     * @return the remote commands
     */
    public IrccRemoteCommands getRemoteCommands() {
        return _remoteCommands;
    }

    /**
     * Post authentication.
     *
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void postAuthentication() throws ParserConfigurationException, SAXException, IOException {
        final String remoteCommandsUrl = getUrlForAction(IrccState.AN_GETREMOTECOMMANDLIST);
        if (StringUtils.isEmpty(remoteCommandsUrl)) {
            _remoteCommands = new IrccRemoteCommands(_codeLists);
        } else {
            final HttpResponse rcResp = _requestor.sendGetCommand(remoteCommandsUrl);
            if (rcResp.getHttpCode() == HttpStatus.SC_OK) {
                _remoteCommands = new IrccRemoteCommands(_codeLists, rcResp.getContentAsXml());
            } else {
                throw rcResp.createException();
            }
        }
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

    // public class IrccIcon {
    // private int width;
    // private int height;
    // private int depth;
    // private String url;
    // private String mimeType;
    //
    // public IrccIcon(Node icon) {
    // final NodeList nodes = icon.getChildNodes();
    // for (int i = nodes.getLength() - 1; i >= 0; i--) {
    // final Node node = nodes.item(i);
    // final String nodeName = node.getNodeName();
    // if ("mimetype".equalsIgnoreCase(nodeName)) {
    // mimeType = node.getTextContent();
    // } else if ("width".equalsIgnoreCase(nodeName)) {
    // try {
    // width = Integer.parseInt(node.getTextContent());
    // } catch (NumberFormatException e) {
    // width = -1;
    // }
    // } else if ("height".equalsIgnoreCase(nodeName)) {
    // try {
    // height = Integer.parseInt(node.getTextContent());
    // } catch (NumberFormatException e) {
    // height = -1;
    // }
    // } else if ("depth".equalsIgnoreCase(nodeName)) {
    // try {
    // depth = Integer.parseInt(node.getTextContent());
    // } catch (NumberFormatException e) {
    // depth = -1;
    // }
    // } else if ("url".equalsIgnoreCase(nodeName)) {
    // url = node.getTextContent();
    // }
    // }
    // }
    // }

}
