/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tellstick.internal.conf;

/**
 * Configuration class for {@link TellstickBridge} bridge used to connect to the
 * Telldus Core service on the local machine.
 *
 * @author Jarle Hjortland - Initial contribution
 */
public class TellstickBridgeConfiguration {
    public int resendInterval;
    public String libraryPath;
}
