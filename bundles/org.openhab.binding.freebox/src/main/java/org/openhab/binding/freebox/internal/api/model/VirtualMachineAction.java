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
package org.openhab.binding.freebox.internal.api.model;

import org.openhab.binding.freebox.internal.api.RequestAnnotation;

/**
 * The {@link VirtualMachineAction} is the Java class used to map the
 * response of the login API
 * https://dev.freebox.fr/sdk/os/vm/#
 * Here I used safe on/off to start and gentle stop the VM but note there also
 * exists the hard "stop" action.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@RequestAnnotation(relativeUrl = "vm/", retryAuth = true, method = "POST")
public class VirtualMachineAction extends APIAction {

    public VirtualMachineAction(String vmId, boolean startIt) {
        super(String.format("%s/%s", vmId, startIt ? "start" : "powerbutton"));
    }
}
