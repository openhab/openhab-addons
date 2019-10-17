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
package org.openhab.binding.nikobus.internal.handler;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.*;
import static org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nikobus.internal.protocol.NikobusCommand;
import org.openhab.binding.nikobus.internal.protocol.SwitchModuleGroup;
import org.openhab.binding.nikobus.internal.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusPushButtonHandler} is responsible for handling Nikobus push buttons.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusPushButtonHandler extends NikobusBaseThingHandler {
    private static class ImpactedModule {
        private final String address;
        private final SwitchModuleGroup group;

        ImpactedModule(String address, SwitchModuleGroup group) {
            this.address = address;
            this.group = group;
        }

        public String getAddress() {
            return address;
        }

        public SwitchModuleGroup getGroup() {
            return group;
        }

        @Override
        public String toString() {
            return "'" + address + "'-" + group;
        }
    }

    private static final String END_OF_TRANSMISSION = "\r#E1";
    private static final String MODULE_CHANNEL_PATTERN = "^[A-Z0-9]{4}-[12]$";
    private final Logger logger = LoggerFactory.getLogger(NikobusPushButtonHandler.class);
    private final List<ImpactedModule> impactedModules = Collections.synchronizedList(new ArrayList<>());
    private @Nullable Future<?> requestUpdateFuture;

    public NikobusPushButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        impactedModules.clear();

        String[] impactedModulesString = getConfig().get(CONFIG_IMPACTED_MODULES).toString().split(",");
        for (String impactedModuleString : impactedModulesString) {
            impactedModuleString = impactedModuleString.trim();
            if (!impactedModuleString.matches(MODULE_CHANNEL_PATTERN)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Unexpected impactedModules format found '" + impactedModuleString + "'");
                return;
            }

            String[] moduleInfo = impactedModuleString.split("-");
            SwitchModuleGroup group = moduleInfo[1].equals("1") ? FIRST : SECOND;

            impactedModules.add(new ImpactedModule(moduleInfo[0], group));
        }

        logger.debug("Impacted modules for {} = {}", thing.getUID(), impactedModules);

        updateStatus(ThingStatus.UNKNOWN);

        NikobusPcLinkHandler pcLink = getPcLink();
        if (pcLink != null) {
            pcLink.addListener(getAddress(), this::commandReceived);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        Utils.cancel(requestUpdateFuture);
        requestUpdateFuture = null;

        NikobusPcLinkHandler pcLink = getPcLink();
        if (pcLink != null) {
            pcLink.removeListener(getAddress());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand '{}' '{}'", channelUID, command);

        if (!CHANNEL_BUTTON.equals(channelUID.getId())) {
            return;
        }

        // Whenever the button receives an ON command,
        // we send a simulated button press to the Nikobus.
        if (command == OnOffType.ON) {
            NikobusPcLinkHandler pcLink = getPcLink();
            if (pcLink != null) {
                pcLink.sendCommand(new NikobusCommand(getAddress() + END_OF_TRANSMISSION));
            }
        }
    }

    private void commandReceived() {
        if (thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }

        updateState(CHANNEL_BUTTON, OnOffType.ON);

        Utils.cancel(requestUpdateFuture);
        requestUpdateFuture = scheduler.schedule(this::update, 400, TimeUnit.MILLISECONDS);
    }

    private void update() {
        for (ImpactedModule module : impactedModules) {
            NikobusSwitchModuleHandler switchModule = getSwitchModuleWithId(module.getAddress());
            if (switchModule != null) {
                switchModule.requestStatus(module.getGroup());
            }
        }
    }

    private @Nullable NikobusSwitchModuleHandler getSwitchModuleWithId(String id) {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }

        for (Thing thing : bridge.getThings()) {
            if (thing.getUID().getId().equals(id)) {
                return (NikobusSwitchModuleHandler) thing.getHandler();
            }
        }

        return null;
    }

    @Override
    protected String getAddress() {
        return "#N" + super.getAddress();
    }
}
