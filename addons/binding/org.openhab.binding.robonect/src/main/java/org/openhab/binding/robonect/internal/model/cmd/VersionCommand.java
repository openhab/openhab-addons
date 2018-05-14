/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
