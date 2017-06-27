/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model.cmd;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco Meyer - Initial contribution
 */
public class NameCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(NameCommand.class);

    private String newName;

    public NameCommand withNewName(String newName) {
        this.newName = newName != null ? newName : "";
        return this;
    }

    @Override
    public String toCommandURL(String baseURL) {
        if (newName == null) {
            return baseURL + "?cmd=name";
        } else {
            try {
                return baseURL + "?cmd=name&name=" + URLEncoder.encode(newName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error( "Could not encode name {} ",newName, e);
                return baseURL + "?cmd=name";
            }
        }
    }
}