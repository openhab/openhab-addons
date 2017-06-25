package org.openhab.binding.supla.internal.server.http;

import org.openhab.binding.supla.internal.server.SuplaToken;

public final class CommonHeaders {
    public static final Header CONTENT_TYPE_JSON = new Header("Content-Type", "application/json; charset=UTF-8");

    private CommonHeaders() {}

    public static Header AUTHORIZATION_HEADER(SuplaToken suplaToken) {
        return new Header("Authorization", "Bearer " + suplaToken.getToken());
    }

}
