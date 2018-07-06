/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opendaikin.internal.config;

/**
 * Holds configuration data for a Daikin air conditioning unit.
 *
 * @author Tim Waterhouse - Initial contribution
 *
 */
public class OpenDaikinConfiguration {
    public static String HOST = "host";

    public String host;

    public long refresh;
}
