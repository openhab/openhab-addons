/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.internal.conventionv200;

import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.*;
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

    public boolean isESHNodeUnit() {
        return isNodeProperty() && !isInternalProperty() && StringUtils.equals(getPropertyName(), ESH_UNIT_TOPIC);
    }

    /**
     * Check if this is a $type announcement
     *
     * @return
     */
    public boolean isNodeTypeAnnouncement() {
        return isNodeProperty() && isInternalProperty()
                && internalPropertyName.endsWith(HOMIE_NODE_TYPE_ANNOUNCEMENT_TOPIC_SUFFIX);
    }

    public boolean isNodePropertyAnnouncement() {
        return isNodeProperty() && isInternalProperty()
                && internalPropertyName.endsWith(HOMIE_NODE_PROPERTYLIST_ANNOUNCEMENT_TOPIC_SUFFIX);
    }

    /**
     * Get the combined name of an node property (nodeid + property name + subproperty)
     *
     * @return
     */
    public String getCombinedNodePropertyName() {

        String result = isNodeProperty() ? getNodeId() + "/" : "";
        if (isInternalProperty()) {
            result += getCombinedInternalPropertyName();
        } else {
            result += propertyName;
            if (StringUtils.isNotBlank(subproperty)) {
                result += "/" + subproperty;
            }
        }

        return result;
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

    public boolean isESHDescription() {
        return isNodeProperty() && !isInternalProperty() && StringUtils.equals(getPropertyName(), ESH_DESC_TOPIC);
    }

    public boolean isESHMin() {
        return isNodeProperty() && !isInternalProperty() && StringUtils.equals(getPropertyName(), ESH_MIN_TOPIC);
    }

    public boolean isESHMax() {
        return isNodeProperty() && !isInternalProperty() && StringUtils.equals(getPropertyName(), ESH_MAX_TOPIC);
    }

    public boolean isESHStep() {
        return isNodeProperty() && !isInternalProperty() && StringUtils.equals(getPropertyName(), ESH_STEP_TOPIC);
    }

    public boolean isESHItemType() {
        return isNodeProperty() && !isInternalProperty() && StringUtils.equals(getPropertyName(), ESH_ITEMTYPE_TOPIC);
    }

    public boolean isESHValue() {
        return isNodeProperty() && !isInternalProperty() && StringUtils.equals(getPropertyName(), ESH_VALUE_TOPIC)
                && StringUtils.isBlank(subproperty);
    }

    public boolean isInternalDeviceProperty() {
        return isInternalProperty() && isDeviceProperty();
    }

}
