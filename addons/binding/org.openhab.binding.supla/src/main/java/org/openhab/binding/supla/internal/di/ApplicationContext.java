package org.openhab.binding.supla.internal.di;

import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.api.SuplaIoDevicesManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;
import org.openhab.binding.supla.internal.api.TokenManager;
import org.openhab.binding.supla.internal.api.SuplaTokenManager;
import org.openhab.binding.supla.internal.server.http.HttpExecutor;
import org.openhab.binding.supla.internal.server.http.SuplaHttpExecutor;
import org.openhab.binding.supla.internal.server.mappers.JsonMapper;
import org.openhab.binding.supla.internal.server.mappers.Mapper;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApplicationContext {

    private final SuplaCloudServer suplaCloudServer;

    // mappers
    private Mapper mapper;

    // http
    private HttpExecutor httpExecutor;

    // API
    private TokenManager tokenManager;
    private IoDevicesManager ioDevicesManager;

    public ApplicationContext(SuplaCloudServer suplaCloudServer) {
        this.suplaCloudServer = checkNotNull(suplaCloudServer);
    }

    private <T> T get(T instance, Supplier<T> supplier) {
        if (instance != null) {
            return instance;
        } else {
            return supplier.get();
        }
    }

    public Mapper getMapper() {
        return get(mapper, JsonMapper::new);
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public HttpExecutor getHttpExecutor() {
        return get(httpExecutor, () -> new SuplaHttpExecutor(suplaCloudServer));
    }

    public void setHttpExecutor(HttpExecutor httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    public TokenManager getTokenManager() {
        return get(tokenManager, () -> new SuplaTokenManager(new JsonMapper(), getHttpExecutor(), suplaCloudServer));
    }

    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public IoDevicesManager getIoDevicesManager() {
        return get(ioDevicesManager, () -> new SuplaIoDevicesManager(getTokenManager(), getHttpExecutor(), new JsonMapper()));
    }

    public void setIoDevicesManager(IoDevicesManager ioDevicesManager) {
        this.ioDevicesManager = ioDevicesManager;
    }
}
