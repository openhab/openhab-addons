/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal.config;

import java.util.Collection;

import org.eclipse.smarthome.config.core.ParameterOption;

/**
 * The {@link WebThingsConnectorConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Sven Schneider - Initial contribution
 */
public class WebThingsServerConfiguration {

    /**
     * Server configuration parameters.
     */
    public Integer port;
    public Collection<ParameterOption> things;
    public boolean linked;
    public boolean allThings;
}
