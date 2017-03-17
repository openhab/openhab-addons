package org.openhab.binding.insteonplm.internal.device;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonPLMBridgeHandler;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.openhab.binding.insteonplm.internal.utils.Utils.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;

/**
 * Methods for creating DeviceFeature classes from the resource xml file.
 *
 * @author David Bennett - Initial contribution
 */
public class DeviceFeatureFactory {
    private Logger logger = LoggerFactory.getLogger(InsteonPLMBridgeHandler.class);

    Map<String, DeviceFeatureBuilder> features;

    public DeviceFeatureFactory() {
        features = Maps.newHashMap();
        // read features from xml file and store them in a map
        InputStream input = DeviceFeature.class.getResourceAsStream("/device_features.xml");
        readFeatureTemplates(input);
    }

    /**
     * Factory method for creating DeviceFeatures.
     *
     * @param s The name of the device feature to create.
     * @return The newly created DeviceFeature, or null if requested DeviceFeature does not exist.
     */
    public DeviceFeature makeDeviceFeature(String s) {
        DeviceFeature f = null;
        if (features.containsKey(s)) {
            f = features.get(s).build();
        } else {
            logger.error("unimplemented feature requested: {}", s);
        }
        return f;
    }

    /**
     * Reads the features templates from an input stream and puts them in global map
     *
     * @param input the input stream from which to read the feature templates
     */
    private void readFeatureTemplates(InputStream input) {
        try {
            ArrayList<DeviceFeatureBuilder> featuresFromTemplate = readTemplates(input);
            for (DeviceFeatureBuilder f : featuresFromTemplate) {
                features.put(f.getName(), f);
            }
        } catch (IOException e) {
            logger.error("IOException while reading device features", e);
        } catch (ParsingException e) {
            logger.error("Parsing exception while reading device features", e);
        }
    }

    /**
     * Figures out if this feature name exists in the factory or not.
     *
     * @param value The feature to lookup
     * @return true/false
     */
    public boolean isDeviceFeature(String value) {
        return features.containsKey(value);
    }

    /**
     * Reads in the xml file for the templates into this factory.
     *
     * @param input The input file to read from
     * @return the array of device feature builders
     * @throws IOException
     * @throws ParsingException
     */
    private ArrayList<DeviceFeatureBuilder> readTemplates(InputStream input) throws IOException, ParsingException {
        ArrayList<DeviceFeatureBuilder> features = new ArrayList<DeviceFeatureBuilder>();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Parse it!
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            NodeList nodes = root.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    if (e.getTagName().equals("feature")) {
                        DeviceFeatureBuilder builder = parseFeature(e);
                        if (builder != null) {
                            features.add(builder);
                        }
                    }
                }
            }
        } catch (SAXException e) {
            throw new ParsingException("Failed to parse XML!", e);
        } catch (ParserConfigurationException e) {
            throw new ParsingException("Got parser config exception! ", e);
        }
        return features;
    }

    private DeviceFeatureBuilder parseFeature(Element e) throws ParsingException {
        String name = e.getAttribute("name");
        if (e.getAttribute("x10") != null) {
            return null;
        } else {
            DeviceFeatureBuilder feature = new DeviceFeatureBuilder();
            feature.setName(name);
            feature.setTimeout(e.getAttribute("timeout"));

            NodeList nodes = e.getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) node;
                    if (child.getTagName().equals("message-handler")) {
                        parseMessageHandler(child, feature);
                    } else if (child.getTagName().equals("command-handler")) {
                        parseCommandHandler(child, feature);
                    } else if (child.getTagName().equals("message-dispatcher")) {
                        parseMessageDispatcher(child, feature);
                    }
                }
            }

            return feature;
        }
    }

    private HandlerEntry makeHandlerEntry(Element e) throws ParsingException {
        String handler = e.getTextContent();
        if (handler == null) {
            throw new ParsingException("Could not find Handler for: " + e.getTextContent());
        }

        NamedNodeMap attributes = e.getAttributes();
        HashMap<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node n = attributes.item(i);
            params.put(n.getNodeName(), n.getNodeValue());
        }
        return new HandlerEntry(handler, params);
    }

    private void parseMessageHandler(Element e, DeviceFeatureBuilder f) throws DOMException, ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        if (e.getAttribute("default").equals("true")) {
            f.setDefaultMessageHandler(he);
        } else {
            String attr = e.getAttribute("cmd");
            StandardInsteonMessages cmdParsed = StandardInsteonMessages.valueOf(attr);
            f.addMessageHandler(cmdParsed, he);
        }
    }

    private void parseCommandHandler(Element e, DeviceFeatureBuilder f) throws ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        if (e.getAttribute("default").equals("true")) {
            f.setDefaultCommandHandler(he);
        } else {
            Class<? extends Command> command = parseCommandClass(e.getAttribute("command"));
            f.addCommandHandler(command, he);
        }
    }

    private void parseMessageDispatcher(Element e, DeviceFeatureBuilder f) throws DOMException, ParsingException {
        HandlerEntry he = makeHandlerEntry(e);
        f.setMessageDispatcher(he);
        if (he.getHandlerName() == null) {
            throw new ParsingException("Could not find MessageDispatcher for: " + e.getTextContent());
        }
    }

    private Class<? extends Command> parseCommandClass(String c) throws ParsingException {
        if (c.equals("OnOffType")) {
            return OnOffType.class;
        } else if (c.equals("PercentType")) {
            return PercentType.class;
        } else if (c.equals("DecimalType")) {
            return DecimalType.class;
        } else if (c.equals("IncreaseDecreaseType")) {
            return IncreaseDecreaseType.class;
        } else {
            throw new ParsingException("Unknown Command Type");
        }
    }

    /**
     * Factory method for creating handlers of a given name using java reflection
     *
     * @param ph the name of the handler to create
     * @param f the feature for which to create the handler
     * @return the handler which was created
     */
    public <T extends PollHandler> T makePollHandler(String pollHandlerClass) {
        String cname = PollHandler.class.getName() + "$" + pollHandlerClass;
        try {
            Class<?> c = Class.forName(cname);
            @SuppressWarnings("unchecked")
            Class<? extends T> dc = (Class<? extends T>) c;
            T phc = dc.getDeclaredConstructor(PollHandler.class).newInstance(cname);
            return phc;
        } catch (Exception e) {
            logger.error("error trying to create message handler: {}", pollHandlerClass, e);
        }
        return null;
    }
}
