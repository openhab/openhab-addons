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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.openhab.binding.robonect.internal.RobonectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The command allows to set or retrieve the mower name.
 *
 * @author Marco Meyer - Initial contribution
 */
public class NameCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(NameCommand.class);

    private String newName;

    /**
     * sets the mower name.
     *
     * @param newName - the mower name.
     * @return - the command instance.
     */
    public NameCommand withNewName(String newName) {
        this.newName = newName != null ? newName : "";
        return this;
    }

    /**
     * @param baseURL - will be passed by the {@link RobonectClient} in the form
     *            http://xxx.xxx.xxx/json?
     * @return
     */
    @Override
    public String toCommandURL(String baseURL) {
        if (newName == null) {
            return baseURL + "?cmd=name";
        } else {
            return baseURL + "?cmd=name&name=" + URLEncoder.encode(newName, StandardCharsets.ISO_8859_1);
        }
    }
}
