/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.VM_SUB_PATH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.vm.VmResponses.VirtualMachineResponse;
import org.openhab.binding.freeboxos.internal.api.vm.VmResponses.VirtualMachinesResponse;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.ListableRest;

/**
 * The {@link VmManager} is the Java class used to handle api requests related to virtual machines
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VmManager extends ListableRest<VirtualMachine, VirtualMachineResponse, VirtualMachinesResponse> {

    public VmManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.VM, VirtualMachineResponse.class, VirtualMachinesResponse.class, VM_SUB_PATH);
    }

    public void power(int vmId, boolean startIt) throws FreeboxException {
        post(deviceSubPath(vmId), startIt ? "start" : "powerbutton");
    }
}
