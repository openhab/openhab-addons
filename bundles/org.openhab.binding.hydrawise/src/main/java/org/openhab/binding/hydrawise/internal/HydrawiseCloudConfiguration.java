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
package org.openhab.binding.hydrawise.internal;

/**
 * The {@link HydrawiseCloudConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class HydrawiseCloudConfiguration {

    /**
     * Customer API key {@link https://app.hydrawise.com/config/account}
     */
    public String apiKey;

    /**
     * refresh interval in seconds.
     */
    public Integer refresh;

    /**
     * optional id of the controller to connect to
     */
    public Integer controllerId;
}
