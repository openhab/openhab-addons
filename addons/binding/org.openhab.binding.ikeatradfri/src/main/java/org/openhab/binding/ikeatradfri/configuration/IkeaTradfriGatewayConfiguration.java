/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ikeatradfri.configuration;

/**
 * The {@link IkeaTradfriGatewayConfiguration} is responsible for holding
 * configuration information needed to access/poll the IKEA Tradfri gateway
 *
 * @author Daniel Sundberg - Initial contribution
 */
public class IkeaTradfriGatewayConfiguration {

    public static final String HOST = "host";
    public static final String TOKEN = "token";

    public String host;
    public String token;
}
