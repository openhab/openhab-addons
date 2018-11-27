/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.service.ReadyMarker;
import org.eclipse.smarthome.core.service.ReadyService;
import org.eclipse.smarthome.core.service.ReadyService.ReadyTracker;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.io.hueemulation.internal.dto.HueCreateUser;
import org.openhab.io.hueemulation.internal.dto.HueDataStore;
import org.openhab.io.hueemulation.internal.dto.HueDevice;
import org.openhab.io.hueemulation.internal.dto.HueResponse;
import org.openhab.io.hueemulation.internal.dto.HueResponse.HueErrorMessage;
import org.openhab.io.hueemulation.internal.dto.HueStateChange;
import org.openhab.io.hueemulation.internal.dto.HueSuccessResponseCreateUser;
import org.openhab.io.hueemulation.internal.dto.HueSuccessResponseStateChanged;
import org.openhab.io.hueemulation.internal.dto.HueUnauthorizedConfig;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

/**
 * Emulates A Hue compatible HTTP API server.
 * All original Hue bridge endpoints are emulated, but only /config, /lights, /whitelist are implemented.
 *
 * @author Dan Cunningham - Initial Contribution
 * @author Kai Kreuzer - Improved resource handling to avoid leaks
 * @author David Graeff - Rewritten. Automatic pairing timeout, correct http method handling, endpoints added
 *
 */
@SuppressWarnings("serial")
@NonNullByDefault
@Component(immediate = true, service = { HueEmulationServlet.class,
        HttpServlet.class }, configurationPid = "org.openhab.hueemulation", property = {
                org.osgi.framework.Constants.SERVICE_PID + "=org.openhab.hueemulation",
                ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=io:hueemulation",
                ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=io",
                ConfigurableService.SERVICE_PROPERTY_LABEL + "=Hue Emulation" })
public class HueEmulationServlet extends HttpServlet implements ReadyTracker {
    public static final String PATH = "/api";
    private final Path DISCOVERY_PATH = Paths.get("/api/description.xml");

    private final Logger logger = LoggerFactory.getLogger(HueEmulationServlet.class);
    private final Gson gson;

