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
package org.openhab.binding.panasonictv.internal.event;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicEventListenerServiceImpl} is responsible for
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = PanasonicEventListenerService.class)
public class PanasonicEventListenerServiceImpl implements NetworkAddressChangeListener, PanasonicEventListenerService {
    private final Logger logger = LoggerFactory.getLogger(PanasonicEventListenerServiceImpl.class);

    private final NetworkAddressService networkAddressService;
    private final Map<String, ListenerObject> listeners = new ConcurrentHashMap<>();

    private @Nullable PanasonicEventReceiver eventReceiver;
    private @Nullable String localIPv4Address;

    @Activate
    public PanasonicEventListenerServiceImpl(@Reference NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
        this.networkAddressService.addNetworkAddressChangeListener(this);
        this.localIPv4Address = this.networkAddressService.getPrimaryIpv4HostAddress();
        startService(localIPv4Address);
    }

    @Deactivate
    public void deactivate() {
        networkAddressService.removeNetworkAddressChangeListener(this);
        stopService();
    }

    @Override
    public void onChanged(List<CidrAddress> list, List<CidrAddress> list1) {
    }

    @Override
    public void onPrimaryAddressChanged(@Nullable String oldPrimaryAddress, @Nullable String newPrimaryAddress) {
        stopService().thenRun(() -> startService(newPrimaryAddress));
    }

    private void startService(@Nullable String localIp) {
        if (eventReceiver != null) {
            logger.warn(
                    "Trying to start listener thread but it is already running. This is a bug and might leak resources.");
            return;
        }
        if (localIp == null) {
            logger.info(
                    "Could not determine primary IPv4. Listener not starting up. Retrying when primary IPv4 changed.");
            return;
        }

        try {
            eventReceiver = new PanasonicEventReceiver(localIp, listeners);
        } catch (IOException e) {
            logger.warn(
                    "IP {} provided but could not start a listening server for that address: {}. Retrying when primary IPv4 changed.",
                    localIp, e.getMessage());
            return;
        }
        // finally subscribe for all listeners
        listeners.keySet().forEach(this::subscribeServiceToTv);
    }

    private CompletableFuture<Boolean> stopService() {
        // unsubscribe listeners before stopping service
        listeners.keySet().forEach(this::unsubscribeServiceFromTv);
        PanasonicEventReceiver eventReceiver = this.eventReceiver;
        if (eventReceiver != null) {
            return eventReceiver.requestStop().handle((v, t) -> {
                this.eventReceiver = null;
                if (t != null) {
                    logger.warn("Failed to shutdown listening server: {}", t.getMessage());
                    return false;
                }
                return true;
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void subscribeListener(String uuid, PanasonicEventListener listener, String remoteAddress) {
        if (listeners.put(uuid, new ListenerObject(listener, remoteAddress)) != null) {
            logger.warn("Listener {} was subscribed before but not unsubscribed, this is a bug.", uuid);
        }
        subscribeServiceToTv(uuid);
    }

    @Override
    public void unsubscribeListener(String uuid) {
        unsubscribeServiceFromTv(uuid);
        listeners.remove(uuid);
    }

    // @formatter:off
    private static final String SUBSCRIBE_MESSAGE = "SUBSCRIBE /nrc/event_0 HTTP/1.1\r\n"
            + "User-Agent: OpenHAB/3.0\r\n"
            + "Host: {0}:55000\r\n"
            + "CALLBACK: <http://{1}/nrc>\r\n"
            + "NT: upnp:event\r\n"
            + "TIMEOUT: Second-300\r\n";
    private static final String UNSUBSCRIBE_MESSAGE = "UNSUBSCRIBE /nrc/event_0 HTTP/1.1\r\n"
            + "User-Agent: OpenHAB/3.0\r\n"
            + "Host: {0}:55000\r\n"
            + "{1}\r\n";
    // @formatter:on

    private void subscribeServiceToTv(String uuid) {
        PanasonicEventReceiver eventReceiver = this.eventReceiver;
        if (eventReceiver == null) {
            logger.debug("Delaying subscription of {}, event receiver  not ready.", uuid);
            return;
        }
        ListenerObject listener = listeners.get(uuid);
        if (listener == null) {
            logger.warn("Listener for UUID {} not found. This is a bug.", uuid);
            return;
        }
        if (listener.sessionId != null) {
            logger.warn("Trying to subscribe listener for UUID {} but a subscription is already present.", uuid);
        }
        try (Socket socket = new Socket(listener.remoteAddress, 55000);
                PrintStream out = new PrintStream(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String subscription = MessageFormat.format(SUBSCRIBE_MESSAGE, listener.remoteAddress,
                    eventReceiver.getServerAddress());
            logger.trace("Trying subscription: {}", subscription);
            out.println(subscription);
            List<String> response = reader.lines().collect(Collectors.toList());
            logger.trace("Subscription response: {}", response);
            if (response.isEmpty() || !response.get(0).contains("200")) {
                throw new IOException("Response invalid: empty or not state 200/OK.");
            }
            // extract SID
            listener.sessionId = response.stream().filter(line -> line.startsWith("SID:")).findAny().orElse(null);
            if (listener.sessionId == null) {
                logger.warn("Failed to get session id.");
            }
        } catch (IOException e) {
            logger.warn("Failed to subscribe to TV {}/{}: {}", uuid, listener.remoteAddress, e.getMessage());
        }
    }

    private void unsubscribeServiceFromTv(String uuid) {
        ListenerObject listener = listeners.get(uuid);
        if (listener == null) {
            logger.warn("Listener for UUID {} not found. This is a bug.", uuid);
            return;
        }
        if (listener.sessionId == null) {
            logger.warn("Trying to unsubscribe listener for UUID {} but no subscription found.", uuid);
            return;
        }
        listener.sessionId = null;
        try (Socket socket = new Socket(listener.remoteAddress, 55000);
                PrintStream out = new PrintStream(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String unsubscription = MessageFormat.format(UNSUBSCRIBE_MESSAGE, listener.remoteAddress,
                    listener.sessionId);
            logger.trace("Trying unsubscription: {}", unsubscription);
            out.println(unsubscription);
        } catch (IOException e) {
            logger.warn("Failed to unsubscribe from TV {}/{}: {}", uuid, listener.remoteAddress, e.getMessage());
        }
    }
}
