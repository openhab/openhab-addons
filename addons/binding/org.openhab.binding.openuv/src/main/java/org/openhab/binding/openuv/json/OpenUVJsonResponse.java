/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv.json;

/**
 * The {@link OpenUVJsonResponse} is the Java class used to map the JSON
 * response to the OpenUV request.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class OpenUVJsonResponse {

    private OpenUVJsonResult result;
    private String error;

    public OpenUVJsonResponse() {
    }

    public OpenUVJsonResult getResult() {
        return result;
    }

    public String getError() {
        return error;
    }

}