    //// Required services ////
    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) ItemRegistry itemRegistry;
    private @NonNullByDefault({}) EventPublisher eventPublisher;
    private @NonNullByDefault({}) HueEmulationUpnpServer discovery;
    private @NonNullByDefault({}) ConfigurationAdmin configAdmin;
    private @NonNullByDefault({}) NetworkAddressService networkAddressService;
    private @NonNullByDefault({}) ReadyService readyService;
    //// objects, set within activate()
    private @NonNullByDefault({}) HueEmulationConfig config;
    private @NonNullByDefault({}) String xmlDoc;

    private final HueDataStore ds = new HueDataStore();
    private final UserManagement userManagement;
    private final LightItems lightItems;
    private @Nullable Thread pairingTimeoutThread;
    private boolean started = false;

    public HueEmulationServlet() {
        gson = new GsonBuilder().registerTypeAdapter(HueSuccessResponseStateChanged.class,
                new HueSuccessResponseStateChanged.Serializer()).create();
        userManagement = new UserManagement(ds, gson);
        lightItems = new LightItems(ds, gson);
    }

    @Activate
    protected void activate(Map<String, Object> properties) {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("discovery.xml");
        if (resourceAsStream == null) {
            logger.warn("Could not start Hue Emulation service: discovery.xml not found");
            return;
        }
        xmlDoc = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));

        this.config = new Configuration(properties).as(HueEmulationConfig.class);
        userManagement.loadUsersFromFile();
        lightItems.loadMappingFromFile();
        lightItems.setFilterTags(config.switchTags(), config.colorTags(), config.whiteTags());

        // The hue emulation need to start very late in the start up process. The
        // ready marker service is used to make sure all items and item descriptions are loaded.
        readyService.registerTracker(this);
    }

    @Modified
    protected void modified(Map<String, Object> properties) {
        // Get and apply configurations
        this.config = new Configuration(properties).as(HueEmulationConfig.class);
        lightItems.setFilterTags(config.switchTags(), config.colorTags(), config.whiteTags());

        // If started: restart all parts of this service that depend on configuration
        if (!started) {
            return;
        }

        restartDiscovery();
        stopPairingTimeoutThread();
        startPairingTimeoutThread();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        readyService.unregisterTracker(this);
        stopPairingTimeoutThread();
        lightItems.close(itemRegistry);
        userManagement.writeToFile();

        try {
            httpService.unregister(PATH);
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
    public void onReadyMarkerAdded(ReadyMarker readyMarker) {
        if (started || !"org.eclipse.smarthome.model.core".equals(readyMarker.getIdentifier())) {
            return;
        }

        started = true;
        try {

            httpService.registerServlet(PATH, this, new Hashtable<String, String>(),
                    httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start Hue Emulation service: {}", e.getMessage(), e);
            return;
        }

        lightItems.fetchItemsAndWatchRegistry(itemRegistry);

        restartDiscovery();
        stopPairingTimeoutThread();
        startPairingTimeoutThread();

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
        this.configAdmin = configAdmin;
    }

    protected void unsetConfigurationAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = null;
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
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    private void startPairingTimeoutThread() {
        if (config.pairingEnabled) {
            logger.info("Started Hue Emulation service with pairing for {}s at {}", config.pairingTimeout, PATH);
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(config.pairingTimeout * 1000);
                    org.osgi.service.cm.Configuration configuration = configAdmin
                            .getConfiguration("org.openhab.hueemulation");
                    Dictionary<String, Object> dictionary = configuration.getProperties();
                    dictionary.put(HueEmulationConfig.CONFIG_PAIRING_ENABLED, false);
                    dictionary.put(HueEmulationConfig.CONFIG_CREATE_NEW_USER_ON_THE_FLY, false);
                    configuration.update(dictionary);
                } catch (IOException | InterruptedException ignore) {
                }
            });
            pairingTimeoutThread = thread;
            thread.start();
        } else {
            logger.info("Started Hue Emulation service without pairing at {}", PATH);
        }
    }

    private void stopPairingTimeoutThread() {
        Thread thread = pairingTimeoutThread;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(2000);
            } catch (InterruptedException e) {
            }
        }
    }

    private void restartDiscovery() {
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

        try {
            discovery = new HueEmulationUpnpServer(PATH + "/description.xml", UDN.getUDN(), address,
                    config.discoveryHttpPort);
            discovery.start();
        } catch (IOException e) {
            logger.warn("Could not start UPNP server for discovery", e);
        }
    }

    @NonNullByDefault({})
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setHeaders(resp);
        final Path path = Paths.get(req.getRequestURI());

        // Fast exit for non-api consumers
        if (!path.startsWith(PATH) || path.getNameCount() > 5) {
            resp.setStatus(404);
            return;
        }

        try (PrintWriter out = resp.getWriter()) {
            // UPNP discovery document
            if (path.equals(DISCOVERY_PATH)) {
                resp.setContentType("application/xml");
                String address = discovery.getAddress().getHostAddress();
                out.write(String.format(xmlDoc, address, config.discoveryHttpPort, address, UDN.getUDN()));
                return;
            }

            // everything is JSON from here
            resp.setContentType("application/json");

            // request for API key
            if (path.getNameCount() == 1) {
                if (!"POST".equals(req.getMethod())) {
                    resp.setStatus(404);
                    apiServerError(req, out, HueResponse.METHOD_NOT_AVAILABLE, "Only POST allowed for this resource");
                    return;
                }
                if (config.pairingEnabled) {
                    apiCreateUser(req, resp, out);
                } else {
                    resp.setStatus(403);
                    apiServerError(req, out, HueResponse.LINK_BUTTON_NOT_PRESSED, "link button not pressed");
                }
                return;
            }

            updateDataStore();

            final String userName = path.getName(1).toString();

            // Reduced config
            /** /api/config */
            if ("config".equals(userName)) {
                try (JsonWriter writer = new JsonWriter(out)) {
                    gson.toJson(ds.config, new TypeToken<HueUnauthorizedConfig>() {
                    }.getType(), writer);
                }
                return;
            }

            if (!userManagement.authorizeUser(userName)) {
                if (config.pairingEnabled && config.createNewUserOnEveryEndpoint) {
                    userManagement.addUser(userName, "Formerly authorized device");
                } else {
                    resp.setStatus(403);
                    apiServerError(req, out, HueResponse.UNAUTHORIZED, "Not Authorized");
                    return;
                }
            }

            if (path.getNameCount() == 2) {
                /** /api/{username} */
                out.write(gson.toJson(ds));
                return;
            }

            String function = path.getName(2).toString();
            // The following block is generic, it works for all Map fields in the datastore
            try {
                Field field = ds.getClass().getField(function);
                Object object = field.get(ds);
                switch (path.getNameCount()) {
                    case 3:
                        /** /api/{username}/lights */
                        if (req.getMethod().equals("GET")) {
                            out.write(gson.toJson(object));
                        } else {
                            apiServerError(req, out, HueResponse.METHOD_NOT_AVAILABLE,
                                    req.getMethod() + " not allowed for this resource");
                        }
                        return;
                    case 4:
                        String id = path.getName(3).toString();
                        /** /api/{username}/lights/{id} */
                        if (req.getMethod().equals("GET")) {
                            final @SuppressWarnings("rawtypes") Map objectMap = Map.class.cast(object);
                            final @SuppressWarnings("rawtypes") Optional first = objectMap.keySet().stream()
                                    .findFirst();
                            final Object value;
                            if (Integer.class.equals(first.get().getClass())) {
                                value = objectMap.get(new Integer(id));
                            } else {
                                value = objectMap.get(id);
                            }
                            if (value == null) {
                                logger.debug("Could not find {} for id {}. ", function, id);
                                apiServerError(req, out, HueResponse.NOT_AVAILABLE,
                                        function + " " + id + " does not exist.");
                                return;
                            } else {
                                out.write(gson.toJson(value));
                            }
                        } else // Remove a user
                        if (req.getMethod().equals("DELETE") && "whitelist".equals(function)) {
                            // Only own user can be removed
                            if (userName.equals(id)) {
                                userManagement.removeUser(id);
                            } else {
                                resp.setStatus(403);
                                apiServerError(req, out, HueResponse.UNAUTHORIZED, "Not Authorized");
                            }
                        } else {
                            apiServerError(req, out, HueResponse.METHOD_NOT_AVAILABLE,
                                    req.getMethod() + " not allowed for this resource");
                        }
                        return;
                    case 5:
                        /** /api/{username}/lights/{id}/state */
                        // Only lights allowed for /state so far
                        if (req.getMethod().equals("PUT") && "lights".equals(function)) {
                            apiSetState(path, req, out);
                        } else {
                            apiServerError(req, out, HueResponse.METHOD_NOT_AVAILABLE,
                                    req.getMethod() + " not allowed for this resource");
                        }
                        return;
                    default:
                        break;
                }
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException
                    | NoSuchElementException e) {
            }
            resp.setStatus(404);
            apiServerError(req, out, HueResponse.NOT_AVAILABLE, "Hue resource not available");
        }
    }

    /**
     * Hue API call to set the state of a light.
     * Enpoint: /api/{username}/lights/{id}/state
     */
    @SuppressWarnings({ "null", "unused" })
    private void apiSetState(Path uri, HttpServletRequest req, PrintWriter out) throws IOException {
        /** {username}/lights/{id}/state */
        Integer hueID = new Integer(uri.getName(3).toString());
        HueDevice hueDevice = ds.lights.get(hueID);
        if (hueDevice == null) {
            apiServerError(req, out, HueResponse.NOT_AVAILABLE, "The Hue device could not be found");
            return;
        }

        HueStateChange state;
        try {
            state = gson.fromJson(req.getReader(), HueStateChange.class);
        } catch (com.google.gson.JsonParseException e) {
            state = null;
        }
        if (state == null) {
            apiServerError(req, out, HueResponse.INTERNAL_ERROR, "Could not parse received json");
            return;
        }

        // Apply new state and collect success, error items
        Map<String, Object> successApplied = new TreeMap<>();
        List<String> errorApplied = new ArrayList<>();
        Command command = hueDevice.applyState(state, successApplied, errorApplied);

        // If a command could be created, post it to the framework now
        if (command != null) {
            logger.debug("sending {} to {}", command, hueDevice.item.getName());
            eventPublisher.post(ItemEventFactory.createCommandEvent(hueDevice.item.getName(), command));
        }

        // Generate the response. The response consists of a list with an entry each for all
        // submitted change requests. If for example "on" and "bri" was send, 2 entries in the response are
        // expected.
        Path contextPath = uri.subpath(2, uri.getNameCount() - 1);
        List<HueResponse> responses = new ArrayList<>();
        successApplied.forEach((t, v) -> {
            responses.add(new HueResponse(new HueSuccessResponseStateChanged(contextPath.resolve(t).toString(), v)));
        });
        errorApplied.forEach(v -> {
            responses.add(new HueResponse(new HueErrorMessage(HueResponse.NOT_AVAILABLE,
                    contextPath.resolve(v).toString(), "Could not set")));
        });

        try (JsonWriter writer = new JsonWriter(out)) {
            gson.toJson(responses, new TypeToken<List<?>>() {
            }.getType(), writer);
        }
    }

    /**
     * HUE API call to get the Data Store of the bridge (only lights supported for now)
     */
    public void updateDataStore() throws IOException {
        ds.config.UTC = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        ds.config.localtime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        ds.config.linkbutton = config.pairingEnabled;
        ds.config.mac = getMAC();
        ds.config.ipaddress = discovery.getAddress().getHostAddress();
        ds.config.uuid = UDN.getUDN();
    }

    /**
     * Handles POST on /api: To configure a new user
     */
    @SuppressWarnings("null")
    public void apiCreateUser(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException {
        HueCreateUser user = gson.fromJson(req.getReader(), HueCreateUser.class);
        if (user == null || user.devicetype == null || user.devicetype.isEmpty()) {
            resp.setStatus(400);
            apiServerError(req, out, HueResponse.INVALID_JSON, "body contains invalid JSON");
            return;
        }

        if (user.username == null || user.username.length() == 0) {
            user.username = UUID.randomUUID().toString();
        }
        userManagement.addUser(user.username, user.devicetype);

        try (JsonWriter writer = new JsonWriter(out)) {
            HueSuccessResponseCreateUser h = new HueSuccessResponseCreateUser(user.username);
            gson.toJson(Collections.singleton(h), new TypeToken<List<?>>() {
            }.getType(), writer);
        }
    }

    /**
     * Hue API error response
     */
    public void apiServerError(HttpServletRequest req, PrintWriter out, int error, String description)
            throws IOException {
        if (error == HueResponse.UNAUTHORIZED) {
            logger.debug("Unauthorized access to {} from {}:{}!\n", req.getRequestURI(), req.getRemoteAddr(),
                    req.getRemotePort());
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

    private String getMAC() throws UnknownHostException, SocketException {
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(discovery.getAddress());

        byte[] mac = networkInterface.getHardwareAddress();
        if (mac == null) {
            return "00:00:88:00:bb:ee";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

    /**
     * Sets Hue API Headers
     */
    private static void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }
}
