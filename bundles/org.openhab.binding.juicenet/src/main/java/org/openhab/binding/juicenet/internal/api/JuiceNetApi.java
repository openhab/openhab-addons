package org.openhab.binding.juicenet.internal.api;

import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JuiceNetApi} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jeff James - Initial contribution
 */
public class JuiceNetApi {
    private final Logger logger = LoggerFactory.getLogger(JuiceNetApi.class);

    protected String apiToken = "";

    public boolean initialize(String apiToken, ThingUID bridgeUID) throws JuiceNetApiException {
        this.apiToken = apiToken;
        /*
         * httpApi = new RachioHttp(this.apikey);
         * if (initializePersonId() && initializeDevices(bridgeUID) && initializeZones()) {
         * logger.trace("Rachio API initialized");
         * return true;
         * }
         *
         * httpApi = null;
         */
        logger.error("RachioApi.initialize(): API initialization failed!");
        return false;
    } // initialize()

    public boolean getDevices() throws JuiceNetApiException {
        return false;
    }

}
