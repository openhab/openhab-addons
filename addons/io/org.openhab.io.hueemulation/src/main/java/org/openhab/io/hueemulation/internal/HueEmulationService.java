/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.service.ReadyMarker;
import org.eclipse.smarthome.core.service.ReadyService;
import org.eclipse.smarthome.core.service.ReadyService.ReadyTracker;
import org.eclipse.smarthome.core.storage.StorageService;
import org.openhab.io.hueemulation.internal.RESTApi.HttpMethod;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueGroup;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse;
import org.openhab.io.hueemulation.internal.dto.response.HueResponse.HueErrorMessage;
import org.openhab.io.hueemulation.internal.dto.response.HueSuccessResponseStateChanged;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

/**
 * Emulates a Hue compatible HTTP API server. Provides /description.xml for upnp (see {@link HueEmulationUpnpServer}
 * and /api for a hue compatible REST API.
 *
 * <ul>
 * <li>Find the user management (/api/{username}/config/whitelist) in {@link UserManagement}.
 * <li>The unique device ID is managed by the {@link ConfigManagement}. A user is able to rename his brigde via the API.
 * <li>ESH items via the {@link ItemRegistry} are mapped to /api/{username}/lights and /api/{username}/groups in
 * {@link LightItems}.
 * <li>The REST processing is done in {@link RESTApi}.
 * </ul>
 *
 * @author Dan Cunningham - Initial Contribution
 * @author Kai Kreuzer - Improved resource handling to avoid leaks
 * @author David Graeff - Rewritten
 *
 */
@SuppressWarnings("serial")
@NonNullByDefault
@Component(immediate = true, service = {
        HueEmulationService.class }, configurationPid = "org.openhab.hueemulation", property = {
                org.osgi.framework.Constants.SERVICE_PID + "=org.openhab.hueemulation",
                ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=io:hueemulation",
                ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=io",
                ConfigurableService.SERVICE_PROPERTY_LABEL + "=Hue Emulation" })
public class HueEmulationService implements ReadyTracker {

    private final Path DISCOVERY_PATH = Paths.get(RESTApi.PATH + "/description.xml");
    private final Path DISCOVERY_PATH_ROOT = Paths.get("/description.xml");

    private final Logger logger = LoggerFactory.getLogger(HueEmulationService.class);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(HueSuccessResponseStateChanged.class, new HueSuccessResponseStateChanged.Serializer())
            .registerTypeAdapter(HueGroup.class, new HueGroup.Serializer()).create();

