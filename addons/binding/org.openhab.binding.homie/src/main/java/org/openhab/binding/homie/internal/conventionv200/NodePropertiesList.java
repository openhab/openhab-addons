/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
