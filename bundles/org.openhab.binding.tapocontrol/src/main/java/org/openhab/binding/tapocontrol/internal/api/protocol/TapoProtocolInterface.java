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
package org.openhab.binding.tapocontrol.internal.api.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.tapocontrol.internal.dto.TapoBaseRequestInterface;
import org.openhab.binding.tapocontrol.internal.dto.TapoRequest;
import org.openhab.binding.tapocontrol.internal.helpers.TapoCredentials;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

/**
 * Interface for TAPO-Protocol
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public interface TapoProtocolInterface {

    /***********************
     * Login Handling
     **********************/
    public boolean login(TapoCredentials credentials) throws TapoErrorHandler;

    public void logout();

    public boolean isLoggedIn();

    /***********************
     * Request Sender
     **********************/
    /* send synchron request - response will be handled in [responseReceived()] function */
    public void sendRequest(TapoRequest tapoRequest) throws TapoErrorHandler;

    /* send asynchron request - response will be handled in [asyncResponseReceived()] function */
    public void sendAsyncRequest(TapoBaseRequestInterface tapoRequest) throws TapoErrorHandler;

    /************************
     * RESPONSE HANDLERS
     ************************/

    /* handle synchron request-response - pushes TapoResponse to [httpDelegator.handleResponse()]-function */
    public void responseReceived(ContentResponse response, String command) throws TapoErrorHandler;

    /* handle asynchron request-response - pushes TapoResponse to [httpDelegator.handleResponse()]-function */
    public void asyncResponseReceived(String content, String command) throws TapoErrorHandler;

    /************************
     * PRIVATE HELPERS
     ************************/
}
