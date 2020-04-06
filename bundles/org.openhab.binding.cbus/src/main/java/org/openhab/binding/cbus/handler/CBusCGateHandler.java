/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.CGateConnectException;
import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.CGateInterface;
import com.daveoxley.cbus.CGateSession;
import com.daveoxley.cbus.CGateThreadPool;
import com.daveoxley.cbus.CGateThreadPoolExecutor;
import com.daveoxley.cbus.events.EventCallback;
import com.daveoxley.cbus.status.StatusChangeCallback;

/**
 * The {@link CBusThreadPool} is responsible for executing jobs from a threadpool
 *
 * @author John Harvey - Initial contribution
 */

@NonNullByDefault
final class CBusThreadPool extends CGateThreadPool {

    @NonNullByDefault
    public class CBusThreadPoolExecutor extends CGateThreadPoolExecutor {
        private final ThreadPoolExecutor threadPool;

        public CBusThreadPoolExecutor(@Nullable String poolName) {
            threadPool = (ThreadPoolExecutor) ThreadPoolManager.getPool(poolName);
        }

        @Override
        protected void execute(@Nullable Runnable runnable) {
            threadPool.execute(runnable);
        }
    }

    public CBusThreadPool() {
    }

    private Map<@Nullable String, CGateThreadPoolExecutor> executorMap = new HashMap<@Nullable String, CGateThreadPoolExecutor>();

    @Override
    @SuppressWarnings({ "unused", "null" }) /* map.get() can return null */
    protected CGateThreadPoolExecutor CreateExecutor(@Nullable String name) {
        @Nullable
        CGateThreadPoolExecutor executor = executorMap.get(name);

        if (executor != null) {
            return executor;
        }
        CGateThreadPoolExecutor newExecutor = new CBusThreadPoolExecutor(name);
        executorMap.put(name, newExecutor);
        return newExecutor;
    }
}

/**
 * The {@link CBusCGateHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */

