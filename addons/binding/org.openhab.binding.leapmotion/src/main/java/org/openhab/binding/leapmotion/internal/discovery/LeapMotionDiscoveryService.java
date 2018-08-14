/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.leapmotion.internal.discovery;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.leapmotion.LeapMotionBindingConstants;
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
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.leapmotion")
public class LeapMotionDiscoveryService extends AbstractDiscoveryService {

    private @NonNullByDefault({}) Controller leapController;
    private @NonNullByDefault({}) Listener listener;

    public LeapMotionDiscoveryService() throws IllegalArgumentException {
        super(Collections.singleton(LeapMotionBindingConstants.THING_TYPE_CONTROLLER), 10, true);
    }

    @Override
    protected void activate(@Nullable Map<String, @Nullable Object> configProperties) {
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
