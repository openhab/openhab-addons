/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.infokeydinrail.internal;

import static org.openhab.binding.infokeydinrail.internal.InfokeyBindingConstants.*;

import java.text.DecimalFormat;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.infokeydinrail.internal.handler.InfokeyDhtHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link InfokeyDhtHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * This GPIO provider implements the DHT 11 / 22 / AM2302 as native device.
 * </p>
 *
 * <p>
 * The DHT 11 / 22 / AM2302 is connected via Custom Rpi Python Server and get results
 * </p>
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
@NonNullByDefault
public class DhtModuleRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static DecimalFormat df = new DecimalFormat("0.00");

    private Thing aThing;
    private String serverIP;
    private InfokeyDhtHandler theHandler;

    public DhtModuleRunnable(InfokeyDhtHandler theHandler, Thing aThing, String serverIP) {
        this.aThing = aThing;
        this.serverIP = serverIP;
        this.theHandler = theHandler;
    }

    @Override
    public void run() {
        Configuration configuration = theHandler.getConfigFile();
        String dataPin = configuration.get(DHT_DATA_PIN).toString();
        String dhtModel = configuration.get(DHT_MODEL).toString();

        String callString = "http://" + serverIP + ":8000/dhtRead/" + dhtModel + "/" + dataPin;

        logger.debug("DHTModuleRunner ---> 0.Show call string {}", callString);

        NetClient aNetClient = new NetClient();
        String jsonResponse = aNetClient.get(callString);

        // logger.debug("1.Updating led value -> json response {}", jsonResponse);

        Gson gson = new Gson(); // Or use new GsonBuilder().create();
        DhtResponse dhtResponse = gson.fromJson(jsonResponse, DhtResponse.class);

        if (dhtResponse != null) {
            Iterator<Channel> it = aThing.getChannels().iterator();
            while (it.hasNext()) {
                Channel ch = it.next();
                // logger.debug("3. {} / {} / {}", ch.toString(), ch.getChannelTypeUID(), ch.getUID());
                if (ch.getUID().toString().contains("#Temp_Celsius")) {
                    theHandler.updateValue(ch.getUID(), df.format(dhtResponse.getTemperatureC()) + " °C");
                } else if (ch.getUID().toString().contains("#Temp_Fahrenheit")) {
                    theHandler.updateValue(ch.getUID(), df.format(dhtResponse.getTemperatureF()) + " °F");
                } else {
                    theHandler.updateValue(ch.getUID(), df.format(dhtResponse.getHumidity()) + " %");
                }
            }
        }
    }
}
