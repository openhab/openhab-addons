/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.scan;

import java.time.Duration;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Lists mDNS services of one type. Multicast scanning goes through this so the caller decides
 * which mDNS stack answers (openHAB's shared {@code MDNSClient} in the binding).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
@FunctionalInterface
public interface ServiceBrowser {

    /**
     * Lists the services of a type that answer within the timeout.
     *
     * @param type fully qualified service type including the trailing dot, e.g.
     *            {@code _mediaremotetv._tcp.local.}
     * @param timeout how long to wait for answers
     * @return the resolved services
     */
    ServiceInfo[] list(String type, Duration timeout);

    /**
     * Browses through a single jmDNS instance.
     *
     * @param jmdns instance to browse with (not closed by the browser)
     * @return a browser backed by {@code jmdns}
     */
    static ServiceBrowser of(JmDNS jmdns) {
        return (type, timeout) -> jmdns.list(type, timeout.toMillis());
    }
}
