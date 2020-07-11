/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal.models.userprofile;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Response-Model after signing in
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public class PostSignInResponseModel {

    public String sessionId = "";

    public String userName = "";

    public int errorCode;

    public PostSignInResponseModel withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public PostSignInResponseModel withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public PostSignInResponseModel withErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }
}
