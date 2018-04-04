/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal.config;

/**
 * The {@link FoxtrotConfiguration} class contains fields mapping thing configuration paramters.
 *
 * @author Radovan Sninsky - Initial contribution
 */
public class FoxtrotConfiguration {

    /**
     * Host address or IP of the PLCComS.
     */
    public String hostname;

    /**
     * Port of web service of the PLCComS.
     */
    public int port;
}
