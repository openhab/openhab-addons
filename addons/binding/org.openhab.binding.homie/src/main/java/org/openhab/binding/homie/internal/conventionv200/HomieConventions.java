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

    public final static String STATS_UPTIME_TOPIC_SUFFIX = "$stats/uptime";
    public final static String ONLINE_TOPIC_SUFFIX = "$online";
    public final static String NAME_TOPIC_SUFFIX = "$name";
    public final static String LOCALIP_TOPIC_SUFFIX = "$localip";
    public static final String IMPLEMENTATION_TOPIC_SUFFIX = "$implementation";
    public static final String FIRMWARE_CHECKSUM_TOPIC_SUFFIX = "$fw/checksum";
    public static final String FIRMWARE_VERSION_TOPIC_SUFFIX = "$fw/version";
    public static final String FIRMWARE_NAME_TOPIC_SUFFIX = "$fw/name";
    public static final String STATS_INTERVAL_TOPIC_SUFFIX = "$stats/interval";
    public static final String STATS_SIGNAL_TOPIC_SUFFIX = "$stats/signal";
    public static final String HOMIE_TOPIC_SUFFIX = "$homie";

    public static final String HOMIE_NODE_TYPE_ANNOUNCEMENT_TOPIC_SUFFIX = "$type";
    public static final String HOMIE_NODE_PROPERTYLIST_ANNOUNCEMENT_TOPIC_SUFFIX = "$properties";

}
