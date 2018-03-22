/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal.model.cmd;

import java.io.UnsupportedEncodingException;
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
     * @param newName - the mower name.
     * @return - the command instance.
     */
    public NameCommand withNewName(String newName) {
        this.newName = newName != null ? newName : "";
        return this;
    }

    /**
     * @param baseURL - will be passed by the {@link RobonectClient} in the form 
     *                http://xxx.xxx.xxx/json?
     * @return
     */
    @Override
    public String toCommandURL(String baseURL) {
        if (newName == null) {
            return baseURL + "?cmd=name";
        } else {
            try {
                return baseURL + "?cmd=name&name=" + URLEncoder.encode(newName, StandardCharsets.ISO_8859_1.displayName());
            } catch (UnsupportedEncodingException e) {
                logger.error( "Could not encode name {} ",newName, e);
                return baseURL + "?cmd=name";
            }
        }
    }
}