@NonNullByDefault
public class CBusCGateHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(CBusCGateHandler.class);

    private @Nullable InetAddress ipAddress = null;

    public @Nullable CGateSession cGateSession = null;

    private @Nullable ScheduledFuture<?> keepAliveFuture = null;

    private final ExecutorService threadPool = ThreadPoolManager.getPool("CBusCGateHandler-Helper");

    public CBusCGateHandler(Bridge br) {
        super(br);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);
        logger.debug("Initializing CGate Bridge handler.");
        Configuration config = getThing().getConfiguration();
        String ipAddress = (String) config.get(CBusBindingConstants.CONFIG_CGATE_IP_ADDRESS);

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

        keepAliveFuture = scheduler.scheduleWithFixedDelay(() -> {
            keepAlive();
        }, 0, 100, TimeUnit.SECONDS);
    }

    private void keepAlive() {
        CGateSession session = cGateSession;
        if (session == null || !session.isConnected()) {
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                connect();
            } else {
                updateStatus();
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
            } catch (CGateConnectException e) {
                updateStatus();
                logger.warn("Failed to connect to CGate: {}", e.getMessage());
                try {
                    cGateSession.close();
                } catch (CGateException e2) {
                    // We dont really care if an exception is thrown when clossing the connection after a failure
                    // connecting.
                }
            }
        }
    }

    private void updateStatus() {
        ThingStatus lastStatus = getThing().getStatus();
        CGateSession cGateSession = this.cGateSession;
        if (cGateSession == null) {
            return;
        }
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

    private void updateChildThings(boolean isOnline) {
        threadPool.execute(() -> {
            // now also re-initialize all network handlers
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof CBusNetworkHandler) {
                    ((CBusNetworkHandler) handler).cgateStateChanged(isOnline);
                }
            }
        });
    }

    @NonNullByDefault
    private class StatusChangeMonitor extends StatusChangeCallback {

        @Override
        public boolean isActive() {
            return true;
        }

        @SuppressWarnings({ "null" })
        @Override
        public void processStatusChange(@Nullable CGateSession cGateSession, @Nullable String status) {
            if (cGateSession == null || status == null) {
                return;
            }
            if (status.startsWith("# ")) {
                status = status.substring(2);
                if (status == null) {
                    return;
                }
            }
            logger.debug("ProcessStatusChange {}", status);
            String contents[] = status.split("#");
            LinkedList<String> tokenizer = new LinkedList<String>(Arrays.asList(contents[0].split("\\s+")));
            LinkedList<String> commentTokenizer = new LinkedList<String>(Arrays.asList(contents[1].split("\\s+")));
            if (cGateSession.getSessionID() != null
                    && contents[1].contains("sessionId=" + cGateSession.getSessionID())) {
                // We created this event - don't worry about processing it again...
                return;
            }
            @Nullable
            String firstToken = tokenizer.peek();
            if (firstToken == null) {
                logger.debug("ProcessStateChange: Cant tokenize status {}", status);
                return;
            }
            switch (firstToken) {
                case "lighting": {
                    tokenizer.poll();
                    @Nullable
                    String state = tokenizer.poll();
                    @Nullable
                    String address = tokenizer.poll();
                    if ("ramp".equals("ramp")) {
                        state = tokenizer.poll();
                    }
                    updateGroup(address, state);
                    break;
                }
                case "temperature": {
                    tokenizer.poll();
                    tokenizer.poll();
                    @Nullable
                    String address = tokenizer.poll();
                    @Nullable
                    String temp = tokenizer.poll();
                    updateGroup(address, temp);
                    break;
                }
                case "trigger": {
                    tokenizer.poll();
                    @Nullable
                    String command = tokenizer.poll();
                    @Nullable
                    String address = tokenizer.poll();
                    if ("event".equals(command)) {
                        @Nullable
                        String level = tokenizer.poll();
                        updateGroup(address, level);
                    } else if ("indicatorkill".equals(command)) {
                        updateGroup(address, "-1");
                    } else {
                        logger.warn("Unhandled trigger command {} - status {}", command, status);
                    }
                    break;
                }
                case "clock": {
                    tokenizer.poll();
                    @Nullable
                    String address = "";
                    @Nullable
                    String value = "";
                    @Nullable
                    String type = tokenizer.poll();
                    if ("date".equals(type)) {
                        address = tokenizer.poll() + "/1";
                        value = tokenizer.poll();
                    } else if ("time".equals(type)) {
                        address = tokenizer.poll() + "/0";
                        value = tokenizer.poll();
                    } else if (!"request_refresh".equals(type)) {
                        logger.warn("Received unknown clock event: {}", status);
                    }
                    if (value != null && !value.isEmpty()) {
                        updateGroup(address, value);
                    }
                    break;
                }
                default:
                    if ("lighting".equals(commentTokenizer.peek())) {
                        commentTokenizer.poll();
                        @Nullable
                        String commentToken = commentTokenizer.peek();
                        if ("SyncUpdate".equals(commentToken)) {
                            commentTokenizer.poll();
                            @Nullable
                            String address = commentTokenizer.poll();

                            @Nullable
                            String level = commentTokenizer.poll();
                            level.replace("level=", "");
                            updateGroup(address, level);
                        }
                    } else {
                        logger.warn("Received unparsed event: '{}'", status);
                    }
                    break;
            }
        }

    }

    @NonNullByDefault
    private class EventMonitor extends EventCallback {

        @Override
        public boolean acceptEvent(int event_code) {
            return true;
        }

        @SuppressWarnings({ "null" })
        @Override
        public void processEvent(@Nullable CGateSession cgate_session, int eventCode,
                @Nullable GregorianCalendar event_time, @Nullable String event) {
            if (event == null) {
                return;
            }
            LinkedList<String> tokenizer = new LinkedList<String>(Arrays.asList(event.trim().split("\\s+")));

            if (eventCode == 701) {
                @Nullable
                String address = tokenizer.poll();
                tokenizer.poll();
                @Nullable
                String value = tokenizer.poll();
                if (value != null && value.startsWith("level=")) {
                    String level = value.replace("level=", "");
                    updateGroup(address, level);
                }
            }
        }

    }

    private void updateGroup(@Nullable String address, @Nullable String value) {
        if (address == null || value == null) {
            return;
        }
        String[] addressParts = address.trim().replace("//", "").split("/");
        int application = Integer.parseInt(addressParts[2]);
        int group = Integer.parseInt(addressParts[3]);
        updateGroup(addressParts[1], application, group, value);
    }

    private void updateGroup(String network, int application, int group, String value) {
        for (Thing networkThing : getThing().getThings()) {
            // Is this networkThing from the network we are looking for...
            if (networkThing.getThingTypeUID().equals(CBusBindingConstants.BRIDGE_TYPE_NETWORK) && networkThing
                    .getConfiguration().get(CBusBindingConstants.CONFIG_NETWORK_ID).toString().equals(network)) {
                ThingHandler netThingHandler = networkThing.getHandler();
                if (netThingHandler == null) {
                    continue;
                }
                // Loop through all the things on this network and see if they match the application / group
                for (Thing thing : ((CBusNetworkHandler) netThingHandler).getThing().getThings()) {
                    ThingHandler thingThingHandler = thing.getHandler();
                    if (thingThingHandler == null) {
                        continue;
                    }

                    if (thingThingHandler instanceof CBusGroupHandler) {
                        ((CBusGroupHandler) thingThingHandler).updateGroup(application, group, value);
                    }
                }
            }
        }
    }

    @Nullable
    public CGateSession getCGateSession() {
        return cGateSession;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> keepAliveFuture = this.keepAliveFuture;
        if (keepAliveFuture != null) {
            keepAliveFuture.cancel(true);
        }
        CGateSession cGateSession = this.cGateSession;
        if (cGateSession != null && cGateSession.isConnected()) {
            try {
                cGateSession.close();
            } catch (CGateException e) {
                logger.warn("Cannot close CGate session", e);
            }
        } else
            logger.debug("no session or it is disconnected");
        super.dispose();
    }

}
