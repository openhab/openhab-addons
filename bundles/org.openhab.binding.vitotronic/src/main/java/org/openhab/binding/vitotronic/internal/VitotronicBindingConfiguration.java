/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.vitotronic.internal;

/**
 * The {@link VitotronicBindingConfiguration} class defines variables which are
 * used for the binding configuration.
 *
 * @author Stefan Andres - Initial contribution
 */

public class VitotronicBindingConfiguration {

    public String ipAddress;
    public Integer port;
    public String adapterId;
    public Integer refreshInterval;
}
