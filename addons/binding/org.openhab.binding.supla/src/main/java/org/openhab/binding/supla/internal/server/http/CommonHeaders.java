/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.server.http;

import org.openhab.binding.supla.internal.supla.entities.SuplaToken;

public final class CommonHeaders {
    public static final Header CONTENT_TYPE_JSON = new Header("Content-Type", "application/json; charset=UTF-8");

    private CommonHeaders() {}

    public static Header authorizationHeader(SuplaToken suplaToken) {
        return new Header("Authorization", "Bearer " + suplaToken.getAccessToken());
    }

}
