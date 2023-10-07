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
package org.openhab.binding.robonect.internal.model.cmd;

import org.openhab.binding.robonect.internal.model.MowerStatus;

/**
 * Queries the mowers status. The status holds a lot of status information.
 * See {@link MowerStatus}
 * or the documentation at <a href="http://www.robonect.de/viewtopic.php?f=11&t=38">
 * http://www.robonect.de/viewtopic.php?f=11&amp;t=38</a>
 * 
 * @author Marco Meyer - Initial contribution
 */
public class StatusCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=status";
    }
}
