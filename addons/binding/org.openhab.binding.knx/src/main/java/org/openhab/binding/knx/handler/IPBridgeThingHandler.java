package org.openhab.binding.knx.handler;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.binding.knx.KNXBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX/IP Gateway, that either acts a a
 * conduit for other {@link KNXBaseThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 */
public class IPBridgeThingHandler extends KNXBridgeBaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(IPBridgeThingHandler.class);

    // List of all Configuration parameters
    public static final String IP_ADDRESS = "ipAddress";
    public static final String IP_CONNECTION_TYPE = "ipConnectionType";
    public static final String LOCAL_IP = "localIp";
    public static final String PORT_NUMBER = "portNumber";
    public static final String LOCAL_SOURCE_ADDRESS = "localSourceAddr";

    // the ip connection type for connecting to the KNX bus. Could be either TUNNEL or ROUTING
    private static int ipConnectionType;

    // the ip address to use for connecting to the KNX bus
    private static String ip;

    // the group address used within the KNX bus
    private static String localsource;

    public IPBridgeThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    public IPBridgeThingHandler() {
        // Default constructor required in order to have OSGi Declarative Services working
        super(null, null);
    }

    @Override
    public void initialize() {

        ip = (String) getConfig().get(IP_ADDRESS);
        localsource = (String) getConfig().get(LOCAL_SOURCE_ADDRESS);

        String connectionTypeString = (String) getConfig().get(IP_CONNECTION_TYPE);
        if (StringUtils.isNotBlank(connectionTypeString)) {
            if ("TUNNEL".equals(connectionTypeString)) {
                ipConnectionType = KNXNetworkLinkIP.TUNNELING;
            } else if ("ROUTER".equals(connectionTypeString)) {
                ipConnectionType = KNXNetworkLinkIP.ROUTING;
                if (StringUtils.isBlank(ip)) {
                    ip = KNXBindingConstants.DEFAULT_MULTICAST_IP;
                }
                if (StringUtils.isBlank(localsource)) {
                    localsource = KNXBindingConstants.DEFAULT_LOCAL_SOURCE_ADDRESS;
                }
            } else {
                logger.warn("unknown IP connection type '" + connectionTypeString
                        + "'! Known types are either 'TUNNEL' or 'ROUTER'");
            }
        } else {
            ipConnectionType = KNXNetworkLinkIP.TUNNELING;
        }

        super.initialize();
    }

    @Override
    public void establishConnection() throws KNXException {

        try {
            InetSocketAddress localEndPoint = null;
            if (StringUtils.isNotBlank((String) getConfig().get(LOCAL_IP))) {
                localEndPoint = new InetSocketAddress((String) getConfig().get(LOCAL_IP), 0);
            } else {
                try {
                    InetAddress localHost = InetAddress.getLocalHost();
                    localEndPoint = new InetSocketAddress(localHost, 0);
                } catch (UnknownHostException uhe) {
                    logger.warn(
                            "Couldn't find an IP address for this host. Please check the .hosts configuration or use the 'localIp' parameter to configure a valid IP address.");
                }
            }

            if (logger.isInfoEnabled()) {
                if (link instanceof KNXNetworkLinkIP) {
                    String ipConnectionTypeString = ipConnectionType == KNXNetworkLinkIP.ROUTING ? "ROUTER" : "TUNNEL";
                    logger.info("Establishing connection to KNX bus on {} in mode {}.",
                            ip + ":" + ((BigDecimal) getConfig().get(PORT_NUMBER)).intValue(), ipConnectionTypeString);
                }
            }

            link = new KNXNetworkLinkIP(ipConnectionType, localEndPoint,
                    new InetSocketAddress(ip, ((BigDecimal) getConfig().get(PORT_NUMBER)).intValue()), false,
                    new TPSettings(new IndividualAddress(localsource)));

        } catch (Exception e) {
            logger.error("Error connecting to KNX bus: {}", e.getMessage());
            throw new KNXException(
                    "Connection to KNX bus on " + ip + ":" + ((BigDecimal) getConfig().get(PORT_NUMBER)).intValue()
                            + " in mode " + (String) getConfig().get(IP_CONNECTION_TYPE) + " could not be established");
        }

    }
}
