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
package org.openhab.binding.linky.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
public interface LinkyAccountHandler {

    /**
     * @return Returns true if the Linky Bridge is authorized.
     */
    boolean isAuthorized();

    /**
     * Calls Smartthings Api to obtain refresh and access tokens and persist data with Thing.
     *
     * @param redirectUrl The redirect url Smartthings calls back to
     * @param reqCode The unique code passed by Smartthings to obtain the refresh and access tokens
     * @return returns the name of the Smartthings user that is authorized
     */
    String authorize(String redirectUrl, String reqState, String reqCode) throws LinkyException;

    /**
     * Formats the Url to use to call Smartthings to authorize the application.
     *
     * @param redirectUri The uri Smartthings will redirect back to
     * @return the formatted url that should be used to call Smartthings Web Api with
     */
    String formatAuthorizationUrl(String redirectUri);

    List<String> getAllPrmId();
}
