/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.cbus.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import org.eclipse.smarthome.core.common.ThreadPoolManager;

import com.daveoxley.cbus.CGateConnectException;
import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.CGateInterface;
import com.daveoxley.cbus.CGateSession;
import com.daveoxley.cbus.CGateThreadPool;
import com.daveoxley.cbus.CGateThreadPoolExecutor;
import com.daveoxley.cbus.events.EventCallback;
import com.daveoxley.cbus.status.StatusChangeCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jdt.annotation.Nullable;


/**
 * The {@link CBusCGateHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
    final class CBusThreadPool extends CGateThreadPool
    {
        public class CBusThreadPoolExecutor extends CGateThreadPoolExecutor
        {

            private final ThreadPoolExecutor thread_pool;

            public CBusThreadPoolExecutor(String poolName)
            {
                    thread_pool = (ThreadPoolExecutor) ThreadPoolManager.getPool(poolName);
            }

            protected void execute(Runnable runnable)
            {
                    thread_pool.execute(runnable);
            }
        }
        public CBusThreadPool()
        {}
        private Map<String, CGateThreadPoolExecutor> m_executorMap= new HashMap<String, CGateThreadPoolExecutor>();
        protected  CGateThreadPoolExecutor CreateExecutor(String name)
        {
            CGateThreadPoolExecutor executor = m_executorMap.get(name);
            if (executor != null)
            {
                    return executor;
            }
            executor =  new CBusThreadPoolExecutor(name);
            m_executorMap.put(name, executor);
            return executor;
        }
    }

public class CBusCGateHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(CBusCGateHandler.class);

    private @Nullable InetAddress ipAddress = null;

    public @Nullable CGateSession cGateSession = null;

    private @Nullable Future<?> keepAliveFuture = null; 

    private final ExecutorService threadPool = ThreadPoolManager.getPool("CBusCGateHandler-Helper");

    public CBusCGateHandler(Bridge br) {
        super(br);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);
        logger.debug("Initializing CGate Bridge handler.");
        Configuration config = getThing().getConfiguration();
        String ipAddress = (String) config.get(CBusBindingConstants.PROPERTY_IP_ADDRESS);

        if ("127.0.0.1".equals(ipAddress) || "localhost".equals(ipAddress)) {
            this.ipAddress = InetAddress.getLoopbackAddress();
        } else {
            try {
                this.ipAddress = InetAddress.getByName(ipAddress);
            } catch (UnknownHostException e1) {
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "IP Address not resolvable");
                return;
            }
        }
	InetAddress address = this.ipAddress;
	if (address != null)
	    logger.debug("CGate IP         {}.", address.getHostAddress());

        keepAliveFuture = threadPool.submit(new KeepAlive());
    }

    private class KeepAlive extends Thread {

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    try {
			CGateSession session = cGateSession;
                        if (session == null || !session.isConnected()) {
                            if (!getThing().getStatus().equals(ThingStatus.ONLINE))
                                connect();
                            else {
                                updateStatus();
                            }
                        } 
                    } catch (Exception e) {
                    }

                    sleep(10000l);
                }
            } catch (InterruptedException e) {
            }

        }
    }



    private void connect() {
	CGateSession cGateSession = this.cGateSession;
        if (cGateSession == null) {
            cGateSession = CGateInterface.connect(this.ipAddress, 20023, 20024, 20025, new CBusThreadPool());
            cGateSession.registerEventCallback(new EventMonitor());
            cGateSession.registerStatusChangeCallback(new StatusChangeMonitor());
	    this.cGateSession = cGateSession;
        }
        if (cGateSession.isConnected()) {
            logger.debug("CGate session reports online");
            updateStatus(ThingStatus.ONLINE);
        } else {
            try {
                cGateSession.connect();
                updateStatus();
                if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    initializeChildThings();
                }
            } catch (CGateConnectException e) {
                updateStatus();
                if (e.getMessage().equals("Connection refused")) {
                    logger.error("Failed to connect to CGate: Connection refused");
                } else {
                    logger.error("Failed to connect to CGate: {}", e);
                }
                try {
                    cGateSession.close();
                } catch (CGateException e2) {
                }
            }
        }
    }

    public void updateStatus() {
        ThingStatus lastStatus = getThing().getStatus();
	CGateSession cGateSession = this.cGateSession;
	if (cGateSession == null)
	    return;
        if (cGateSession.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            if (!lastStatus.equals(ThingStatus.OFFLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
        if (!getThing().getStatus().equals(lastStatus)) {
            boolean isOnline = getThing().getStatus().equals(ThingStatus.ONLINE);
            updateChildThings(isOnline);
        }
    }

    private void initializeChildThings() {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                // now also re-initialize all network handlers
                for (Thing thing : getThing().getThings()) {
                    ThingHandler handler = thing.getHandler();
                    if (handler instanceof CBusNetworkHandler) {
                        ((CBusNetworkHandler) handler).cgateStateChanged(true);
                    }
                }
            }
        });
    }

    private void updateChildThings(boolean isOnline) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                // now also re-initialize all network handlers
                for (Thing thing : getThing().getThings()) {
                    ThingHandler handler = thing.getHandler();
                    if (handler instanceof CBusNetworkHandler) {
                        ((CBusNetworkHandler) handler).cgateStateChanged(isOnline);
                    }
                }
            }
        });
    }

    private class StatusChangeMonitor extends StatusChangeCallback {

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void processStatusChange(@Nullable CGateSession cGateSession, String status) {
	    if (cGateSession == null)
		return;
            if (status.startsWith("# "))
                status = status.substring(2);
                              
            String contents[] = status.split("#");
            LinkedList<String> tokenizer = new LinkedList<String>(Arrays.asList(contents[0].split("\\s+")));
            LinkedList<String> commentTokenizer = new LinkedList<String>(Arrays.asList(contents[1].split("\\s+")));
            // logger.debug("Status - {} - sessionId {}" , status , cGateSession.getSessionID());
            if (cGateSession.getSessionID() != null
                    && contents[1].contains("sessionId=" + cGateSession.getSessionID())) {
                // logger.debug("Event generated by self, ignore post back {}", status);
                // We created this event - don't worry about processing it again...
                return;
            }
            if (tokenizer.peek().equals("lighting")) {
                tokenizer.poll();
                String state = tokenizer.poll();
                String address = tokenizer.poll();
                if (state.equals("ramp")) {
                    state = tokenizer.poll();
                }
                updateGroup(address, state);
            } else if (tokenizer.peek().equals("temperature")) {
                tokenizer.poll();
                tokenizer.poll();
                String address = tokenizer.poll();
                String temp = tokenizer.poll();
                updateGroup(address, temp);
            } else if (tokenizer.peek().equals("trigger")) {
                tokenizer.poll();
                String command = tokenizer.poll();
                String address = tokenizer.poll();
                if (command.equals("event"))
                {
                    String level = tokenizer.poll();
                    updateGroup(address, level);
                } else if (command.equals("indicatorkill"))
                {
                    updateGroup(address, "-1");
                } else
                {
                    logger.error("Unhandled trigger command {} - status {}",command, status);
                }
            } else if (tokenizer.peek().equals("clock")) {
                tokenizer.poll();
                String address = "";
                String value = "";
                String type = tokenizer.poll();
                if (type.equals("date")) {
                    address = tokenizer.poll() + "/1";
                    value = tokenizer.poll();
                } else if (type.equals("time")) {
                    address = tokenizer.poll() + "/0";
                    value = tokenizer.poll();
                } else if (!type.equals("request_refresh")) {
                    logger.error("Received unknown clock event: {}", status);
                }
                if (value != "") {
                    updateGroup(address, value);
                }
            } else if (commentTokenizer.peek().equals("lighting")) {
                commentTokenizer.poll();
                if (commentTokenizer.peek().equals("SyncUpdate")) {
                    commentTokenizer.poll();
                    String address = commentTokenizer.poll();
                    String level = commentTokenizer.poll().replace("level=", "");
                    updateGroup(address, level);
                }
            } else {
                logger.error("Received unparsed event: '{}'", status);
            }
        }

    }

    private class EventMonitor extends EventCallback {

        @Override
        public boolean acceptEvent(int event_code) {
            return true;
        }

        @Override
        public void processEvent(CGateSession cgate_session, int eventCode, GregorianCalendar event_time,
                String event) {
            LinkedList<String> tokenizer = new LinkedList<String>(Arrays.asList(event.trim().split("\\s+")));
            // List<String> tokenizer = Collections.synchronizedList(new LinkedList<String>());//
            // Arrays.asList(event.trim().split("\\s+"))));
            if (eventCode == 701) {
                String address = tokenizer.poll();
                // String oid = tokenizer.poll();
                String value = tokenizer.poll();
                if (value.startsWith("level=")) {
                    String level = value.replace("level=", "");
                    updateGroup(address, level);
                }
            }
        }

    }

    private void updateGroup(String address, String value) {
        String[] addressParts = address.trim().replace("//", "").split("/");
        updateGroup(addressParts[1], addressParts[2], addressParts[3], value);
    }

    private void updateGroup(String network, String application, String group, String value) {
        boolean handled = false;
        for (Thing networkThing : getThing().getThings()) {
            // Is this networkThing from the network we are looking for...
            if (networkThing.getThingTypeUID().equals(CBusBindingConstants.BRIDGE_TYPE_NETWORK) && networkThing
                    .getConfiguration().get(CBusBindingConstants.PROPERTY_ID).toString().equals(network)) {
                ThingHandler thingHandler = networkThing.getHandler();
                if (thingHandler == null) {
                    continue;
                }
                // Loop through all the things on this network and see if they match the application / group
                for (Thing thing : ((CBusNetworkHandler) thingHandler).getThing().getThings()) {
                    // Handle Lighting Application messages
                    if (application.equals(CBusBindingConstants.CBUS_APPLICATION_LIGHTING)
                            && thing.getThingTypeUID().equals(CBusBindingConstants.THING_TYPE_LIGHT)
                            && thing.getConfiguration().get(CBusBindingConstants.CONFIG_GROUP_ID).toString()
                                    .equals(group)) {
                        Channel channel = thing.getChannel(CBusBindingConstants.CHANNEL_STATE);
                        Channel channelLevel = thing.getChannel(CBusBindingConstants.CHANNEL_LEVEL);
                        if (channel != null && channelLevel != null)
                        {
                            ChannelUID channelUID = channel.getUID();
                            ChannelUID channelLevelUID = channelLevel.getUID();

                            if ("on".equalsIgnoreCase(value) || "255".equalsIgnoreCase(value)) {
                                updateState(channelUID, OnOffType.ON);
                                updateState(channelLevelUID, new PercentType(100));
                            } else if ("off".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value)) {
                                updateState(channelUID, OnOffType.OFF);
                                updateState(channelLevelUID, new PercentType(0));
                            } else {
                                try {
                                    int v = Integer.parseInt(value);
                                    updateState(channelUID, v > 0 ? OnOffType.ON : OnOffType.OFF);
                                    updateState(channelLevelUID, new PercentType((int) (v * 100 / 255.0)));
                                } catch (NumberFormatException e) {
                                    logger.error("Invalid value presented to channel {}. Received {}, expected On/Off",
                                                 channelUID, value);
                                }
                            }
                            logger.debug("Updating CBus Lighting Group {} with value {}", thing.getUID(), value);
                        } else
                        {
                            logger.debug("Failed to Update CBus Lighting Group {} with value {}: No Channel", thing.getUID(), value);
                        }
                                
                        handled = true;
                    }
                    // DALI Application
                    else if (application.equals(CBusBindingConstants.CBUS_APPLICATION_DALI)
                            && thing.getThingTypeUID().equals(CBusBindingConstants.THING_TYPE_DALI)
                            && thing.getConfiguration().get(CBusBindingConstants.CONFIG_GROUP_ID).toString()
                                    .equals(group)) {
                        Channel channel = thing.getChannel(CBusBindingConstants.CHANNEL_LEVEL);
                        if (channel != null)
                        {
                            ChannelUID channelUID = channel.getUID();                        

                            if ("on".equalsIgnoreCase(value) || "255".equalsIgnoreCase(value)) {
                                updateState(channelUID, OnOffType.ON);
                                updateState(channelUID, new PercentType(100));
                            } else if ("off".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value)) {
                                updateState(channelUID, OnOffType.OFF);
                                updateState(channelUID, new PercentType(0));
                            } else {
                                try {
                                    int v = Integer.parseInt(value);
                                    PercentType perc = new PercentType(Math.round(v * 100 / 255));
                                    updateState(channelUID, perc);
                                } catch (NumberFormatException e) {
                                    logger.error(
                                        "Invalid value presented to channel {}. Received {}, expected On/Off or decimal value",
                                        channelUID, value);
                                }
                            }
                            logger.debug("Updating CBus Lighting Group {} with value {}", thing.getUID(), value);
                        } else
                        {
                            logger.debug("Failed to Updat CBus Lighting Group {} with value {}: No Channel", thing.getUID(), value);
                        }
                        handled = true;

                    }
                    // Temperature Application
                    else if (application.equals(CBusBindingConstants.CBUS_APPLICATION_TEMPERATURE)
                            && thing.getThingTypeUID().equals(CBusBindingConstants.THING_TYPE_TEMPERATURE)
                            && thing.getConfiguration().get(CBusBindingConstants.CONFIG_GROUP_ID).toString()
                                    .equals(group)) {
                        Channel channel = thing.getChannel(CBusBindingConstants.CHANNEL_TEMP);

                        if (channel != null)
                        {
                            ChannelUID channelUID = channel.getUID();
                            DecimalType temp = new DecimalType(value);
                            updateState(channelUID, temp);
                            logger.trace("Updating CBus Temperature Group {} with value {}", thing.getUID(), value);
                        } else
                        {
                            logger.trace("Failed to Updat CBus Temperature Group {} with value {}: No Channel", thing.getUID(), value);
                        }
                        handled = true;
                    }
                    // Trigger Application
                    else if (application.equals(CBusBindingConstants.CBUS_APPLICATION_TRIGGER)
                            && thing.getThingTypeUID().equals(CBusBindingConstants.THING_TYPE_TRIGGER)
                            && thing.getConfiguration().get(CBusBindingConstants.CONFIG_GROUP_ID).toString()
                                    .equals(group)) {
			Channel channel = thing.getChannel(CBusBindingConstants.CHANNEL_VALUE);
			if (channel != null)
			{
				ChannelUID channelUID = channel.getUID();
				DecimalType val = new DecimalType(value);
				updateState(channelUID, val);
				logger.trace("Updating CBus Trigger Group {} with value {}", thing.getUID(), value);
			}
                        handled = true;
                    }
                }
            }
        }
        if (!handled) {
            // logger.warn("Unhandled CBus value update for {}/{}/{}: {}", network, application, group, value);
            ;
        } else {
            logger.trace("CBus value update for {}/{}/{}: {}", network, application, group, value);
        }
    }

    public CGateSession getCGateSession() {
        return cGateSession;
    }

    @Override
    public void dispose() {
        super.dispose();
	Future<?> keepAliveFuture = this.keepAliveFuture;
	if (keepAliveFuture != null)
		keepAliveFuture.cancel(true);
	CGateSession cGateSession = this.cGateSession;
        if (cGateSession != null && cGateSession.isConnected()) {
            try {
                cGateSession.close();
            } catch (CGateException e) {
                logger.error("Cannot close CGate session", e);
            }
        } else
            logger.debug("no session or it is disconnected");
    }

}
