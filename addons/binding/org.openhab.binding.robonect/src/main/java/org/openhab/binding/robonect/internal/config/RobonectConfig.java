/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal.config;

/**
 * 
 * This class acts simply a structure for holding the thing configuration.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class RobonectConfig {
    
    private String host;
    
    private String user;
    
    private String password;
    
    private int pollInterval;
    
    private int offlineTimeout;

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public int getOfflineTimeout() {
        return offlineTimeout;
    }
}
