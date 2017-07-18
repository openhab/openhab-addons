package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.http.Response;
import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

import static java.lang.String.format;

public class TokenException extends RuntimeException {
    public TokenException(Response response, SuplaCloudServer server) {
        super(format("Got error %s while obtaining token for server %s!", response.getStatusCode(), server.getServer()));
    }
}
