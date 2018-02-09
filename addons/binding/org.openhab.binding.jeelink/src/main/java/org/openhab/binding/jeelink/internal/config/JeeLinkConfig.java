/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.config;

/**
 * Configuration for a JeeLinkHandler.
 *
 * @author Volker Bier - Initial contribution
 */
public class JeeLinkConfig {
    public String ipAddress;
    public Integer port;
    public String serialPort;
    public Integer baudRate;
    public String initCommands;
}
