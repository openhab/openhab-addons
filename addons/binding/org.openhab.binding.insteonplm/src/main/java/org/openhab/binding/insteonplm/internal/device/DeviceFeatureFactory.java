package org.openhab.binding.insteonplm.internal.device;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.openhab.binding.insteonplm.handler.InsteonPLMBridgeHandler;
import org.openhab.binding.insteonplm.internal.utils.Utils.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Methods for creating DeviceFeature classes from the resource xml file. */
public class DeviceFeatureFactory {
    private Logger logger = LoggerFactory.getLogger(InsteonPLMBridgeHandler.class);

    Map<String, FeatureTemplate> m_features;

    public DeviceFeatureFactory() {
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
        if (m_features.containsKey(s)) {
            f = m_features.get(s).build();
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
            ArrayList<FeatureTemplate> features = FeatureTemplateLoader.s_readTemplates(input);
            for (FeatureTemplate f : features) {
                m_features.put(f.getName(), f);
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
        return m_features.containsKey(value);
    }
}
