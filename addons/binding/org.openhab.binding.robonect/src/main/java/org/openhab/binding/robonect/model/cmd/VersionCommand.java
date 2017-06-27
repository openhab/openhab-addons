/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model.cmd;

/**
 * @author Marco Meyer - Initial contribution
 */
public class VersionCommand implements Command {
    @Override
    public String toCommandURL(String baseURL) {
        return baseURL + "?cmd=version";
    }
    
}
