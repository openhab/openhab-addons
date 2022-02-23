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
package org.openhab.binding.freeboxos.internal.api.vm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.ListableRest;
import org.openhab.binding.freeboxos.internal.api.vm.VirtualMachine.VirtualMachineResponse;
import org.openhab.binding.freeboxos.internal.api.vm.VirtualMachine.VirtualMachinesResponse;

/**
 * The {@link VmManager} is the Java class used to handle api requests
 * related to virtual machines
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VmManager extends ListableRest<VirtualMachine, VirtualMachineResponse, VirtualMachinesResponse> {
    private static final String VM_SUB_PATH = "vm";

    public VmManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.VM, VirtualMachineResponse.class, VirtualMachinesResponse.class, VM_SUB_PATH);
    }

    public void power(int vmId, boolean startIt) throws FreeboxException {
        post(deviceSubPath(vmId), startIt ? "start" : "powerbutton");
    }
}
