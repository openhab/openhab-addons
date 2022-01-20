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
package org.openhab.binding.robonect.internal.model.cmd;

import org.openhab.binding.robonect.internal.model.VersionInfo;

/**
 * Queries version information about the mower and the module. See {@link VersionInfo}
 * for more information.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class VersionCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=version";
    }
}
