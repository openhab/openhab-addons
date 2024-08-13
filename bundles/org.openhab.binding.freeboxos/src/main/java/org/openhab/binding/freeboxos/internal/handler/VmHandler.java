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

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.VmManager;
import org.openhab.binding.freeboxos.internal.api.rest.VmManager.Status;
import org.openhab.binding.freeboxos.internal.api.rest.VmManager.VirtualMachine;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VmHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VmHandler extends HostHandler {
    private final Logger logger = LoggerFactory.getLogger(VmHandler.class);

    public VmHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        // We need to get and set the MAC address before calling super.initializeProperties
        VirtualMachine vm = getManager(VmManager.class).getDevice(getClientId());
        properties.put(Thing.PROPERTY_MAC_ADDRESS, vm.mac().toColonDelimitedString());
        updateProperties(properties);
        super.initializeProperties(properties);
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();

        if (!pushSubscribed) {
            logger.debug("Polling Virtual machine status");
            VirtualMachine vm = getManager(VmManager.class).getDevice(getClientId());
            updateVmChannels(vm);
        }
    }

    @Override
    protected void internalForcePoll() throws FreeboxException {
        super.internalForcePoll();

        logger.debug("Polling Virtual machine status");
        VirtualMachine vm = getManager(VmManager.class).getDevice(getClientId());
        updateVmChannels(vm);
    }

    public void updateVmChannels(VirtualMachine vm) {
        boolean running = Status.RUNNING.equals(vm.status());
        updateChannelOnOff(GROUP_VM_STATUS, STATUS, running);
        updateChannelOnOff(GROUP_CONNECTIVITY, REACHABLE, running);
        if (running) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/info-vm-not-running");
        }
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        if (STATUS.equals(channelId) && command instanceof OnOffType) {
            getManager(VmManager.class).power(getClientId(), OnOffType.ON.equals(command));
            return true;
        }
        return super.internalHandleCommand(channelId, command);
    }
}
