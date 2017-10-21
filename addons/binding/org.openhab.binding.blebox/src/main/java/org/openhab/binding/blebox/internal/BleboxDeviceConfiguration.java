/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.internal;

/**
 * The {@link BleboxDeviceConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Szymon Tokarski - Initial contribution
 */
public class BleboxDeviceConfiguration {
    public static final String IP = "ip";
    public static final String POLL_INTERVAL = "pollingInterval";

    public static final int DEFAULT_POLL_INTERVAL = 10;

    public String ip;
    public Integer pollingInterval;
}
