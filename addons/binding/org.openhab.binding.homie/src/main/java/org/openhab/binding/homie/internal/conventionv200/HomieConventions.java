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
    public static final String HOMIE_NODE_VALUE_TOPIC_SUFFIX = "value";
    public static final String HOMIE_NODE_ITEMTYPE_TOPIC_SUFFIX = "itemtype";
    public static final String HOMIE_NODE_UNIT_TOPIC_SUFFIX = "unit";

    /**
     * Optional properties that are not part of the homie convention
     */

    public static final String HOMIE_NODE_ESH_TYPE_PREFIX = "ESH:";

}
