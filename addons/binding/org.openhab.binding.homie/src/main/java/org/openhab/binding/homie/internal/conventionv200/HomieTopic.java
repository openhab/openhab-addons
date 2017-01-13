package org.openhab.binding.homie.internal.conventionv200;

import static org.openhab.binding.homie.internal.conventionv200.TopicParser.*;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

/**
 * This class represents a parsed MQTT homie topic
 *
 * @author Michael Kolb - Initial Contribution
 *
 */
public class HomieTopic {

    private final String deviceId;
    private final String nodeId;
    private final String subproperty;
    private final String propertyName;
    private final String internalPropertyName;

    public HomieTopic(Matcher m) {

        subproperty = m.group(MATCHGROUP_INTERNAL_SUBPROPERTY_NAME);
        propertyName = m.group(MATCHGROUP_PROPERTY_NAME);
        internalPropertyName = m.group(MATCHGROUP_INTERNAL_PROPERTY_NAME);
        deviceId = m.group(MATCHGROUP_DEVICEID_NAME);
        nodeId = m.group(MATCHGROUP_NODEID_NAME);
    }

    /**
     * Check if this topic is a topic published by the device itself
     *
     * @return
     */
    public boolean isDeviceProperty() {
        return StringUtils.isBlank(nodeId);
    }

    /**
     * Check if this topic is a topic published by a node of a device
     *
     * @return
     */
    public boolean isNodeProperty() {
        return StringUtils.isNotBlank(nodeId);
    }

    /**
     * Check if this topic is an internal topic (all topics marked with $)
     *
     * @return
     */
    public boolean isInternalProperty() {
        return StringUtils.isNotBlank(internalPropertyName);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getSubproperty() {
        return subproperty;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getInternalPropertyName() {
        return internalPropertyName;
    }

    /**
     * Get the combined name of an internal property (property name + subproperty)
     *
     * @return
     */
    public String getCombinedInternalPropertyName() {
        String result = internalPropertyName;
        if (StringUtils.isNotBlank(subproperty)) {
            result += "/" + subproperty;
        }
        return result;
    }

}
