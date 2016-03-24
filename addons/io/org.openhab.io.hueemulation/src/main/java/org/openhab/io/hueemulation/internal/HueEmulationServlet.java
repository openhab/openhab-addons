/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.io.hueemulation.internal.api.HueCreateUser;
import org.openhab.io.hueemulation.internal.api.HueDataStore;
import org.openhab.io.hueemulation.internal.api.HueDevice;
import org.openhab.io.hueemulation.internal.api.HueErrorResponse;
import org.openhab.io.hueemulation.internal.api.HueState;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Emulates A Hue compatible HTTP API server
 *
 * @author Dan Cunningham
 *
 */
@SuppressWarnings("serial")
public class HueEmulationServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(HueEmulationServlet.class);
    private static final String CONFIG_PAIRING_ENABLED = "pairingEnabled";
    private static final String PATH = "/api";
    private static final String HOMEKIT_PREFIX = "homekit:";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String APPLICATION_XML = "application/xml";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CHARSET = "utf-8";
    private static final String NEW_CLIENT_RESP = "[{\"success\":{\"username\": \"%s\"}}]";
    private static final String STATE_RESP = "[{\"success\":{\"/lights/%s/state/on\":%s}}]";
    private static final File USER_FILE = new File(
            ConfigConstants.getUserDataFolder() + File.separator + "hueemulation" + File.separator + "usernames");
    private static final File UDN_FILE = new File(
            ConfigConstants.getUserDataFolder() + File.separator + "hueemulation" + File.separator + "udn");

    /**
     * This parses "/api/{username}/{lights}/{id}/{state}"
     */
    private static final Pattern PATH_PATTERN = Pattern
            .compile(PATH + "/([^/]+)(?:(?:/(lights)/?([^/]+)?/?(state)?)?)?");

    private Gson gson = new Gson();
    private HttpService httpService;
    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private HueEmulationUpnpServer disco;
    private String udn;
    private String xmlDoc;
    private CopyOnWriteArrayList<String> userNames = new CopyOnWriteArrayList<String>();

    private boolean pairingEnabled = false;

    protected void activate(Map<String, Object> config) {
        modified(config);
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
            disco = new HueEmulationUpnpServer(PATH + "/discovery.xml", getUDN());
            disco.start();
            if (USER_FILE.exists()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(USER_FILE);
                    userNames.addAll(IOUtils.readLines(fis));
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            }
            logger.info("Started Hue Emulation service at " + PATH);
        } catch (Exception e) {
            logger.error("Could not start Hue Emulation service: {}", e.getMessage(), e);
        }
    }

    protected void modified(Map<String, ?> config) {
        Object pairingString = config.get(CONFIG_PAIRING_ENABLED);
        if (pairingString == null) {
            pairingEnabled = false;
        } else {
            if (pairingString instanceof Boolean) {
                pairingEnabled = ((Boolean) pairingString).booleanValue();
            } else {
                pairingEnabled = "true".equalsIgnoreCase((String) pairingString);
            }
        }
        logger.debug("Device pairing enabled : {}", pairingEnabled);
    }

    protected void deactivate(ComponentContext componentContext) {
        try {
            httpService.unregister(PATH);
        } catch (IllegalArgumentException ignored) {
        }
        if (disco != null) {
            disco.shutdown();
        }
    }

    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        logger.debug("{}: {} {}", req.getRemoteAddr(), req.getMethod(), path);
        setHeaders(resp);

        // UPNP discovery document
        if (path.equals(PATH + "/discovery.xml")) {
            apiDiscoveryXML(req, resp);
            return;
        }

        // everything is JSON from here
        resp.setContentType(APPLICATION_JSON);

        // request for API key
        if (path.equals(PATH) || path.equals(PATH + "/")) {
            if (pairingEnabled) {
                apiConfig(req, resp);
            } else {
                apiServerError(req, resp, HueErrorResponse.UNAUTHORIZED,
                        "Not Authorized. Pair button must be pressed to add users.");
            }
            return;
        }

        // All other API requests
        Matcher m = PATH_PATTERN.matcher(path);
        if (m.matches()) {
            String userName = m.group(1);
            boolean lightsReq = m.group(2) != null;
            String id = m.group(3);
            boolean stateReq = m.group(4) != null;

            /**
             * Some devices (Amazon Echo) seem to rely on the bridge to add an unknown user if pairing is on
             * instead of using the configApi method
             */
            if (pairingEnabled) {
                addUser(userName);
            } else if (!authorizeUser(userName)) {
                apiServerError(req, resp, HueErrorResponse.UNAUTHORIZED, "Not Authorized");
                return;
            }

            if (stateReq) {
                /**
                 * /api/{username}/{lights}/{id}/{state}
                 */
                apiState(id, req, resp);
            } else if (id != null) {
                /**
                 * /api/{username}/{lights}/{id}
                 */
                apiLight(id, req, resp);
            } else if (lightsReq) {
                /**
                 * /api/{username}/{lights}
                 */
                apiLights(req, resp);
            } else if (userName != null) {
                /**
                 * /api/{username}
                 */
                apiDataStore(req, resp);
            } else {
                apiServerError(req, resp, HueErrorResponse.NOT_AVAILABLE, "Hue resource not available");
            }
        }

    }

    /**
     * Hue API call to set the state of a light
     *
     * @param id
     * @param req
     * @param resp
     * @throws IOException
     */
    private void apiState(String id, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!req.getMethod().equals(METHOD_PUT)) {
            apiServerError(req, resp, HueErrorResponse.METHOD_NOT_AVAILABLE, "Only PUT allowed for this resource");
            return;
        }
        try {
            // will throw exception if not found
            Item item = itemRegistry.getItem(id);
            HueState state = gson.fromJson(req.getReader(), HueState.class);
            logger.debug("State " + state);
            String value;
            if (state.bri > -1) {
                value = String.valueOf(Math.round(state.bri / 255.0 * 100));
            } else {
                value = state.on ? "ON" : "OFF";
            }
            Command command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), value);
            eventPublisher.post(ItemEventFactory.createCommandEvent(id, command));
            PrintWriter out = resp.getWriter();
            out.write(String.format(STATE_RESP, id, String.valueOf(state.on)));
            out.close();
        } catch (ItemNotFoundException e) {
            logger.debug("Item not found: " + id);
            apiServerError(req, resp, HueErrorResponse.NOT_AVAILABLE, "The Hue device could not be found");
        }
    }

    /**
     * Hue API call to get the state of a single light
     *
     * @param id
     * @param req
     * @param resp
     * @throws IOException
     */
    private void apiLight(String id, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Item item = itemRegistry.getItem(id);
            PrintWriter out = resp.getWriter();
            out.write(gson.toJson(itemToDevice(item)));
            out.close();
        } catch (ItemNotFoundException e) {
            logger.debug("Item not found: " + id);
            apiServerError(req, resp, HueErrorResponse.NOT_AVAILABLE, "Item not found " + id);
        }
    }

    /**
     * Hue API call to get a listing of all lights
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    public void apiLights(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        out.write(gson.toJson(getHueDeviceNames()));
        out.close();
    }

    /**
     * HUE API call to get the Data Store of the bridge (only lights supported for now)
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    public void apiDataStore(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        HueDataStore ds = new HueDataStore();
        ds.lights = getHueDevices();
        out.write(gson.toJson(ds));
    }

    /**
     * Hue API call to configure a user
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    public void apiConfig(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!req.getMethod().equals(METHOD_POST)) {
            apiServerError(req, resp, HueErrorResponse.METHOD_NOT_AVAILABLE, "Only POST allowed for this resource");
            return;
        }
        PrintWriter out = resp.getWriter();

        HueCreateUser user = gson.fromJson(req.getReader(), HueCreateUser.class);
        logger.debug("Create user: " + user.devicetype);
        if (user.username == null || user.username.length() == 0) {
            user.username = UUID.randomUUID().toString();
        }
        addUser(user.username);
        String response = String.format(NEW_CLIENT_RESP, user.username);
        out.write(response);
        out.close();
    }

    /**
     * Hue API error response
     *
     * @param req
     * @param resp
     * @param error
     * @param description
     * @throws IOException
     */
    public void apiServerError(HttpServletRequest req, HttpServletResponse resp, int error, String description)
            throws IOException {
        logger.debug("apiServerError {} {}", error, description);
        PrintWriter out = resp.getWriter();
        HueErrorResponse e = new HueErrorResponse(error, req.getRequestURI(), description);
        out.write(gson.toJson(e));
    }

    /**
     * Generates the XML Discovery document
     *
     * @return
     *         XML document
     */
    public void apiDiscoveryXML(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (xmlDoc == null) {
            xmlDoc = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("discovery.xml"), "UTF-8");
        }

        InetAddress address = disco.getAddress();
        if (address == null) {
            return;
        }

        String formattedXML = String.format(xmlDoc, address.getHostAddress(),
                System.getProperty("org.osgi.service.http.port"), getUDN());
        resp.setContentType(APPLICATION_XML);
        PrintWriter out = resp.getWriter();
        out.write(formattedXML);
        out.close();
    }

    /**
     * Returns a map of all our items that have voice tags.
     *
     * @param username
     * @return
     *         Map <item name, HueDevice>
     */
    private Map<String, HueDevice> getHueDevices() {
        Collection<Item> items = getTaggedItems();
        Map<String, HueDevice> devices = new HashMap<String, HueDevice>();
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            devices.put(item.getName(), itemToDevice(item));
        }
        return devices;
    }

    /**
     * Returns the item name and voice name of each item
     *
     * @param username
     * @return
     *         Map<item name, item voice tag>
     */
    public Map<String, String> getHueDeviceNames() {
        Collection<Item> items = getTaggedItems();
        Map<String, String> devices = new HashMap<String, String>();
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            devices.put(item.getName(), item.getLabel());
        }
        return devices;
    }

    /**
     * Converts an VoiceItem to a HueDevice
     *
     * @param voiceItem
     * @return
     *         HueDevice
     */
    private HueDevice itemToDevice(Item item) {
        State itemState = item.getState();
        short bri = 0;
        if (itemState instanceof DecimalType) {
            bri = (short) ((((DecimalType) itemState).intValue() * 255) / 100);
        } else if (itemState instanceof OnOffType) {
            bri = (short) (((OnOffType) itemState) == OnOffType.ON ? 255 : 0);
        }
        HueState hueState = new HueState(bri > 0, bri);
        HueDevice d = new HueDevice(hueState, item.getLabel(), item.getName());
        return d;
    }

    /**
     * Gets all items that match our tag
     *
     * @return
     */
    private Collection<Item> getTaggedItems() {
        Collection<Item> items = new LinkedList<Item>();
        for (Item item : itemRegistry.getItems()) {
            for (String tag : item.getTags()) {
                if (tag.startsWith(HOMEKIT_PREFIX)) {
                    items.add(item);
                    break;
                }
            }
        }
        return items;
    }

    /**
     * Checks if the username exists in our user list
     *
     * @param userName
     * @return
     * @throws IOException
     */
    private boolean authorizeUser(String userName) throws IOException {
        return userNames.contains(userName);
    }

    /**
     * Adds a username to the user file
     *
     * @param userName
     * @throws IOException
     */
    private synchronized void addUser(String userName) throws IOException {
        if (!userNames.contains(userName)) {
            userNames.add(userName);
            USER_FILE.getParentFile().mkdirs();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(USER_FILE);
                IOUtils.writeLines(userNames, null, fos);
            } finally {
                IOUtils.closeQuietly(fos);
            }
        }
    }

    /**
     * Gets the unique UDN for this server, will generate and persist one if not found.
     *
     * @throws IOException
     */
    private synchronized String getUDN() throws IOException {
        if (udn == null) {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                if (!UDN_FILE.exists()) {
                    UDN_FILE.getParentFile().mkdirs();
                } else {
                    fis = new FileInputStream(UDN_FILE);
                    List<String> lines = IOUtils.readLines(fis);
                    if (lines.size() > 0) {
                        udn = lines.get(0);
                    }
                }
                if (udn == null) {
                    udn = UUID.randomUUID().toString();
                    fos = new FileOutputStream(UDN_FILE);
                    IOUtils.write(udn.getBytes(), fos);
                }
            } finally {
                IOUtils.closeQuietly(fis);
                IOUtils.closeQuietly(fos);
            }
        }
        return udn;
    }

    /**
     * Sets Hue API Headers
     *
     * @param response
     */
    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(CHARSET);
        response.setContentType(APPLICATION_JSON);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }

    public boolean getPairingEnabled() {
        return pairingEnabled;
    }
}
