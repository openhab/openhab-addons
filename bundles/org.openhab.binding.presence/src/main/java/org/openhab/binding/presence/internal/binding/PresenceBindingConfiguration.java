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
package org.openhab.binding.presence.internal.binding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.presence.internal.dhcp.DHCPListener;
import org.openhab.binding.presence.internal.utils.NetworkUtils;

/**
 * @author Mike Dabbs - Initial contribution
 */
@NonNullByDefault
public class PresenceBindingConfiguration {
    public String arpPingToolPath = "arping";
    public String pingToolPath = "ping";
    public @NonNullByDefault({}) ArpPingUtilEnum arpPingUtilMethod;
    public IpPingMethodEnum ipPingMethod = IpPingMethodEnum.JAVA_PING;
    public boolean allowDHCPListen = true;

    // This executor uses a cached thread pool that can grow as needed and does not server "Scheduled" tasks
    public volatile ExecutorService executor = Executors.newSingleThreadExecutor();

    public enum ArpPingUtilEnum {
        UNKNOWN_TOOL,
        IPUTILS_ARPING,
        THOMAS_HABET_ARPING,
        THOMAS_HABET_ARPING_WITHOUT_TIMEOUT,
        ELI_FULKERSON_ARP_PING_FOR_WINDOWS
    }

    public enum IpPingMethodEnum {
        JAVA_PING,
        WINDOWS_PING,
        IPUTILS_LINUX_PING,
        MAC_OS_PING
    }

    AtomicInteger c = new AtomicInteger(0);

    public void update(PresenceBindingConfiguration newConfig) {
        this.arpPingToolPath = newConfig.arpPingToolPath;
        this.pingToolPath = newConfig.pingToolPath;
        this.allowDHCPListen = newConfig.allowDHCPListen;
        if (!allowDHCPListen) {
            DHCPListener.shutdown();
        }

        this.arpPingUtilMethod = NetworkUtils.determineNativeARPpingMethod(arpPingToolPath);
        this.ipPingMethod = NetworkUtils.determinePingMethod(pingToolPath);

        executor.shutdown();
        executor = Executors.newCachedThreadPool((r) -> {
            return new Thread(r, "PresenceBindingWorker-" + c.addAndGet(1));
        });
    }
}
