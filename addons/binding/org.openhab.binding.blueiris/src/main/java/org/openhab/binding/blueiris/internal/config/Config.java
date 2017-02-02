/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.internal.config;

/**
 * The configuration for the blue iris code.
 *
 * @author David Bennett - Initial Contribution
 */
public class Config {
    public String ipAddress;
    public Integer port;
    public String user;
    public String password;
    public Integer pollInterval;
    public Integer configPollInterval;
}
