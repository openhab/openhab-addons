/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eightdevices.handler;

import static org.openhab.binding.eightdevices.EightDevicesBindingConstants.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.cache.ExpiringCacheMap;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.eightdevices.connection.EightDevicesConnection;
import org.openhab.binding.eightdevices.embeddedservlet.ExampleServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EightDevicesHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Nedas - Initial contribution
 */
@NonNullByDefault
public class EightDevicesHandler extends BaseThingHandler {

    private static final String UID = "uid";
    private static final String INTERVAL = "interval";

    private final Logger logger = LoggerFactory.getLogger(EightDevicesHandler.class);
    private static Boolean callbackSet = false;

    private final EightDevicesConnection connection = new EightDevicesConnection();

    private String uid = "";
    private String interval = "";

    private static final int CACHE_EXPIRY = 45 * 1000; // 45s
    // private final ExpiringCacheMap<String, String> cache = new ExpiringCacheMap<>(CACHE_EXPIRY);

    static ExpiringCacheMap<String, ChannelUID> ReqAsyncResponses = new ExpiringCacheMap<String, ChannelUID>(
            CACHE_EXPIRY);
    static ExampleServlet Servlet = new ExampleServlet();

    public EightDevicesHandler(Thing thing) {
        super(thing);
        Servlet.addAsyncListener(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String sensorType;
        String[] splitArr;
        if (command instanceof RefreshType) {
            splitArr = channelUID.getThingUID().toString().split(":");
            sensorType = splitArr[1];
            try {
                if (sensorType.equals("s3700")) {
                    handle3700(channelUID);
                }
                if (sensorType.equals("s3800")) {
                    handle3800(channelUID);
                }
                if (sensorType.equals("s4400")) {
                    handle4400(channelUID);
                }
                if (sensorType.equals("s4500")) {
                    handle4500(channelUID);
                }
            } catch (Exception e) {
                System.out.println(sensorType + " ERROR");
                updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    public void handle3700(ChannelUID channelUID) {
        String data;
        String[] splitArr;
        if (channelUID.getId().equals(CHANNEL_ACTIVE_POWER)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3305/1/5800");
            splitArr = data.split("\"");
            ReqAsyncResponses.put(splitArr[3], () -> channelUID);
        }
        if (channelUID.getId().equals(CHANNEL_ACTIVE_ENERGY)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3305/1/5805");
            splitArr = data.split("\"");
            ReqAsyncResponses.put(splitArr[3], () -> channelUID);
        }
        if (channelUID.getId().equals(CHANNEL_REACTIVE_POWER)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3305/1/5810");
            splitArr = data.split("\"");
            ReqAsyncResponses.put(splitArr[3], () -> channelUID);
        }
        if (channelUID.getId().equals(CHANNEL_REACTIVE_ENERGY)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3305/1/5815");
            splitArr = data.split("\"");
            ReqAsyncResponses.put(splitArr[3], () -> channelUID);
        }
        if (channelUID.getId().equals(CHANNEL_RELAY)) {
            System.out.println("REALAY");
            System.out.println("REALAY");
            System.out.println("REALAY");
        }
    }

    public void handle3800(ChannelUID channelUID) {
        String data = "";
        String[] splitArr;
        if (channelUID.getId().equals(CHANNEL_TEMPERATURE)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3303/0/5700");
        }
        if (channelUID.getId().equals(CHANNEL_HUMIDITY)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3304/0/5700");
        }
        splitArr = data.split("\"");
        ReqAsyncResponses.put(splitArr[3], () -> channelUID);
    }

    public void handle4400(ChannelUID channelUID) {
        String data = "";
        String[] splitArr;
        if (channelUID.getId().equals(CHANNEL_TEMPERATURE)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3303/0/5700");
        }
        if (channelUID.getId().equals(CHANNEL_MAGNETIC_FIELD)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3200/0/5500");
        }
        if (channelUID.getId().equals(CHANNEL_MAGNETIC_COUNTER)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3200/0/5501");
        }
        splitArr = data.split("\"");
        ReqAsyncResponses.put(splitArr[3], () -> channelUID);
    }

    public void handle4500(ChannelUID channelUID) {
        String data = "";
        String[] splitArr;
        if (channelUID.getId().equals(CHANNEL_VOLTAGE)) {
            data = connection.getResponseFromQuery("endpoints/" + uid + "/3202/0/5600");
        }
        splitArr = data.split("\"");
        ReqAsyncResponses.put(splitArr[3], () -> channelUID);
    }

    public void CheckIfConnected(String uid) {
        String data = "";
        String[] splitArr;
        // JSONObject json;
        data = connection.getResponseFromQuery("endpoints/");
        // Object obj = JSONValue.parse(data);
        // JSONArray finalResult = (JSONArray) obj;
        // System.out.println(finalResult);
        /*
         * JSONParser parser = new JSONParser();
         * try {
         * json = (JSONObject) parser.parse(data);
         * System.out.println(json);
         * } catch (ParseException e) {
         * // TODO Auto-generated catch block
         * e.printStackTrace();
         * }
         */
        // splitArr = data.split("\"");
        // if (Arrays.asList(splitArr).contains(uid)) {
        // connected = true;
        // }
        // return false;
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        uid = config.get(UID).toString();
        interval = config.get(INTERVAL).toString();
        if (callbackSet == false) {
            connection.SetCallback();
            Server server = new Server(5727);
            ServletContextHandler handler = new ServletContextHandler(server, "/notification");
            handler.addServlet(ExampleServlet.class, "/");
            try {
                server.start();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            callbackSet = true;
        }
        CheckIfConnected(uid);
        sendAutomaticRequests(getThing().getChannels());
        updateStatus(ThingStatus.ONLINE);
    }

    public void handleAsync(String id, String status, String payload) {
        if (ReqAsyncResponses.containsKey(id)) {
            if (status.equals("200")) {
                updateState(ReqAsyncResponses.get(id), getMeasurement(payload));
            }
        }
    }

    public void sendAutomaticRequests(java.util.List<Channel> channels) {
        ScheduledFuture<?> refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            String sensorType;
            String[] splitArr;
            ChannelUID channelUID;
            for (int i = 0; i < channels.size(); i++) {
                channelUID = channels.get(i).getUID();
                splitArr = channelUID.getThingUID().toString().split(":");
                sensorType = splitArr[1];
                try {
                    if (sensorType.equals("s3700")) {
                        handle3700(channelUID);
                    }
                    if (sensorType.equals("s3800")) {
                        handle3800(channelUID);
                    }
                    if (sensorType.equals("s4400")) {
                        handle4400(channelUID);
                    }
                    if (sensorType.equals("s4500")) {
                        handle4500(channelUID);
                    }

                } catch (Exception e) {
                    System.out.println(sensorType + " ERROR");
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }, 0, Integer.parseInt(interval), TimeUnit.SECONDS);
    }

    private State getMeasurement(String payload) {
        if (payload != null) {
            byte[] decoded = Base64.getDecoder().decode(payload);
            String myString = String.format("%x", new BigInteger(1, decoded)).substring(6);
            Long in = Long.parseLong(myString, 16);
            Float f = Float.intBitsToFloat(in.intValue());
            return new DecimalType(BigDecimal.valueOf(f));
        }
        return UnDefType.UNDEF;
    }

}