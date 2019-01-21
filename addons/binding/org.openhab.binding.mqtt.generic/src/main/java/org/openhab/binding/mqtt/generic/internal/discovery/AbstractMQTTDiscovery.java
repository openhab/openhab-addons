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
package org.openhab.binding.mqtt.generic.internal.discovery;

import java.util.Date;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryParticipant;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;

/**
 * Base MQTT discovery class. Responsible for connecting to the {@link MQTTTopicDiscoveryService}.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractMQTTDiscovery extends AbstractDiscoveryService implements MQTTTopicDiscoveryParticipant {
    final protected String subscribeTopic;

    public AbstractMQTTDiscovery(@Nullable Set<ThingTypeUID> supportedThingTypes, int timeout,
            boolean backgroundDiscoveryEnabledByDefault, String baseTopic) {
        super(supportedThingTypes, timeout, backgroundDiscoveryEnabledByDefault);
        this.subscribeTopic = baseTopic;
    }

    /**
     * Return the topic discovery service.
     */
    protected abstract MQTTTopicDiscoveryService getDiscoveryService();

    @Override
    protected void startScan() {
        if (isBackgroundDiscoveryEnabled()) {
            super.stopScan();
            return;
        }
        getDiscoveryService().subscribe(this, subscribeTopic);
    }

    @Override
    protected synchronized void stopScan() {
        if (isBackgroundDiscoveryEnabled()) {
            super.stopScan();
            return;
        }
        getDiscoveryService().unsubscribe(this);
        super.stopScan();
    }

    @Override
    protected void startBackgroundDiscovery() {
        // Remove results that are restored after a restart
        removeOlderResults(new Date().getTime());
        getDiscoveryService().subscribe(this, subscribeTopic);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        getDiscoveryService().unsubscribe(this);
    }
}
