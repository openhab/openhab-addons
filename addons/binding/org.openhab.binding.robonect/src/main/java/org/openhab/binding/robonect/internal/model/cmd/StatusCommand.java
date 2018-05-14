/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal.model.cmd;

import org.openhab.binding.robonect.internal.model.MowerStatus;

/**
 * Queries the mowers status. The status holds a lot of status information. 
 * See {@link MowerStatus}
 * or the documentation at: http://www.robonect.de/viewtopic.php?f=11&t=38
 * 
 * @author Marco Meyer - Initial contribution
 */
public class StatusCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=status";
    }
}
