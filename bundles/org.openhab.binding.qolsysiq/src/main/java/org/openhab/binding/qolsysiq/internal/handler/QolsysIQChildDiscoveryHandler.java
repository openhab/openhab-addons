/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.qolsysiq.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.qolsysiq.internal.discovery.QolsysIQChildDiscoveryService;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Callback for our custom discovery service
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public interface QolsysIQChildDiscoveryHandler extends ThingHandler {
    /**
     * Sets a {@link QolsysIQChildDiscoveryService} to call when device information is received
     *
     * @param service
     */
    void setDiscoveryService(QolsysIQChildDiscoveryService service);

    /**
     * Initiates the discovery process
     */
    void startDiscovery();
}
