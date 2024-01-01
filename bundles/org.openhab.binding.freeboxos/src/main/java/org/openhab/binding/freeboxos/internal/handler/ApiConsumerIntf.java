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
package org.openhab.binding.freeboxos.internal.handler;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;

import inet.ipaddr.MACAddressString;
import inet.ipaddr.mac.MACAddress;

/**
 * The {@link ApiConsumerIntf} defines some common methods for various devices (server, player, repeater) not belonging
 * to the same class hierarchy
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public interface ApiConsumerIntf extends ThingHandler {

    Map<String, String> editProperties();

    Configuration getConfig();

    void updateProperties(@Nullable Map<String, String> properties);

    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);

    void stopJobs();

    void addJob(String name, Runnable command, long initialDelay, long delay, TimeUnit unit);

    void addJob(String name, Runnable command, long delay, TimeUnit unit);

    default int getClientId() {
        return ((BigDecimal) getConfig().get(ClientConfiguration.ID)).intValue();
    }

    default MACAddress getMac() {
        String mac = (String) getConfig().get(Thing.PROPERTY_MAC_ADDRESS);
        return new MACAddressString(mac).getAddress();
    }
}
