/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.vm.VirtualMachine;
import org.openhab.binding.freeboxos.internal.api.vm.VirtualMachine.Status;
import org.openhab.binding.freeboxos.internal.api.vm.VmManager;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
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
    protected void internalPoll() throws FreeboxException {
        super.internalPoll();
        logger.debug("Polling Virtual machine status");
        VmManager vmManager = getManager(VmManager.class);
        VirtualMachine vm = vmManager.getDevice(getConfigAs(ClientConfiguration.class).id);
        updateChannelOnOff(VM_STATUS, STATUS, vm.getStatus() == Status.RUNNING);
    }

    @Override
    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        VmManager vmManager = getManager(VmManager.class);
        if (STATUS.equals(channelUID.getIdWithoutGroup()) && command instanceof OnOffType) {
            vmManager.power(getConfigAs(ClientConfiguration.class).id, command == OnOffType.ON);
            return true;
        }
        return super.internalHandleCommand(channelUID, command);
    }
}
