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
package org.openhab.binding.leapmotion.internal.discovery;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.leapmotion.internal.LeapMotionBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;

/**
 * This is a discovery service, which finds locally attached LeapMotion controllers and adds them to the Inbox
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.leapmotion")
public class LeapMotionDiscoveryService extends AbstractDiscoveryService {

    private @NonNullByDefault({}) Controller leapController;
    private @NonNullByDefault({}) Listener listener;

    public LeapMotionDiscoveryService() throws IllegalArgumentException {
        super(Set.of(LeapMotionBindingConstants.THING_TYPE_CONTROLLER), 10, true);
    }

    @Override
    protected void activate(@Nullable Map<String, Object> configProperties) {
        leapController = new Controller();
        listener = new Listener() {
            @Override
            public void onConnect(@Nullable Controller c) {
                createDiscoveryResult();
            }

            @Override
            public void onDisconnect(@Nullable Controller c) {
                removeDiscoveryResult();
            }
        };
        super.activate(configProperties);
    }

    @Override
    protected void deactivate() {
        leapController.removeListener(listener);
        listener = null;
        leapController.delete();
        leapController = null;
        super.deactivate();
    }

    @Override
    protected void startScan() {
        if (leapController.isConnected()) {
            createDiscoveryResult();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        super.startBackgroundDiscovery();
        leapController.addListener(listener);
    }

    private void createDiscoveryResult() {
        DiscoveryResult result = DiscoveryResultBuilder
                .create(new ThingUID(LeapMotionBindingConstants.THING_TYPE_CONTROLLER, "local"))
                .withLabel("Leap Motion Controller").build();
        thingDiscovered(result);
    }

    private void removeDiscoveryResult() {
        removeOlderResults(System.currentTimeMillis());
    }
}
