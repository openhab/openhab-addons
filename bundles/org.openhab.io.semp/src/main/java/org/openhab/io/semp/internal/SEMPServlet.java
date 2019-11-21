/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for SSDP/SEMP Informations
 *
 * @author Markus Eckhardt - Initial Contribution
 *
 */
@SuppressWarnings("serial")
@Component(immediate = true, service = SEMPServlet.class, configurationPid = "org.openhab.semp", property = {
        ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=io:semp",
        ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=io",
        ConfigurableService.SERVICE_PROPERTY_LABEL + "=SEMP Service" })
public class SEMPServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(SEMPServlet.class);
    private static final String CONFIG_DISCOVERY_IP = "discoveryIp";
    private static final String CONFIG_DISCOVERY_UUID = "discoveryUUID";
    private static final String CONFIG_DISCOVERY_HTTP_PORT = "discoveryHttpPort";
    private static final String PATH_SEMP = "/semp";
    private HttpService httpService;
    private SEMPUpnpServer disco;
    private int webPort;
    private String uuid;
    private boolean isConfigured = false;
    private boolean isRunning = false;

    private SEMPCommandServlet commandServlet = new SEMPCommandServlet();

    @Activate
    protected void activate(Map<String, Object> config) {
        modified(config);
        if (!isRunning && isConfigured) {
            startServlets();
        }
    }

    @Modified
    protected void modified(Map<String, ?> config) {
        if (disco != null) {
            disco.shutdown();
            disco = null;
        }
        if (isRunning) {
            stopServlets();
        }

        Object obj = config.get(CONFIG_DISCOVERY_IP);
        String ip = obj != null ? (String) obj : null;
        obj = config.get(CONFIG_DISCOVERY_UUID);
        if (obj == null) {
            isConfigured = false;
            return;
        }

        UUID configUUID = null;
        try {
            configUUID = UUID.fromString((String) obj);
            uuid = configUUID.toString();
        } catch (IllegalArgumentException e) {
            logger.error("Wrong UUID: {} : {}", obj, e.toString());
            isConfigured = false;
            return;
        }
        if (uuid == null) {
            isConfigured = false;
            return;
        }

        obj = config.get(CONFIG_DISCOVERY_HTTP_PORT);
        webPort = obj == null ? Integer.getInteger("org.osgi.service.http.port") : Integer.parseInt((String) obj);
        disco = new SEMPUpnpServer("/uuid:" + uuid + "/description.xml", webPort, ip, uuid);
        disco.start();
        isConfigured = true;
    }

    private void startServlets() {
        if (isRunning || !isConfigured) {
            return;
        }
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            httpService.registerServlet("/uuid:" + uuid, this, servletParams, httpService.createDefaultHttpContext());
            httpService.registerServlet(PATH_SEMP, commandServlet, servletParams,
                    httpService.createDefaultHttpContext());
            logger.info("Started SSDP/SEMP Info service at {} and {}.", "/uuid:" + uuid, PATH_SEMP);
            isRunning = true;
        } catch (Exception e) {
            logger.error("Could not start SSDP/SEMP Info service: {}", e.getMessage(), e);
        }
    }

    private void stopServlets() {
        try {
            if (isRunning) {
                httpService.unregister("/uuid:" + uuid);
                httpService.unregister(PATH_SEMP);
            }
            isRunning = false;
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        stopServlets();
        if (disco != null) {
            disco.shutdown();
            isConfigured = false;
        }
    }

    @Reference
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        commandServlet.setItemRegistry(itemRegistry);
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        commandServlet.unsetItemRegistry(itemRegistry);
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        commandServlet.setEventPublisher(eventPublisher);
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        commandServlet.unsetEventPublisher(eventPublisher);
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addPersistenceService(PersistenceService service) {
        commandServlet.addPersistenceService(service);
    }

    public void removePersistenceService(PersistenceService service) {
        commandServlet.removePersistenceService(service);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        logger.debug("{}: {} {}", req.getRemoteAddr(), req.getMethod(), path);

        // handle UPnP device description request
        if (("/uuid:" + uuid + "/description.xml").equals(path)) {
            resp.setContentType("text/xml");
            apiDiscoveryXML(req, resp);
            resp.setStatus(Response.Status.OK.getStatusCode());
            return;
        } else if (("/uuid:" + uuid + "/presentation.html").equals(path)) {
            resp.setContentType("text/html");
            apiDiscoveryHTML(req, resp);
            resp.setStatus(Response.Status.OK.getStatusCode());
            return;
        } else if (path.startsWith(PATH_SEMP)) {
            // remove baseURI and remove trailing '/' if existing
            String cmd = path.substring(PATH_SEMP.length());
            if (cmd.endsWith("/")) {
                cmd = cmd.substring(0, cmd.length() - 2);
            }
            return;
        }
    }

    /**
     * Returns a empty ("{}") JSON response
     */
    public void emptyResponse(HttpServletRequest req, PrintWriter out) throws IOException {
        out.write("{}");
    }

    /**
     * Generates the XML Discovery document
     *
     * @return
     *         XML document
     */
    public void apiDiscoveryXML(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        InetAddress address = disco.getAddress();
        if (address == null) {
            return;
        }

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<?xml version=\"1.0\"?>\n");
        xmlBuilder.append("<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n");
        xmlBuilder.append("\t<specVersion>\n");
        xmlBuilder.append("\t\t<major>1</major>\n");
        xmlBuilder.append("\t\t<minor>0</minor>\n");
        xmlBuilder.append("\t</specVersion>\n");
        xmlBuilder.append("\t<device>\n");
        xmlBuilder.append("\t\t<deviceType>urn:" + SEMPConstants.SEMP_DEVICE_CONFIG.urn + "</deviceType>\n");
        xmlBuilder.append("\t\t<friendlyName>" + SEMPConstants.SEMP_DEVICE_CONFIG.friendlyName + "</friendlyName>\n");
        xmlBuilder.append("\t\t<manufacturer>" + SEMPConstants.SEMP_DEVICE_CONFIG.manufacturer + "</manufacturer>\n");
        xmlBuilder.append(
                "\t\t<manufacturerURL>" + SEMPConstants.SEMP_DEVICE_CONFIG.manufacturerURL + "</manufacturerURL>\n");
        xmlBuilder.append(
                "\t\t<modelDescription>" + SEMPConstants.SEMP_DEVICE_CONFIG.modelDescription + "</modelDescription>\n");
        xmlBuilder.append("\t\t<modelName>" + SEMPConstants.SEMP_DEVICE_CONFIG.modelName + "</modelName>\n");
        xmlBuilder.append("\t\t<modelNumber>" + SEMPConstants.SEMP_DEVICE_CONFIG.modelNumber + "</modelNumber>\n");
        xmlBuilder.append("\t\t<modelURL>" + SEMPConstants.SEMP_DEVICE_CONFIG.modelURL + "</modelURL>\n");
        xmlBuilder.append("\t\t<serialNumber>" + SEMPConstants.SEMP_DEVICE_CONFIG.serialNumber + "</serialNumber>\n");
        xmlBuilder.append("\t\t<UDN>uuid:" + uuid + "</UDN>\n");
        xmlBuilder.append("\t\t<serviceList>\n");
        for (SEMPConstants.SSDPServiceConfig serviceConfig : SEMPConstants.SSDP_SERVICE_CONFIG_LIST) {
            xmlBuilder.append("\t\t\t<service>\n");
            xmlBuilder.append("\t\t\t\t<serviceType>urn:" + serviceConfig.typeUrn + ":service:NULL:1</serviceType>\n");
            xmlBuilder.append("\t\t\t\t<serviceId>urn:" + serviceConfig.idUrn + ":serviceId:NULL</serviceId>\n");
            xmlBuilder.append("\t\t\t\t<SCPDURL>" + serviceConfig.serviceDescriptorURL + "</SCPDURL>\n");
            xmlBuilder.append("\t\t\t\t<controlURL>" + serviceConfig.controlURL + "</controlURL>\n");
            xmlBuilder.append("\t\t\t\t<eventSubURL>" + serviceConfig.eventSubURL + "</eventSubURL>\n");
            xmlBuilder.append("\t\t\t</service>\n");
        }
        xmlBuilder.append("\t\t</serviceList>\n");
        xmlBuilder.append(
                "\t\t<presentationURL>" + SEMPConstants.SEMP_DEVICE_CONFIG.presentationURL + "</presentationURL>\n");

        for (String element : SEMPConstants.SEMP_DEVICE_CONFIG.additionalElements) {
            element = element.replace("@IFACE_ADDR@", address.getHostAddress());
            xmlBuilder.append("\t\t" + element + "\n");
        }
        xmlBuilder.append("\t</device>\n");
        xmlBuilder.append("</root>\n");

        try (PrintWriter out = resp.getWriter()) {
            out.write(xmlBuilder.toString());
            logger.debug("Dic: {}", xmlBuilder.toString());
        }
    }

    /**
     * Generates the HTML Discovery document
     *
     * @return
     *         HTML document
     */
    public void apiDiscoveryHTML(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        InetAddress address = disco.getAddress();
        if (address == null) {
            return;
        }

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<html>\n");
        xmlBuilder.append("\t<head><title>SimpleEnergyManagementProtocol</title></head>\n");
        xmlBuilder.append("\t<body><p>SEMP Gateway Demo<p></body>\n");
        xmlBuilder.append("</html>\n");

        try (PrintWriter out = resp.getWriter()) {
            out.write(xmlBuilder.toString());
        }
    }
}
