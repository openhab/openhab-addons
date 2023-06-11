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
package org.openhab.io.hueemulation.internal.dto.changerequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Multiple endpoints support PUT changes, for example:
 * <ul>
 * <li>Config: Allows to change the bridge name, dhcp, portalservices, linkbutton
 * <li>Light: Allows to change the name
 * <li>Group: Allows to change the name
 * <li>Sensor: Allows to change the name
 * </ul>
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueChangeRequest {
    public @Nullable String devicename;
    public @Nullable String name;
    public @Nullable Boolean dhcp;
    public @Nullable Boolean linkbutton;
    public @Nullable Boolean portalservices;
}
