/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal.model.cmd;

import org.openhab.binding.robonect.internal.RobonectClient;

/**
 * 
 * Interface implemented by all commands. The robonect module is called with urls like 
 * http://xxx.xxx.xxx/json?cmd=[command]. The command implementation is responsible to construct the full command url.
 * 
 * 
 * @author Marco Meyer - Initial contribution
 */
public interface Command {

    /**
     * Implementations of this interface have to return baseUrl + command specific extensions, where the baseURL 
     * already is in the form http://xxx.xxx.xxx/json?
     * @param baseURL - will be passed by the {@link RobonectClient} in the form 
     *                http://xxx.xxx.xxx/json?
     * @return - the full command string like for example for a name command http://xxx.xxx.xxx/json?cmd=name
     */
    String toCommandURL(String baseURL);
}
