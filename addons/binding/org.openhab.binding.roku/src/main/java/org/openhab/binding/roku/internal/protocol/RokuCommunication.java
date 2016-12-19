package org.openhab.binding.roku.internal.protocol;

import static org.openhab.binding.roku.RokuBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.roku.internal.RokuState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class RokuCommunication {
    private final Logger logger = LoggerFactory.getLogger(RokuCommunication.class);
    private final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private final String host;
    private final Number port;

    public RokuCommunication(String host, Number port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public Number getPort() {
        return port;
    }

    private StringType getTagName(String tag, Element eElement) {
        return new StringType(eElement.getElementsByTagName(tag).item(0).getTextContent());
    }

    private String getSubTagName(String tag, Element eElement) {
        return eElement.getElementsByTagName(tag).item(0).getAttributes().getNamedItem("id").toString()
                .replace("id=", "").replace("\"", "");
    }

    /**
     * Future Functionality
     * private HashMap<String, String> getAllSubTags(String tag, Element eElement) {
     * HashMap<String, String> map = new HashMap<String, String>();
     * for (int i = 0; i < eElement.getElementsByTagName(tag).getLength(); i++) {
     * String id = eElement.getElementsByTagName(tag).item(i).getAttributes().getNamedItem("id").toString()
     * .replace("id=", "").replace("\"", "");
     * String app = eElement.getElementsByTagName(tag).item(i).getTextContent();
     * map.put(app, id);
     * }
     * return map;
     * }
     */

    public void updateState(RokuState state) throws IOException {
        Document doc = getRequest(ROKU_DEVICE_INFO);
        String[] methodStringArray = { "udn", "serial-number", "device-id", "advertising-id", "vendor-name",
                "model-name", "model-number", "model-region", "wifi-mac", "ethernet-mac", "network-type",
                "user-device-name", "software-version", "software-build", "secure-device", "language", "country",
                "locale", "time-zone", "time-zone-offset", "power-mode", "supports-suspend", "developer-enabled",
                "search-enabled", "voice-search-enabled", "notifications-enabled", "headphones-connected" };
        doc.getDocumentElement().normalize();
        if (doc != null) {
            NodeList nList = doc.getElementsByTagName("device-info");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                logger.debug("Current Element: " + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    for (int ii = 0; ii < methodStringArray.length; ii++) {
                        Element eElement = (Element) nNode;
                        Class<RokuState> aClass = RokuState.class;
                        Field field = null;
                        try {
                            field = aClass.getField(methodStringArray[ii].replace("-", "_"));
                            field.set(state, getTagName(methodStringArray[ii], eElement));
                        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                                | IllegalAccessException e) {
                            logger.error("Method not found {}", methodStringArray[ii].replace("-", "_"));
                        }
                    }
                }
            }
        }
        doc = getRequest(ROKU_ACTIVE_APP);
        doc.getDocumentElement().normalize();
        if (doc != null) {
            NodeList nList = doc.getElementsByTagName("active-app");
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                Element eElement = (Element) nNode;
                logger.debug("Current Element: " + nNode.getNodeName());
                try {
                    state.active_app = getTagName("screensaver", eElement);
                    String app_value = getSubTagName("screensaver", eElement);
                    try {
                        state.active_app_img = new RawType(getData("/query/icon/" + app_value));
                    } catch (Exception e) {
                        logger.debug("Failed to get channel artwork for: {}", e);
                    }
                } catch (NullPointerException e) {
                    state.active_app = getTagName("app", eElement);
                    String app_value = getSubTagName("app", eElement);
                    try {
                        state.active_app_img = new RawType(getData("/query/icon/" + app_value));
                    } catch (Exception e1) {
                        logger.debug("Failed to get channel artwork for: {}", e1);
                    }
                }
            }
        }
        /**
         * Future Functionality
         * doc = getRequest(ROKU_QUERY_APPS);
         * doc.getDocumentElement().normalize();
         * if (doc != null) {
         * NodeList nList = doc.getElementsByTagName("apps");
         * for (int i = 0; i < nList.getLength(); i++) {
         * Node nNode = nList.item(i);
         * Element eElement = (Element) nNode;
         * logger.debug("Current Element: " + nNode.getNodeName());
         * HashMap<String, String> map = getAllSubTags("app", eElement);
         * StringBuffer sb = new StringBuffer();
         * for (String key : map.keySet()) {
         * sb.append(key + " => " + map.get(key) + "\n");
         * }
         * state.application_menu = new StringType(sb.toString());
         * }
         * }
         */
    }

    private Document getRequest(String context) throws IOException {
        String response = processRequest(context);
        logger.debug("HTTP Response: {}", response);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            if (doc.getFirstChild().hasChildNodes() == false) {
                throw new IOException("Could not handle response");
            }
            return doc;
        } catch (Exception e) {
            throw new IOException("Could not handle response", e);
        }
    }

    private String processRequest(String context) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://" + host + ":" + port + context);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            throw new IOException("Could not handle http get", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public byte[] getData(String context) throws Exception {
        URL url = new URL("http://" + host + ":" + port + context);
        URLConnection connection = url.openConnection();
        return IOUtils.toByteArray(connection.getInputStream());
    }
}
