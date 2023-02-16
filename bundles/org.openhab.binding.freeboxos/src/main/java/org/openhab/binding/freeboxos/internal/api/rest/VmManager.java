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
package org.openhab.binding.freeboxos.internal.api.rest;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.THING_VM;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link VmManager} is the Java class used to handle api requests related to virtual machines
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VmManager extends ListableRest<VmManager.VirtualMachine, VmManager.VirtualMachineResponse> {

    protected class VirtualMachineResponse extends Response<VirtualMachine> {
    }

    public static enum Status {
        STOPPED,
        RUNNING,
        UNKNOWN;
    }

    public static record VirtualMachine(int id, String name, MACAddress mac, Status status) {
    }

    public VmManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.VM, VirtualMachineResponse.class,
                session.getUriBuilder().path(THING_VM));
=======
import org.openhab.binding.freeboxos.internal.api.rest.LoginManager.Session.Permission;
=======
>>>>>>> 9aef877 Rebooting Home Node part

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link VmManager} is the Java class used to handle api requests related to virtual machines
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VmManager extends ListableRest<VmManager.VirtualMachine, VmManager.VirtualMachineResponse> {

    protected class VirtualMachineResponse extends Response<VirtualMachine> {
    }

    public static enum Status {
        STOPPED,
        RUNNING,
        UNKNOWN;
    }

    public static record VirtualMachine(int id, String name, MACAddress mac, Status status) {
    }

    public VmManager(FreeboxOsSession session) throws FreeboxException {
<<<<<<< Upstream, based on origin/main
<<<<<<< Upstream, based on origin/main
        super(session, Permission.VM, VirtualMachineResponse.class, session.getUriBuilder().path(PATH));
>>>>>>> e4ef5cc Switching to Java 17 records
=======
        super(session, Permission.VM, VirtualMachineResponse.class, session.getUriBuilder().path(THING_VM));
>>>>>>> f468f3b Enhance usage of global variables
=======
        super(session, LoginManager.Permission.VM, VirtualMachineResponse.class,
                session.getUriBuilder().path(THING_VM));
>>>>>>> 9aef877 Rebooting Home Node part
    }

    public void power(int vmId, boolean startIt) throws FreeboxException {
        post(Integer.toString(vmId), startIt ? "start" : "powerbutton");
    }
}
