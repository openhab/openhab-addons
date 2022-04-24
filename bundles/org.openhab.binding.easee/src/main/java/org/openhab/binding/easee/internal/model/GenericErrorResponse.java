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
package org.openhab.binding.easee.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * generic class to map json error responses of the API
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GenericErrorResponse {
    int errorCode;
    String errorCodeName = "";
    String title = "";

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCodeName() {
        return errorCodeName;
    }

    public void setErrorCodeName(String errorCodeName) {
        this.errorCodeName = errorCodeName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
