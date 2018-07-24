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
 * Implementation of the error command allowing to retrieve the list of errors or resetting the list.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class ErrorCommand implements Command {
    
    private boolean reset = false;

    /**
     * has to be set to 'true' if the errors should be reset.
     * @param reset - true if errors should be reset, false if the list of errors should be retrieved.
     * @return
     */
    public ErrorCommand withReset(boolean reset){
        this.reset = reset;
        return this;
    }

    /**
     * @param baseURL - will be passed by the {@link RobonectClient} in the form 
     *                http://xxx.xxx.xxx/json?
     * @return - the command for retrieving or resetting the error list.
     */
    @Override
    public String toCommandURL(String baseURL) {
        if(reset){
            return baseURL + "?cmd=error&reset";
        }else {
            return baseURL + "?cmd=error";
        }
    }
}
