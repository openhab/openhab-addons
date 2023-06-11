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
package org.openhab.binding.networkupstools.internal.nut;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * API implementation handling communicating with the NUT server.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class NutApi {

    private static final String LIST_VAR = "LIST VAR %s";
    private static final String VAR = "VAR %s";
    private static final String LIST_UPS = "LIST UPS";
    private static final String UPS = "UPS ";
    private static final String GET_VAR = "GET VAR %s %s";

    private final NutResponseReader responseReader = new NutResponseReader();
    private final NutConnector connector;

    /**
     * Constructor.
     *
     * @param host host
     * @param port port
     * @param username username
     * @param password password
     */
    public NutApi(final String host, final int port, final String username, final String password) {
        this.connector = new NutConnector(host, port, username, password);
    }

    /**
     * Constructor for unit tests to inject mock connector.
     *
     * @param connector Connector.
     */
    NutApi(final NutConnector connector) {
        this.connector = connector;
    }

    /**
     * Closes the connector.
     */
    public void close() {
        connector.close();
    }

    /**
     * Retrieves a list of the UPS devices available from the NUT server.
     *
     * @return List of UPS devices
     * @throws NutException Exception in case of any error related to the API.
     */
    public Map<String, String> getUpsList() throws NutException {
        return connector.read(LIST_UPS, r -> responseReader.parseList(UPS, r));
    }

    /**
     * Retrieves a list of the variables available for the given UPS.
     *
     * @param ups UPS to get the variables for
     * @return List of variables for the given UPS
     * @throws NutException Exception in case of any error related to the API.
     */
    public Map<String, String> getVariables(final String ups) throws NutException {
        return connector.read(String.format(LIST_VAR, ups), r -> responseReader.parseList(String.format(VAR, ups), r));
    }

    /**
     * Retrieves the value of the given nut variable for the given UPS.
     *
     * @param ups UPS to get the variables for
     * @param nut The variable to the value for
     * @return Returns the value for the given nut
     * @throws NutException Exception when the variable could not retrieved
     */
    public String getVariable(final String ups, final String nut) throws NutException {
        return connector.read(String.format(GET_VAR, ups, nut), r -> responseReader.parseVariable(ups, nut, r));
    }
}
