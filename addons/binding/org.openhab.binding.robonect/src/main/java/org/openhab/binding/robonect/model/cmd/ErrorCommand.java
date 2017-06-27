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
public class ErrorCommand implements Command {
    
    private boolean reset = false;
    
    public ErrorCommand withReset(boolean reset){
        this.reset = reset;
        return this;
    }
    
    @Override
    public String toCommandURL(String baseURL) {
        if(reset){
            return baseURL + "?cmd=error&reset";
        }else {
            return baseURL + "?cmd=error";
        }
    }
}
