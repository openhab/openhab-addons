/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal.dto.changerequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Multiple endpoints support POST changes, for example:
 * <ul>
 * <li>Config: Allows to change the bridge name, dhcp, portalservices, linkbutton
 * <li>Light: Allows to change the name
 * <li>Group: Allows to change the name
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
