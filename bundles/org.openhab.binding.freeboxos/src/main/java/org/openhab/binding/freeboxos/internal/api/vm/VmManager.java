/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;

/**
 * The {@link VmManager} is the Java class used to handle api requests
 * related to virtual machines
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VmManager extends RestManager {
    public VmManager(FreeboxOsSession session) throws FreeboxException {
        super("vm", session, Permission.VM);
    }

    public VirtualMachine getVM(int vmId) throws FreeboxException {
        return get(String.format("%d", vmId), VirtualMachineResponse.class, true);
    }

    public void power(int vmId, boolean startIt) throws FreeboxException {
        post(String.format("%d/%s", vmId, startIt ? "start" : "powerbutton"));
    }

    public List<VirtualMachine> getVms() throws FreeboxException {
        return getList(VirtualMachinesResponse.class, true);
    }

    // Response classes and validity evaluations
    private class VirtualMachineResponse extends Response<VirtualMachine> {
    }

    private class VirtualMachinesResponse extends ListResponse<VirtualMachine> {
    }
}
