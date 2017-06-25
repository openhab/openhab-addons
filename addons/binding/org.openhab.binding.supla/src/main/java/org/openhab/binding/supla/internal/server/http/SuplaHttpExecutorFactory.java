package org.openhab.binding.supla.internal.server.http;

import org.openhab.binding.supla.internal.server.SuplaCloudServer;
import org.openhab.binding.supla.internal.server.mappers.Mapper;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SuplaHttpExecutorFactory implements HttpExecutorFactory {
    private final Mapper mapper;

    public SuplaHttpExecutorFactory(Mapper mapper) {
        this.mapper = checkNotNull(mapper);
    }

    public HttpExecutor get(SuplaCloudServer server) {
        return new SuplaHttpExecutor(server, mapper);
    }
}
