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
package org.openhab.binding.ipcamera.internal;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
    public Set<IpCameraHandler> listOfOnlineCameraHandlers = new CopyOnWriteArraySet<>();
    public Set<IpCameraGroupHandler> listOfGroupHandlers = new CopyOnWriteArraySet<>();
    public Set<String> listOfOnlineCameraUID = new CopyOnWriteArraySet<>();
}
