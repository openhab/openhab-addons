/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.exceptions;

import org.eclipse.jetty.client.api.ContentResponse;

/**
 * Exception to be thrown when a request made to the Energenie API does not return code 200
 *
 * @author Lyubomir Papazov - Initial contribution
 */
public class UnsuccessfulHttpResponseException extends Exception {

    private ContentResponse contentResponse;

    public UnsuccessfulHttpResponseException(ContentResponse contentResponse) {
        this.contentResponse = contentResponse;
    }

    public ContentResponse getResponse() {
        return contentResponse;
    }
}
