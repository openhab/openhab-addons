/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coolmasternet.internal.config;

/**
 * The {@link ControllerConfiguration} is responsible for holding configuration information needed to access/poll the
 * CoolMasterNet Controller.
 *
 * @author Angus Gratton - Initial contribution
 * @author Wouter Born - Split Controller and HVAC configurations
 */
public class ControllerConfiguration {

    public static final String HOST = "host";
    public String host;

    public static final String PORT = "port";
    public int port = 10102;

    public static final String REFRESH = "refresh";
    public int refresh = 5; // seconds

}
