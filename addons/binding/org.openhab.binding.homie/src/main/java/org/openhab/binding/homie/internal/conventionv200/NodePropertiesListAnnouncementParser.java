package org.openhab.binding.homie.internal.conventionv200;

import static org.openhab.binding.homie.internal.conventionv200.HomieConventions.ID_PATTERN;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
