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

import org.openhab.binding.robonect.internal.RobonectClient;

/**
 * The command starts the mower if it was stopped.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class StartCommand implements Command {

    /**
     * {@inheritDoc}
     * 
     * @param baseURL - will be passed by the {@link RobonectClient} in the form
     *            http://xxx.xxx.xxx/json?
     * @return
     */
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=start";
    }
}
