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
package org.openhab.io.hueemulation.internal.upnp;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.Selector;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.hueemulation.internal.HueEmulationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The upnp server runtime configuration. Based on a {@link HueEmulationConfig} and determined ip address and port.
 * This extends {@link Thread}, because a runtime configuration is always valid for exactly one thread to be started
 * once.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
class HueEmulationConfigWithRuntime extends Thread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(HueEmulationConfigWithRuntime.class);

    final @NonNullByDefault({}) HueEmulationConfig config;
    final InetAddress address;
    final String addressString;
    final InetAddress multicastAddress;
    int port;

    final CompletableFuture<@Nullable HueEmulationConfigWithRuntime> future = new CompletableFuture<>();
    final Consumer<HueEmulationConfigWithRuntime> r;

    // IO
    public @Nullable Selector asyncIOselector;
    private boolean hasAlreadyBeenStarted = false;

    HueEmulationConfigWithRuntime(Consumer<HueEmulationConfigWithRuntime> r, HueEmulationConfig config,
            String addrString, InetAddress MULTI_ADDR_IPV4, InetAddress MULTI_ADDR_IPV6) throws UnknownHostException {
        super("HueEmulation UPNP Server");
        this.r = r;
        this.config = config;

        address = InetAddress.getByName(addrString);
        if (address instanceof Inet6Address) {
            addressString = "[" + address.getHostAddress().split("%")[0] + "]";
            multicastAddress = MULTI_ADDR_IPV6;
        } else {
            addressString = address.getHostAddress();
            multicastAddress = MULTI_ADDR_IPV4;
        }

        port = config.discoveryHttpPort == 0 ? Integer.getInteger("org.osgi.service.http.port", 8080)
                : config.discoveryHttpPort;
    }

    HueEmulationConfigWithRuntime(Consumer<HueEmulationConfigWithRuntime> r, @Nullable HueEmulationConfig config,
            InetAddress MULTI_ADDR_IPV4, InetAddress MULTI_ADDR_IPV6) throws UnknownHostException {
        super("HueEmulation UPNP Server");
        this.r = r;
        this.config = config;

        address = InetAddress.getByName("localhost");
        if (address instanceof Inet6Address) {
            addressString = "[" + address.getHostAddress().split("%")[0] + "]";
            multicastAddress = MULTI_ADDR_IPV6;
        } else {
            addressString = address.getHostAddress();
            multicastAddress = MULTI_ADDR_IPV4;
        }
        port = 8080;
    }

    String getMulticastAddress() {
        if (multicastAddress instanceof Inet6Address) {
            return "[" + multicastAddress.getHostAddress().split("%")[0] + "]";
        } else {
            return multicastAddress.getHostAddress();
        }
    }

    public synchronized CompletableFuture<@Nullable HueEmulationConfigWithRuntime> startNow() {
        if (hasAlreadyBeenStarted) {
            logger.debug("Cannot restart thread");
            return future;
        }
        hasAlreadyBeenStarted = true;
        super.start();
        return future;
    }

    @Override
    public void run() {
        r.accept(this);
    }

    public void dispose() {
        Selector selector = asyncIOselector;
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException ignored) {
            }

            try {
                join();
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
            asyncIOselector = null;
        }
    }
}
