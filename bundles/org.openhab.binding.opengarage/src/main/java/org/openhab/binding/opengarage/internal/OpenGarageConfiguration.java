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
package org.openhab.binding.opengarage.internal;

/**
 * The OpenGarageConfiguration class contains fields mapping thing configuration parameters.
 *
 * @author Paul Smedley - Initial contribution
 */
public class OpenGarageConfiguration {
    public String hostname;
    public long port = 80;
    public String password = "opendoor";
    public long refresh = 10;
}
