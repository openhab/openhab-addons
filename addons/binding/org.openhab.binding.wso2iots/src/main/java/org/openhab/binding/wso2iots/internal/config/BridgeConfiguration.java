/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wso2iots.internal.config;

/**
 *
 * @author Ramesha Karunasena - Initial contribution
 */
public class BridgeConfiguration {

    public String hostname;

    public Integer port;

    public Integer refresh;

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

}
