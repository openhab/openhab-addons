/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.io.hueemulation.internal.api.HueCreateUser;
import org.openhab.io.hueemulation.internal.api.HueDataStore;
import org.openhab.io.hueemulation.internal.api.HueDevice;
import org.openhab.io.hueemulation.internal.api.HueErrorResponse;
import org.openhab.io.hueemulation.internal.api.HueGroup;
import org.openhab.io.hueemulation.internal.api.HueState;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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
    private static final String CONFIG_DISCOVERY_IP = "discoveryIp";
    private static final String CONFIG_DISCOVERY_HTTP_PORT = "discoveryHttpPort";
    private static final String PATH = "/api";
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
    private static final File ITEM_FILE = new File(
            ConfigConstants.getUserDataFolder() + File.separator + "hueemulation" + File.separator + "items");
    private static final String[] SUPPORTED_TAGS = new String[] { "Switchable", "Lighting" };
    private Gson gson = new Gson();
    private HttpService httpService;
    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;
    private HueEmulationUpnpServer disco;
    private String udn;
    private String xmlDoc;
    private int webPort;
    private boolean pairingEnabled = false;
    // list of valid Hue API user ids
    private ArrayList<String> userNames = new ArrayList<String>();
    // deviceMap maps a Hue numeric id to a Item Name, ordered by that id
    private TreeMap<Integer, String> deviceMap = new TreeMap<Integer, String>();

    protected void activate(Map<String, Object> config) {
        modified(config);
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());

            // load users from disk
            if (USER_FILE.exists()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(USER_FILE);
                    userNames.addAll(IOUtils.readLines(fis));
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            }

            // load item list from disk
            if (ITEM_FILE.exists()) {
                JsonReader reader = null;
                try {
                    reader = new JsonReader(new FileReader(ITEM_FILE));
                    LinkedHashMap<Integer, String> tmpMap = gson.fromJson(reader,
                            new TypeToken<Map<Integer, String>>() {
                            }.getType());
                    if (tmpMap != null) {
                        deviceMap.putAll(tmpMap);
                    }
                } finally {
                    IOUtils.closeQuietly(reader);
                }
            }
            logger.info("Started Hue Emulation service at " + PATH);
        } catch (Exception e) {
            logger.error("Could not start Hue Emulation service: {}", e.getMessage(), e);
        }
    }

    protected void modified(Map<String, ?> config) {
        if (disco != null) {
            disco.shutdown();
            disco = null;
        }

        Object obj = config.get(CONFIG_DISCOVERY_IP);
        String ip = obj != null ? (String) obj : null;

        obj = config.get(CONFIG_DISCOVERY_HTTP_PORT);
        webPort = obj == null ? Integer.parseInt(System.getProperty("org.osgi.service.http.port"))
                : Integer.parseInt((String) obj);
        try {
            disco = new HueEmulationUpnpServer(PATH + "/description.xml", getUDN(), webPort, ip);
            disco.start();
        } catch (IOException e) {
            logger.error("Could not start UPNP server for discovery", e);
        }

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
        if (path.equals(PATH + "/description.xml")) {
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
        String[] pathParts = path.replace("/api/", "").split("/");

        if (pathParts.length > 0) {
            String userName = pathParts[0];

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

            if (pathParts.length == 1) {
                /**
                 * /api/{username}
                 */
                apiDataStore(req, resp);
            } else {
                String function = pathParts[1];
                if ("lights".equals(function)) {
                    switch (pathParts.length) {
                        case 2:
                            /**
                             * /api/{username}/lights
                             */
                            apiLights(req, resp);
                            break;
                        case 3:
                            /**
                             * /api/{username}/lights/{id}
                             */
                            apiLight(pathParts[2], req, resp);
                            break;
                        case 4:
                            /**
                             * /api/{username}/lights/{id}/state
                             */
                            apiState(pathParts[2], req, resp);
                            break;
                        default:
                            break;
                    }
                } else if ("groups".equals(function)) {
                    switch (pathParts.length) {
                        case 2:
                            /**
                             * /api/{username}/group
                             */
                            emptyResponse(req, resp);
                            break;
                        case 3:
                            /**
                             * /api/{username}/group/{id}
                             */
                            if ("0".equals(pathParts[2])) {
                                apiGroupZero(req, resp);
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    apiServerError(req, resp, HueErrorResponse.NOT_AVAILABLE, "Hue resource not available");
                }
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
            Item item = itemRegistry.getItem(deviceMap.get(new Integer(id)));
            HueState state = gson.fromJson(req.getReader(), HueState.class);
            HSBType hsb = state.toHSBType();
            logger.debug("HuState {}", state);
            logger.debug("HSBType {}", hsb);
            Command command = null;
            if (hsb.getBrightness().intValue() > 0) {
                // if state is on then send HSB, Brightness or ON
                if (item.getAcceptedCommandTypes().contains(HSBType.class)) {
                    command = hsb;
                } else {
                    // try and set the brightness level first
                    command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), hsb.getBrightness().toString());
                    if (command == null) {
                        // if the item does not accept a number or String type, try ON
                        command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), "ON");
                    }
                }
            } else {
                // if state is off, then send 0 or 0FF
                command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), "0");
                if (command == null) {
                    command = TypeParser.parseCommand(item.getAcceptedCommandTypes(), "OFF");
                }
            }

            if (command != null) {
                logger.debug("sending {} to {}", command, item.getName());
                eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), command));
                PrintWriter out = resp.getWriter();
                out.write(String.format(STATE_RESP, id, String.valueOf(state.on)));
                out.close();
            } else {
                logger.error("Item {} does not accept Decimal, ON/OFF or String types", item.getName());
                apiServerError(req, resp, HueErrorResponse.INTERNAL_ERROR,
                        "The Hue device does not respond to that command");
            }
        } catch (ItemNotFoundException e) {
            logger.debug("Item not found: {}", id);
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
            HueDevice device = getHueDevices().get(new Integer(id));
            if (device == null) {
                throw new Exception("Could not find light for id " + id);
            }
            PrintWriter out = resp.getWriter();
            out.write(gson.toJson(device));
            out.close();
        } catch (Exception e) {
            logger.error("error getting light: ", e);
            apiServerError(req, resp, HueErrorResponse.NOT_AVAILABLE, e.getMessage());
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
        // out.write(gson.toJson(getHueDeviceNames()));
        out.write(gson.toJson(getHueDevices()));
        out.close();
    }

    /**
     * Hue API call to get a listing of Group 0
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    public void apiGroupZero(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        List<String> lights = new LinkedList<String>();
        for (Integer key : deviceMap.keySet()) {
            lights.add(key.toString());
        }
        HueState action = new HueState();
        out.write(gson.toJson(new HueGroup("Group 0", lights.toArray(new String[0]), action)));
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
        logger.debug("Create user: {}", user.devicetype);
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
     * Returns a empty ("{}") JSON response
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    public void emptyResponse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        out.write("{}");
        out.close();
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

        String formattedXML = String.format(xmlDoc, address.getHostAddress(), webPort, getUDN());
        resp.setContentType(APPLICATION_XML);
        PrintWriter out = resp.getWriter();
        out.write(formattedXML);
        out.close();
    }

    /**
     * Converts an Item to a HueDevice
     *
     * @param item
     * @return
     *         HueDevice
     */
    private HueDevice itemToDevice(Item item) {
        State itemState = item.getState();
        HueState hueState;
        if (itemState instanceof HSBType) {
            HSBType color = (HSBType) itemState;
            hueState = new HueState(color);
        } else if (itemState instanceof DecimalType) {
            short bri = (short) ((((DecimalType) itemState).intValue() * 255) / 100);
            hueState = new HueState(bri);
        } else if (itemState instanceof OnOffType) {
            short bri = (short) (((OnOffType) itemState) == OnOffType.ON ? 255 : 0);
            hueState = new HueState(bri);
        } else {
            hueState = new HueState((short) 0);
        }

        HueDevice d = new HueDevice(hueState, item.getLabel(), item.getName());
        return d;
    }

    /**
     * Gets and syncs all items tagged for voice.
     *
     * @return
     */
    private synchronized TreeMap<Integer, HueDevice> getHueDevices() {
        TreeMap<Integer, HueDevice> returnMap = new TreeMap<Integer, HueDevice>();
        HashMap<String, Item> taggedItems = new HashMap<String, Item>();

        // if we modify our internal map, persist it to disk
        boolean modified = false;

        // get all tagged items
        for (Item item : itemRegistry.getItems()) {
            for (String tag : item.getTags()) {
                if (ArrayUtils.contains(SUPPORTED_TAGS, tag)) {
                    taggedItems.put(item.getName(), item);
                    if (!deviceMap.containsValue(item.getName())) {
                        // hue devices are assigned a numeric number starting with 1, if a device is
                        // removed that number is not used again. Not sure how high this id can get
                        // not worrying about it here
                        Integer next = deviceMap.size() == 0 ? new Integer(1)
                                : new Integer(deviceMap.lastKey().intValue() + 1);
                        deviceMap.put(next, item.getName());
                        modified = true;
                    }
                    break;
                }
            }
        }

        // clean up removed entries
        for (String itemName : deviceMap.values()) {
            if (!taggedItems.containsKey(itemName)) {
                deviceMap.remove(itemName);
                modified = true;
            }
        }

        // for each entry, lookup the item and convert it to a hue device
        for (Integer key : deviceMap.keySet()) {
            try {
                returnMap.put(key, itemToDevice(itemRegistry.getItem(deviceMap.get(key))));
            } catch (ItemNotFoundException e) {
                logger.warn("Could not find item", e);
            }
        }

        if (modified) {
            JsonWriter writer = null;
            try {
                writer = new JsonWriter(new FileWriter(ITEM_FILE));
                gson.toJson(deviceMap, new TypeToken<Map<Integer, String>>() {
                }.getType(), writer);
            } catch (IOException e) {
                logger.error("Could not persist item cache", e);
            } finally {
                IOUtils.closeQuietly(writer);
            }
        }
        return returnMap;
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
