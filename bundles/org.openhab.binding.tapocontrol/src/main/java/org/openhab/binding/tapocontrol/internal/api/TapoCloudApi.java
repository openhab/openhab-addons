/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.api;

//import static org.openhab.binding.tapocontrol.internal.TapoControlBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler class for TAPO Smart Home device cloud-connections.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoCloudApi {
    public static final String HTTP_HEADER_AUTH = "Authorization";
    public static final String HTTP_AUTH_TYPE_BASIC = "Basic";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    private final Logger logger = LoggerFactory.getLogger(TapoCloudApi.class);
    private final HttpClient httpClient;

    /**
     * INIT CLASS
     * 
     */
    public TapoCloudApi() {
        this.httpClient = new HttpClient();
    }

    /**
     * Initializes a connection to the given url.
     *
     * @param ipAddress ip address of the connection
     * @return ResponseContent of HTTP Request
     */
    /*
     * private ContentResponse QueryCloud(String url, String payload) {
     * logger.trace("query tapo cloud '{}'", url);
     * 
     * Request request = httpClient.POST(url);
     * request.header(HttpHeader.ACCEPT, CONTENT_TYPE_JSON);
     * request.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
     * request.content(new StringContentProvider(payload), CONTENT_TYPE_JSON);
     * 
     * ContentResponse response = request.send();
     * String res = new String(response.getContent());
     * 
     * logger.trace("{}: HTTP Response {}: {}", "TAPO_CLOUD_CONNECT", response.getStatus(), res);
     * 
     * httpClient.stop();
     * 
     * return res;
     * 
     * return null;
     * }
     */

    /**
     * Query token from tapo-cloud
     *
     * @param ipAddress ip address of the connection
     */
    /*
     * private getToken( email, password, terminalUUID ){
     * String url = TAPO_CLOUD_URL;
     * String payload = {
     * "method": "login",
     * "params": {
     * "appType": "Tapo_Ios",
     * "cloudUserName": email,
     * "cloudPassword": password,
     * "terminalUUID": terminalUUID
     * };
     * 
     * String json = QueryCloud( url, payload );
     * String token = ['result']['token'];
     * return token;
     * }
     */
    /**
     * Query types from tapo-cloud
     *
     * @param ipAddress ip address of the connection
     */
    /*
     * public GetDevices(){
     * 
     * String url = TAPO_CLOUD_URL + "?token=" + getToken( CONFIG_EMAIL, CONFIG_PASS );
     * String payload ={
     * "method": "getDeviceList",
     * };
     * 
     * String json = QueryCloud( url, payload );
     * return json;
     * }
     */
}
