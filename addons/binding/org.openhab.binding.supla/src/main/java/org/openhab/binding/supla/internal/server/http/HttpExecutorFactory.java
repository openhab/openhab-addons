package org.openhab.binding.supla.internal.server.http;

import org.openhab.binding.supla.internal.server.SuplaCloudServer;

public interface HttpExecutorFactory {
    HttpExecutor get(SuplaCloudServer server);
}
