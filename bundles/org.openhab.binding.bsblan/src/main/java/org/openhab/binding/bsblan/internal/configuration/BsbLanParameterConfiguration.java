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
package org.openhab.binding.bsblan.internal.configuration;

/**
 * The {@link BsbLanParameterConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanParameterConfiguration {
    /**
     * Parameter Id (ProgNr) to query
     */
    public Integer id;

    /**
     * Parameter Id (ProgNr) used for change requests.
     */
    public Integer setId;

    /**
     * Command type used for change requests (INF or SET)
     */
    public String setType;
}
