/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm.internal.discovery;

import static org.openhab.binding.alarm.AlarmBindingConstants.*;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link AlarmDiscoveryService} creates an Alarm controller.
 *
 * @author Gerhard Riegler - Initial Contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.alarm")
public class AlarmDiscoveryService extends AbstractDiscoveryService {
    private static final ThingUID ALARM_CONTROLLER_THING = new ThingUID(THING_TYPE_ALARM_CONTROLLER, HOME);

    public AlarmDiscoveryService() {
        super(new HashSet<>(Arrays.asList(new ThingTypeUID(BINDING_ID, "-"))), 1, true);
    }

    @Override
    protected void startScan() {
        discover();
    }

    @Override
    protected void startBackgroundDiscovery() {
        discover();
    }

    private void discover() {
        thingDiscovered(
                DiscoveryResultBuilder.create(ALARM_CONTROLLER_THING).withLabel(ALARM_CONTROLLER_LABEL).build());
    }

}
