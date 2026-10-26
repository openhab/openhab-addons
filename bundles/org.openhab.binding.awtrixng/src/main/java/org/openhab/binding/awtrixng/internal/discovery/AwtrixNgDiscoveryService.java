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
package org.openhab.binding.awtrixng.internal.discovery;

import static org.openhab.binding.awtrixng.internal.AwtrixNgBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.awtrixng.internal.Helper;
import org.openhab.binding.awtrixng.internal.dto.DeviceState;
import org.openhab.binding.mqtt.discovery.AbstractMQTTDiscovery;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link AwtrixNgDiscoveryService} finds AWTRIX NG clocks by the retained device state they publish on
 * {@code <prefix>/state/device}. The prefix is the first topic level, so a clock with a prefix of several levels is not
 * found and has to be added manually.
 *
 * @author Keith T. Garner - Initial contribution
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.awtrixng")
@NonNullByDefault
public class AwtrixNgDiscoveryService extends AbstractMQTTDiscovery {

    // The uid of a clock is its Wi-Fi MAC address in lowercase without colons
    private static final Pattern UID_PATTERN = Pattern.compile("[0-9a-f]{12}");

    private final MQTTTopicDiscoveryService discoveryService;

    @Activate
    public AwtrixNgDiscoveryService(@Reference MQTTTopicDiscoveryService discoveryService) {
        super(Set.of(THING_TYPE_CLOCK), 3, true, "+" + TOPIC_STATE_DEVICE);
        this.discoveryService = discoveryService;
    }

    @Override
    public void receivedMessage(ThingUID connectionBridge, MqttBrokerConnection connection, String topic,
            byte[] payload) {
        resetTimeout();
        if (!topic.endsWith(TOPIC_STATE_DEVICE) || topic.length() == TOPIC_STATE_DEVICE.length()) {
            return;
        }
        DeviceState state = Helper.fromJson(new String(payload, StandardCharsets.UTF_8), DeviceState.class);
        // Other devices publish a state/device topic as well, so only accept what an AWTRIX NG clock reports
        if (state == null || state.boardType == null || state.uid == null
                || !UID_PATTERN.matcher(state.uid).matches()) {
            return;
        }
        String basetopic = topic.substring(0, topic.length() - TOPIC_STATE_DEVICE.length());

        DiscoveryResultBuilder result = DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_CLOCK, connectionBridge, state.uid)).withBridge(connectionBridge)
                .withProperty(PROP_BASETOPIC, basetopic).withProperty(PROP_UNIQUEID, state.uid)
                .withProperty(PROP_BOARD_TYPE, state.boardType).withRepresentationProperty(PROP_UNIQUEID)
                .withLabel("AWTRIX NG Clock " + (state.hostname != null ? state.hostname : state.uid));
        if (state.version != null) {
            result.withProperty(PROP_FIRMWARE, state.version);
        }
        if (state.macAddress != null) {
            result.withProperty(PROP_MAC_ADDRESS, state.macAddress);
        }
        if (state.hostname != null) {
            result.withProperty(PROP_HOSTNAME, state.hostname);
        }
        thingDiscovered(result.build());
    }

    @Override
    public void topicVanished(ThingUID connectionBridge, MqttBrokerConnection connection, String topic) {
    }

    @Override
    protected MQTTTopicDiscoveryService getDiscoveryService() {
        return discoveryService;
    }
}