    //// Required services ////
    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) NetworkAddressService networkAddressService;
    private @NonNullByDefault({}) ReadyService readyService;
    protected @NonNullByDefault({}) HueEmulationUpnpServer discovery;

    //// objects, set within activate()
    private @NonNullByDefault({}) HueEmulationConfig config;
    private @NonNullByDefault({}) String xmlDoc;

    protected final HueDataStore ds = new HueDataStore();
    protected final UserManagement userManagement = new UserManagement(ds);
    protected final LightItems lightItems = new LightItems(ds);
    protected final ConfigManagement configManagement = new ConfigManagement(ds);
    protected final RESTApi restAPI = new RESTApi(ds, userManagement, configManagement, gson);
    protected boolean started = false;

    /**
     * A servlet for providing /api/discovery.xml and the REST API
     */
    HttpServlet restAPIservlet = new HttpServlet() {
        @NonNullByDefault({})
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            Utils.setHeaders(resp);
            final Path path = Paths.get(req.getRequestURI());
            final boolean isDebug = "debug=true".equals(req.getQueryString());
            String postBody;
            final HttpMethod method;

            try (PrintWriter httpOut = resp.getWriter()) {
                // UPNP discovery document
                if (path.equals(DISCOVERY_PATH)) {
                    sendDiscoveryXML(resp, httpOut);
                    return;
                }

                StringWriter out = new StringWriter();

                if (!isDebug) {
                    resp.setContentType("application/json");
                } else {
                    resp.setContentType("text/plain");
                }

                try {
                    method = Enum.valueOf(HttpMethod.class, req.getMethod());
                } catch (IllegalArgumentException e) {
                    resp.setStatus(405);
                    apiServerError(req, out, HueResponse.METHOD_NOT_ALLOWED,
                            req.getMethod() + " not allowed for this resource");
                    httpOut.print(out.toString());
                    return;
                }

                if (method == HttpMethod.POST || method == HttpMethod.PUT) {
                    try {
                        postBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                    } catch (IllegalStateException e) {
                        apiServerError(req, out, HueResponse.INTERNAL_ERROR,
                                "Could not read http body. Jetty failure.");
                        resp.setStatus(500);
                        return;
                    }
                } else {
                    postBody = "";
                }

                int statuscode = 0;
                try {
                    statuscode = restAPI.handle(method, postBody, out, path, isDebug);
                    switch (statuscode) {
                        case 10403: // Fake status code -> translate to real one
                            statuscode = 403;
                            apiServerError(req, out, HueResponse.LINK_BUTTON_NOT_PRESSED, "link button not pressed");
                            break;
                        case 403:
                            logger.debug("Unauthorized access to {} from {}:{}!\n", req.getRequestURI(),
                                    req.getRemoteAddr(), req.getRemotePort());
                            apiServerError(req, out, HueResponse.UNAUTHORIZED, "Not Authorized");
                            break;
                        case 404:
                            apiServerError(req, out, HueResponse.NOT_AVAILABLE, "Hue resource not available");
                            break;
                        case 405:
                            apiServerError(req, out, HueResponse.METHOD_NOT_ALLOWED,
                                    req.getMethod() + " not allowed for this resource");
                            break;
                    }
                } catch (JsonParseException e) {
                    statuscode = 400;
                    apiServerError(req, out, HueResponse.INVALID_JSON, "Invalid request: " + e.getMessage());
                }

                resp.setStatus(statuscode);
                httpOut.print(out.toString());

            }
        }
    };

    /**
     * A second servlet for providing /discovery.xml next to /api/discovery.xml
     */
    HttpServlet discoveryXMLservlet = new HttpServlet() {
        @NonNullByDefault({})
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            final Path path = Paths.get(req.getRequestURI());
            try (PrintWriter out = resp.getWriter()) {
                // UPNP discovery document
                if (path.equals(DISCOVERY_PATH) || path.equals(DISCOVERY_PATH_ROOT)) {
                    sendDiscoveryXML(resp, out);
                    return;
                }
            }
            resp.setStatus(404);
        }
    };

    @Activate
    protected void activate(Map<String, Object> properties) {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("discovery.xml");
        if (resourceAsStream == null) {
            logger.warn("Could not start Hue Emulation service: discovery.xml not found");
            return;
        }
        xmlDoc = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));

        started = false;
        modified(properties);

        // The hue emulation need to start very late in the start up process. The
        // ready marker service is used to make sure all items and item descriptions are loaded.
        readyService.registerTracker(this);
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        // Get and apply configurations
        this.config = new Configuration(properties).as(HueEmulationConfig.class);
        lightItems.setFilterTags(config.switchTags(), config.colorTags(), config.whiteTags());
        ds.config.linkbutton = config.pairingEnabled;
        ds.config.createNewUserOnEveryEndpoint = config.createNewUserOnEveryEndpoint;
        ds.config.networkopenduration = config.pairingTimeout;

        // If started: restart all parts of this service that depend on configuration
        if (!started) {
            return;
        }

        restartDiscovery();
        configManagement.checkPairingTimeout();
    }

    @Deactivate
    protected void deactivate() {
        readyService.unregisterTracker(this);
        configManagement.stopPairingTimeoutThread();
        lightItems.close();
        userManagement.writeToFile();
        configManagement.writeToFile();

        try {
            httpService.unregister(RESTApi.PATH);
            httpService.unregister("/description.xml");
        } catch (IllegalArgumentException ignored) {
        }
        if (discovery != null) {
            discovery.shutdown();
        }
    }

    @Override
    public void onReadyMarkerRemoved(ReadyMarker readyMarker) {
    }

    @Override
    public synchronized void onReadyMarkerAdded(ReadyMarker readyMarker) {
        if (started || !"org.eclipse.smarthome.model.core".equals(readyMarker.getIdentifier())) {
            return;
        }

        started = true;
        HttpContext httpContext = httpService.createDefaultHttpContext();
        try {
            httpService.registerServlet(RESTApi.PATH, restAPIservlet, new Hashtable<String, String>(), httpContext);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start Hue Emulation service: {}", e.getMessage(), e);
            return;
        }

        try { // This may fail, but it is not essential for the emulation (just for non-standard devices)
            httpService.registerServlet("/description.xml", discoveryXMLservlet, new Hashtable<String, String>(),
                    httpContext);
        } catch (ServletException | NamespaceException e) {
            logger.debug("Hue Emulation: Cannot register /description.xml");
        }

        configManagement.checkPairingTimeout();
        restartDiscovery();

        // Announce that this service is ready and unregister from the tracker
        readyService.markReady(new ReadyMarker("Online", "org.openhab.hueemulation"));
        CompletableFuture.runAsync(() -> readyService.unregisterTracker(this));
    }

    @Reference
    protected void setStateDescriptionService(ReadyService readyService) {
        this.readyService = readyService;
    }

    protected void unsetStateDescriptionService(ReadyService readyService) {
        this.readyService = null;
    }

    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configAdmin) {
        configManagement.setConfigAdmin(configAdmin);
    }

    protected void unsetConfigurationAdmin(ConfigurationAdmin configAdmin) {
        configManagement.setConfigAdmin(null);
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService netUtils) {
        this.networkAddressService = netUtils;
    }

    protected void unsetNetworkAddressService(NetworkAddressService netUtils) {
        this.networkAddressService = null;
    }

    @Reference
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        lightItems.setItemRegistry(itemRegistry);
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        lightItems.setItemRegistry(null);
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        restAPI.setEventPublisher(eventPublisher);
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        restAPI.setEventPublisher(null);
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC)
    protected void setStorageService(StorageService storageService) {
        ClassLoader loader = this.getClass().getClassLoader();
        userManagement.loadUsersFromFile(storageService.getStorage("hue.emulation.users", loader));
        lightItems.loadMappingFromFile(storageService.getStorage("hue.emulation.lights", loader));
        configManagement.loadConfigFromFile(storageService.getStorage("hue.emulation.config", loader));
    }

    protected void unsetStorageService(StorageService storageService) {
        userManagement.resetStorage();
        lightItems.resetStorage();
        configManagement.resetStorage();
    }

    void restartDiscovery() {
        if (discovery != null) {
            discovery.shutdown();
            discovery = null;
        }

        if (config.discoveryHttpPort == 0) {
            config.discoveryHttpPort = Integer.getInteger("org.osgi.service.http.port");
        }

        String discoveryIp = config.discoveryIp;
        if (discoveryIp == null) {
            discoveryIp = networkAddressService.getPrimaryIpv4HostAddress();
        }

        if (discoveryIp == null) {
            logger.warn("No primary IP address configured. Discovery disabled!");
            return;
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(discoveryIp);
        } catch (UnknownHostException e) {
            logger.warn("No primary IP address configured. Discovery disabled!", e);
            return;
        }

        discovery = new HueEmulationUpnpServer(RESTApi.PATH + "/description.xml", ds.config, address,
                config.discoveryHttpPort);
        discovery.start();

        ds.config.mac = Utils.getMAC(address);
        ds.config.ipaddress = address.getHostAddress();
    }

    private void sendDiscoveryXML(HttpServletResponse resp, PrintWriter out) throws IOException {
        resp.setContentType("application/xml");
        String address = discovery.getAddress().getHostAddress();
        out.write(
                String.format(xmlDoc, address, config.discoveryHttpPort, address, ds.config.bridgeid, ds.config.uuid));
    }

    /**
     * Hue API error response
     */
    public void apiServerError(HttpServletRequest req, Writer out, int error, String description) throws IOException {
        if (error == HueResponse.UNAUTHORIZED) {

        } else {
            logger.debug("'{}' for: {}\nRequest from: {}:{}\n", description, req.getRequestURI(), req.getRemoteAddr(),
                    req.getRemotePort());
        }

        try (JsonWriter writer = new JsonWriter(out)) {
            HueResponse e = new HueResponse(
                    new HueErrorMessage(error, req.getRequestURI().replace("/api", ""), description));
            gson.toJson(Collections.singleton(e), new TypeToken<List<?>>() {
            }.getType(), writer);
        }
    }
}
