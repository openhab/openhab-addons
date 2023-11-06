/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.LinkedList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.internal.CBusCGateConfiguration;
import org.openhab.binding.cbus.internal.CBusThreadPool;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.CGateConnectException;
import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.CGateInterface;
import com.daveoxley.cbus.CGateSession;
import com.daveoxley.cbus.events.EventCallback;
import com.daveoxley.cbus.status.StatusChangeCallback;

/**
 * The {@link CBusCGateHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */

@NonNullByDefault
public class CBusCGateHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(CBusCGateHandler.class);
    private @Nullable InetAddress ipAddress;
    public @Nullable CGateSession cGateSession;
    private @Nullable ScheduledFuture<?> keepAliveFuture;

    public CBusCGateHandler(Bridge br) {
        super(br);
    }

    // This is abstract in base class so have to implement it.
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);
        logger.debug("Initializing CGate Bridge handler. {} {}", getThing().getThingTypeUID(), getThing().getUID());
        CBusCGateConfiguration configuration = getConfigAs(CBusCGateConfiguration.class);
        logger.debug("Using configuration {}", configuration);
        try {
            this.ipAddress = InetAddress.getByName(configuration.ipAddress);
        } catch (UnknownHostException e1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "IP Address not resolvable");
            return;
        }

        InetAddress address = this.ipAddress;
        if (address != null) {
            logger.debug("CGate IP         {}.", address.getHostAddress());
        }

        keepAliveFuture = scheduler.scheduleWithFixedDelay(this::keepAlive, 0, 100, TimeUnit.SECONDS);
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
                logger.debug("Failed to connect to CGate:", e);
                try {
                    cGateSession.close();
                } catch (CGateException ignore) {
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
            if (lastStatus != ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
        if (!getThing().getStatus().equals(lastStatus)) {
            boolean isOnline = getThing().getStatus().equals(ThingStatus.ONLINE);
            updateChildThings(isOnline);
        }
    }

    private void updateChildThings(boolean isOnline) {
        scheduler.execute(() -> {
            // now also re-initialize all network handlers
            for (Thing thing : getThing().getThings()) {
                ThingHandler handler = thing.getHandler();
                if (handler instanceof CBusNetworkHandler networkHandler) {
                    networkHandler.cgateStateChanged(isOnline);
                }
            }
        });
    }

    private void updateGroup(@Nullable String address, @Nullable String value) {
        if (address == null || value == null) {
            return;
        }
        logger.debug("updateGroup address {}", address);
        // Address should be of the form //Project/network/application/group
        if (!address.startsWith("//")) {
            logger.debug("Address does not start with // so ignoring this update");
            return;
        }
        String[] addressParts = address.substring(2).split("/");
        if (addressParts.length != 4) {
            logger.debug("Address is badly formed so ignoring this update length of parts is {} not 4",
                    addressParts.length);
            return;
        }
        updateGroup(Integer.parseInt(addressParts[1]), Integer.parseInt(addressParts[2]),
                Integer.parseInt(addressParts[3]), value);
    }

    private void updateGroup(int network, int application, int group, String value) {
        for (Thing networkThing : getThing().getThings()) {
            // Is this networkThing from the network we are looking for...
            if (networkThing.getThingTypeUID().equals(CBusBindingConstants.BRIDGE_TYPE_NETWORK)) {
                CBusNetworkHandler netThingHandler = (CBusNetworkHandler) networkThing.getHandler();
                if (netThingHandler == null || netThingHandler.getNetworkId() != network) {
                    continue;
                }
                // Loop through all the things on this network and see if they match the application / group
                for (Thing thing : netThingHandler.getThing().getThings()) {
                    ThingHandler thingThingHandler = thing.getHandler();
                    if (thingThingHandler == null) {
                        continue;
                    }

                    if (thingThingHandler instanceof CBusGroupHandler groupHandler) {
                        groupHandler.updateGroup(application, group, value);
                    }
                }
            }
        }
    }

    public @Nullable CGateSession getCGateSession() {
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
        } else {
            logger.debug("no session or it is disconnected");
        }
        super.dispose();
    }

    @NonNullByDefault
    private class EventMonitor extends EventCallback {

        @Override
        public boolean acceptEvent(int eventCode) {
            return true;
        }

        @Override
        public void processEvent(@Nullable CGateSession cgate_session, int eventCode,
                @Nullable GregorianCalendar event_time, @Nullable String event) {
            if (event == null) {
                return;
            }

            if (eventCode == 701) {
                // By Marking this as Nullable it fools the static analyser into understanding that poll can return Null
                LinkedList<@Nullable String> tokenizer = new LinkedList<>(Arrays.asList(event.trim().split("\\s+")));
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

    @NonNullByDefault
    private class StatusChangeMonitor extends StatusChangeCallback {

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void processStatusChange(@Nullable CGateSession cGateSession, @Nullable String status) {
            if (cGateSession == null || status == null) {
                return;
            }
            if (status.startsWith("# ")) {
                status = status.substring(2);
                // Shouldnt need to check for null but this silences a warning
                if (status == null || status.isEmpty()) {
                    return;
                }
            }
            logger.debug("ProcessStatusChange {}", status);
            String[] contents = status.split("#");
            if (cGateSession.getSessionID() != null
                    && contents[1].contains("sessionId=" + cGateSession.getSessionID())) {
                // We created this event - don't worry about processing it again...
                return;
            }
            // By Marking this as Nullable it fools the static analyser into understanding that poll can return Null
            LinkedList<@Nullable String> tokenizer = new LinkedList<>(Arrays.asList(contents[0].split("\\s+")));
            @Nullable
            String firstToken = tokenizer.poll();
            if (firstToken == null) {
                logger.debug("ProcessStateChange: Cant tokenize status {}", status);
                return;
            }
            switch (firstToken) {
                case "lighting": {
                    @Nullable
                    String state = tokenizer.poll();
                    @Nullable
                    String address = tokenizer.poll();
                    if ("ramp".equals(state)) {
                        state = tokenizer.poll();
                    }
                    updateGroup(address, state);
                    break;
                }
                case "temperature": {
                    // For temperature we ignore the state
                    tokenizer.poll();
                    @Nullable
                    String address = tokenizer.poll();
                    @Nullable
                    String temp = tokenizer.poll();
                    updateGroup(address, temp);
                    break;
                }
                case "trigger": {
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
                        // We dont handle request_refresh as we are not a clock master
                        logger.debug("Received unknown clock event: {}", status);
                    }
                    if (value != null && !value.isEmpty()) {
                        updateGroup(address, value);
                    }
                    break;
                }
                default: {
                    LinkedList<String> commentTokenizer = new LinkedList<>(Arrays.asList(contents[1].split("\\s+")));
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
                            level = level.replace("level=", "");
                            updateGroup(address, level);
                        }
                    } else {
                        logger.debug("Received unparsed event: '{}'", status);
                    }
                    break;
                }
            }
        }
    }
}
