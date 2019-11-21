package org.openhab.binding.boschshc.internal;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoschSHCBridgeHandler extends BaseBridgeHandler {

    private HttpClient httpClient;

    public BoschSHCBridgeHandler(Bridge bridge) {
        super(bridge);

        // TODO Make this an asynchronous request
        // TODO Don't think we need to disable all these checks here.

        // Instantiate and configure the SslContextFactory
        // SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        SslContextFactory sslContextFactory = new SslContextFactory(true); // Accept all certificates

        // Keystore for managing the keys that have been used to pair with the SHC
        // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
        sslContextFactory.setKeyStorePath("/home/skaestle/projects/smart-home/bosch/keystore");
        sslContextFactory.setKeyStorePassword("123456");

        // Bosch is using a self signed certificate
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        // Instantiate HttpClient with the SslContextFactory
        this.httpClient = new HttpClient(sslContextFactory);

        try {
            this.httpClient.start();
        } catch (Exception e) {
            logger.warn("Failed to start http client", e);
        }

        this.getRooms();
        this.getDevices();
        this.subscribe();
        this.longPoll();
    }

    private final Logger logger = LoggerFactory.getLogger(BoschSHCBridgeHandler.class);

    @Override
    public void initialize() {

        config = getConfigAs(BoschSHCBridgeConfiguration.class);
        logger.warn("Initializating bridge: {}", config.ipAddress);

        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {

            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("Handle command on bridge: {}", config.ipAddress);

    }

    private BoschSHCBridgeConfiguration config;
}
