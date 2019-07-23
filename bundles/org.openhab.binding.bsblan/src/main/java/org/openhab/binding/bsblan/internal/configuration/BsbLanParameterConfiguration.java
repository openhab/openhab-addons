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
package org.openhab.binding.bsblan.internal.configuration;

import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterSetRequest;

/**
 * The {@link BsbLanParameterConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanParameterConfiguration {
    public Integer id;

    public Integer setId;

    public BsbLanApiParameterSetRequest.Type setType;
}
