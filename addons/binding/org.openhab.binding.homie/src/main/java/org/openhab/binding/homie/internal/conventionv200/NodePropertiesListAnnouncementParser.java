/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.internal.conventionv200;

import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.ID_PATTERN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for property announcement messages (e.g. the message sent to homie/686f6d6965/temperature/$properties)
 *
 * @author Michael Kolb - Initial Contribution
 *
 */
public class NodePropertiesListAnnouncementParser {

    protected final static String MATCHGROUP_PROPERTYNAME_NAME = "propname";
    protected final static String MATCHGROUP_SETTABLE_NAME = "settable";

    public final static String PATTERN_NODE_PROPERTY_TYPE_PAYLOAD = String.format("((?<%s>%s)(:(?<%s>settable))?)",
            MATCHGROUP_PROPERTYNAME_NAME, ID_PATTERN, MATCHGROUP_SETTABLE_NAME);
    private final Pattern pattern;

    public NodePropertiesListAnnouncementParser() {
        pattern = Pattern.compile(PATTERN_NODE_PROPERTY_TYPE_PAYLOAD);
    }

    public NodePropertiesList parse(String messageContent) {
        Matcher m = pattern.matcher(messageContent);
        return new NodePropertiesList(m);
    }
}
