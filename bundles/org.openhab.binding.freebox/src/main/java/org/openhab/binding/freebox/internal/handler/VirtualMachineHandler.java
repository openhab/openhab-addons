/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.handler;

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freebox.internal.api.APIRequests;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.VirtualMachine;
import org.openhab.binding.freebox.internal.api.model.VirtualMachine.Status;
import org.openhab.binding.freebox.internal.config.VirtualMachineConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VirtualMachineHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VirtualMachineHandler extends HostHandler {
    private final Logger logger = LoggerFactory.getLogger(VirtualMachineHandler.class);
    private int vmId;

    public VirtualMachineHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
    }

    @Override
    public void initialize() {
        vmId = getConfigAs(VirtualMachineConfiguration.class).vmId;
        super.initialize();
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        logger.debug("Polling Virtual machine status");
        super.internalPoll();
        VirtualMachine vm = getApiManager().execute(new APIRequests.VirtualMachine(vmId));
        updateChannelOnOff(VM_STATUS, STATUS, vm.getStatus() == Status.RUNNING);
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        if (STATUS.equals(channelUID.getIdWithoutGroup()) && command instanceof OnOffType) {
            getApiManager().execute(new APIRequests.VirtualMachineAction(vmId, (OnOffType) command == OnOffType.ON));
            return true;
        }
        return false;
    }
}
