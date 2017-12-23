package org.openhab.binding.knx.internal.client;

import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.knx.handler.StatusUpdateCallback;
import org.openhab.binding.knx.handler.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

@NonNullByDefault
public class IPClient extends KNXClient {

    private final Logger logger = LoggerFactory.getLogger(IPClient.class);

    private static final String MODE_ROUTER = "ROUTER";
    private static final String MODE_TUNNEL = "TUNNEL";

    private final int ipConnectionType;
    private final String ip;
    private final String localSource;
    private final int port;
    @Nullable
    private final InetSocketAddress localEndPoint;
    private final boolean useNAT;

    public IPClient(int ipConnectionType, String ip, String localSource, int port,
            @Nullable InetSocketAddress localEndPoint, boolean useNAT, int autoReconnectPeriod, ThingUID thingUID,
            int responseTimeout, int readingPause, int readRetriesLimit, ScheduledExecutorService knxScheduler,
            StatusUpdateCallback statusUpdateCallback, TypeHelper typeHelper) {
        super(autoReconnectPeriod, thingUID, responseTimeout, readingPause, readRetriesLimit, knxScheduler,
                statusUpdateCallback, typeHelper);
        this.ipConnectionType = ipConnectionType;
        this.ip = ip;
        this.localSource = localSource;
        this.port = port;
        this.localEndPoint = localEndPoint;
        this.useNAT = useNAT;
    }

    @Override
    protected KNXNetworkLink establishConnection() throws KNXException, InterruptedException {
        logger.debug("Establishing connection to KNX bus on {}:{} in mode {}.", ip, port, connectionTypeToString());
        TPSettings settings = new TPSettings(new IndividualAddress(localSource));
        return new KNXNetworkLinkIP(ipConnectionType, localEndPoint, new InetSocketAddress(ip, port), useNAT, settings);
    }

    private String connectionTypeToString() {
        return ipConnectionType == KNXNetworkLinkIP.ROUTING ? MODE_ROUTER : MODE_TUNNEL;
    }

}
