/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.speedporthybrid.internal;

/**
 * The {@link SpeedportHybridConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Henning Treu - Initial contribution
 */
public class SpeedportHybridConfiguration {

    /**
     * The host address of the SpeedportHybrid router.
     */
    public String host;

    /**
     * The device password of the SpeedportHybrid router.
     */
    public String password;

    /**
     * The refresh interval in seconds. Use '0' to disable refresh.
     */
    public int refreshInterval;

}
