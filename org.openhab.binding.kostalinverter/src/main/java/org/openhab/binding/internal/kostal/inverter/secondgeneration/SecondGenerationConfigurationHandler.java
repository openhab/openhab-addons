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

package org.openhab.binding.internal.kostal.inverter.secondgeneration;

/**
 * The {@link SecondGenerationConfigurationHandler} class defines methods used for configuration of dxsId's, which are
 * used in the second generation part of the binding.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */

public class SecondGenerationConfigurationHandler {
    // Handler for authorization and changes of dxsId values
    public static int configurationHandler(String url, String username, String password, String dxsIdConf,
            String valueConf) throws Exception {
        String urlLogin = url + "/api/login.json";
        String[] getResponse = SecondGenerationLoginGet.loginGet(urlLogin, username, password);
        if (getResponse[0].contentEquals("6")) {
            int faultCode = 6;
            return faultCode;
        }
        String salt = getResponse[0];
        String sessionId = getResponse[1];

        String urlLoginPost = urlLogin + "?sessionId=" + sessionId;
        int response = SecondGenerationLoginPost.loginPost(urlLoginPost, username, password, salt);

        String urlDxsIdPost = url + "/api/dxs.json?sessionId=" + sessionId;
        response = SecondGenerationPost.postValue(urlDxsIdPost, username, dxsIdConf, valueConf);

        return response;
    }
}
