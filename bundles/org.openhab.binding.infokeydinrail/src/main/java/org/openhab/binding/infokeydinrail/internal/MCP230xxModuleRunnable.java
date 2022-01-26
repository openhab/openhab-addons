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

import java.text.DecimalFormat;

import org.openhab.binding.infokeydinrail.internal.handler.InfokeyOptoDinV1Handler;
import org.openhab.binding.infokeydinrail.internal.handler.InfokeyRelayOptoDinV1Handler;
import org.openhab.core.thing.Thing;

import com.google.gson.Gson;
import com.pi4j.io.gpio.PinState;

/**
 * The {@link MCP230xxModuleRunnable} is responsible for handling commands, which are
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
public class MCP230xxModuleRunnable implements Runnable {

    // private final Logger logger = LoggerFactory.getLogger(getClass());
    private static DecimalFormat df = new DecimalFormat("0.00");

    private Thing aThing;
    private String serverIP;
    private Integer mcpModel;
    private Integer bus;
    private String address;
    private Object theHandler;

    public MCP230xxModuleRunnable(Object theHandler, Thing aThing, String serverIP, Integer mcpModel, Integer bus,
            String address) {
        this.aThing = aThing;
        this.serverIP = serverIP;
        this.mcpModel = mcpModel;
        this.bus = bus;
        this.address = address;
        this.theHandler = theHandler;
    }

    @Override
    public void run() {
        String jsonString = "";
        if (theHandler instanceof InfokeyRelayOptoDinV1Handler) {
            jsonString = "{\"pinsList\":"
                    + new Gson().toJson(((InfokeyRelayOptoDinV1Handler) theHandler).getInputPins()) + "}";
        } else if (theHandler instanceof InfokeyOptoDinV1Handler) {
            jsonString = "{\"pinsList\":" + new Gson().toJson(((InfokeyOptoDinV1Handler) theHandler).getInputPins())
                    + "}";
        }

        // logger.debug("-1.Show jsonString {}", jsonString);

        if (jsonString != null && jsonString.trim().length() > 0) {
            String callString = "http://" + serverIP + ":8000/" + (mcpModel == 17 ? "mcp23017_read" : "mcp23008_read")
                    + "/" + bus + "/0x" + address;

            // logger.debug("0.Show call string {}, jsonString {}", callString, jsonString);

            NetClient aNetClient = new NetClient();
            String jsonResponse = aNetClient.post(callString, jsonString);

            // logger.debug("1.Updating led value -> json response {}", jsonResponse);

            Gson gson = new Gson(); // Or use new GsonBuilder().create();
            Mcp230xxResponse[] mcpResponse = gson.fromJson(jsonResponse, Mcp230xxResponse[].class);

            if (mcpResponse != null && mcpResponse.length > 0) {

                if (theHandler instanceof InfokeyRelayOptoDinV1Handler) {
                    InfokeyRelayOptoDinV1Handler instance = ((InfokeyRelayOptoDinV1Handler) theHandler);

                    for (Mcp230xxResponse aResponse : mcpResponse) {
                        instance.readInputPinState(aResponse.getValue() ? PinState.LOW : PinState.HIGH,
                                instance.getChannelFromPin(aResponse.getPinNo()));
                    }
                } else if (theHandler instanceof InfokeyOptoDinV1Handler) {
                    InfokeyOptoDinV1Handler instance = ((InfokeyOptoDinV1Handler) theHandler);

                    for (Mcp230xxResponse aResponse : mcpResponse) {
                        instance.readInputPinState(aResponse.getValue() ? PinState.LOW : PinState.HIGH,
                                instance.getChannelFromPin(aResponse.getPinNo()));
                    }
                }

            }
        }
    }
}
