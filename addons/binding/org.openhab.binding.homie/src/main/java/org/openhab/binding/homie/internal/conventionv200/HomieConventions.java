package org.openhab.binding.homie.internal.conventionv200;

/**
 * Homie Convention Version 2.0.0
 *
 * @author Michael Kolb
 *
 */
public class HomieConventions {

    /**
     * Pattern to match the ID Format of homie
     */
    protected final static String ID_PATTERN = "(?!-)([a-z0-9\\-]+)(?<!-)";
    /**
     * Topic suffix which contents will be used to name a discovered device
     */
    public final static String NAME_TOPIC_SUFFIX = "$name";

    public static final String HOMIE_NODE_TYPE_ANNOUNCEMENT_TOPIC_SUFFIX = "$type";
    public static final String HOMIE_NODE_PROPERTYLIST_ANNOUNCEMENT_TOPIC_SUFFIX = "$properties";

}
