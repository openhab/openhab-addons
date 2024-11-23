/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Implementation of the error command allowing to retrieve the list of errors or resetting the list.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class ErrorCommand implements Command {

    private boolean reset = false;

    /**
     * has to be set to 'true' if the errors should be reset.
     * 
     * @param reset - true if errors should be reset, false if the list of errors should be retrieved.
     * @return
     */
    public ErrorCommand withReset(boolean reset) {
        this.reset = reset;
        return this;
    }

    /**
     * @param baseURL - will be passed by the {@link RobonectClient} in the form
     *            http://xxx.xxx.xxx/json?
     * @return - the command for retrieving or resetting the error list.
     */
    @Override
    public String toCommandURL(String baseURL) {
        if (reset) {
            return baseURL + "?cmd=error&reset";
        } else {
            return baseURL + "?cmd=error";
        }
    }
}
