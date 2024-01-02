/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ecowatt.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EcowattConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - New parameter apiVersion
 */
@NonNullByDefault
public class EcowattConfiguration {

    public int apiVersion = 4;
    public String idClient = "";
    public String idSecret = "";
}
