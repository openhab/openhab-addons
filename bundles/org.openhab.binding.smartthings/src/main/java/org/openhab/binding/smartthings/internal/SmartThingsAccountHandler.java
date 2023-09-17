/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Interface to decouple SmartthingAuthHandler Bridge Handler implementation from other code.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface SmartThingsAccountHandler extends ThingHandler {

    /**
     * @return Returns true if the SmartThings Bridge is authorized.
     */
    boolean isAuthorized();

    /**
     * Calls SmartThings Api to obtain refresh and access tokens and persist data with Thing.
     *
     * @param redirectUrl The redirect url SmartThings calls back to
     * @param reqCode The unique code passed by SmartThings to obtain the refresh and access tokens
     * @return returns the name of the SmartThings user that is authorized
     */
    String authorize(String redirectUrl, String reqCode) throws SmartThingsException;

    /**
     * Formats the Url to use to call SmartThings to authorize the application.
     *
     * @param redirectUri The uri SmartThings will redirect back to
     * @return the formatted url that should be used to call SmartThings Web Api with
     */
    String formatAuthorizationUrl(String redirectUri, String state);
}
