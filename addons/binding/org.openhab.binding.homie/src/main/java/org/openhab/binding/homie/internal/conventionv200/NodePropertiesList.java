package org.openhab.binding.homie.internal.conventionv200;

import static org.openhab.binding.homie.internal.conventionv200.NodePropertiesListAnnouncementParser.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Class represents parsed properties of a node announcement message (e.g. homie/686f6d6965/temperature/$properties)
 * 
 * @author Michael Kolb - initial contribution
 *
 */
public class NodePropertiesList {
    private Map<String, Boolean> properties = new HashMap<>();

    public NodePropertiesList(Matcher m) {
        while (m.find()) {
            String propname = m.group(MATCHGROUP_PROPERTYNAME_NAME);
            boolean settable = m.group(MATCHGROUP_SETTABLE_NAME) == null;
            properties.put(propname, settable);
        }
    }

    public Set<String> getProperties() {
        return properties.keySet();
    }

    public boolean isPropertySettable(String property) {
        return properties.get(property);
    }
}
