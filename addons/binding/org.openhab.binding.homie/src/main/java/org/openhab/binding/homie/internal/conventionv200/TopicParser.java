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

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.homie.HomieBindingConstants;

/**
 * Parser for Homie MQTT Topics
 *
 * @author Michael Kolb - Initial Contribution
 *
 */
public class TopicParser {

    protected final static String MATCHGROUP_INTERNAL_PROPERTY_NAME = "intproperty";
    protected final static String MATCHGROUP_INTERNAL_SUBPROPERTY_NAME = "intsubproperty";
    protected final static String MATCHGROUP_PROPERTY_NAME = "property";
    protected final static String MATCHGROUP_NODEID_NAME = "nodeid";
    protected final static String MATCHGROUP_DEVICEID_NAME = "deviceid";

    protected final static String DEVICEID_PATTERN = String.format("(?<%s>%s)", MATCHGROUP_DEVICEID_NAME, ID_PATTERN);
    protected final static String NODEID_PATTERN = String.format("(?<%s>%s)", MATCHGROUP_NODEID_NAME, ID_PATTERN);
    protected final static String PROPERTY_PATTERN = String.format("(?<%s>%s)", MATCHGROUP_PROPERTY_NAME, ID_PATTERN);
    protected final static String INTERNAL_SUBPROPERTY_PATTERN = String.format("(?<%s>%s)",
            MATCHGROUP_INTERNAL_SUBPROPERTY_NAME, ID_PATTERN);
    protected final static String INTERNAL_PROPERTY_PATTERN = String.format("(?<%s>\\$%s)",
            MATCHGROUP_INTERNAL_PROPERTY_NAME, ID_PATTERN);
    /**
     * Regex to match a mqtt topic and extract matched
     */
    protected final static String TOPIC_PATTERN = String.format("\\/%s(\\/%s)?\\/(%s|%s)(\\/%s)?$", DEVICEID_PATTERN,
            NODEID_PATTERN, PROPERTY_PATTERN, INTERNAL_PROPERTY_PATTERN, INTERNAL_SUBPROPERTY_PATTERN);
    private final Pattern pattern;

    private static Pattern getPattern(String basetopic) {
        if (basetopic.endsWith(HomieBindingConstants.MQTT_TOPIC_SEPARATOR)) {
            basetopic = StringUtils.substring(basetopic, -1);
        }
        String spattern = String.format("^%s%s", basetopic, TOPIC_PATTERN);
        return Pattern.compile(spattern);
    }

    public TopicParser(String basetopic) {
        pattern = getPattern(basetopic);
    }

    /**
     * Parse a topic for homie information
     *
     * @param topic The topic to parse
     * @return An object representing all information contained in the topic
     * @throws ParseException Thrown if the given topic does not complie to the homie specification
     */
    public HomieTopic parse(String topic) throws ParseException {
        Matcher m = pattern.matcher(topic);
        if (!m.matches()) {
            throw new ParseException("Topic '" + topic + "' is does not complie to homie specification 2.0.0", 0);
        }
        return new HomieTopic(m);
    }

}
