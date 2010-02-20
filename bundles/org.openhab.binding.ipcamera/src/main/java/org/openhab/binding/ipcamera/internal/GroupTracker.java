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

package org.openhab.binding.ipcamera.internal;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ipcamera.internal.handler.IpCameraGroupHandler;
import org.openhab.binding.ipcamera.internal.handler.IpCameraHandler;

/**
 * The {@link GroupTracker} is used so a 'group' thing can get a handle to each cameras handler, and the group and
 * cameras can talk to each other.
 *
 * @author Matthew Skinner - Initial contribution
 */

@NonNullByDefault
public class GroupTracker {
    public ArrayList<IpCameraHandler> listOfOnlineCameraHandlers = new ArrayList<>(1);
    public ArrayList<IpCameraGroupHandler> listOfGroupHandlers = new ArrayList<>(0);
    public ArrayList<String> listOfOnlineCameraUID = new ArrayList<>(1);
}
