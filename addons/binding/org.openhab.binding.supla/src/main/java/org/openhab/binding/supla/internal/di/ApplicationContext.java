package org.openhab.binding.supla.internal.di;

import org.openhab.binding.supla.internal.server.SuplaCloudServer;
import org.openhab.binding.supla.internal.api.TokenManager;
import org.openhab.binding.supla.internal.api.SuplaTokenManager;
import org.openhab.binding.supla.internal.server.http.HttpExecutor;
import org.openhab.binding.supla.internal.server.http.SuplaHttpExecutor;
import org.openhab.binding.supla.internal.server.mappers.JsonMapper;
import org.openhab.binding.supla.internal.server.mappers.Mapper;

import java.util.function.Supplier;

public class ApplicationContext {

    // mappers
    private Mapper mapper;

    // http
    private HttpExecutor httpExecutor;

    // API
    private TokenManager tokenManager;

    private <T> T get(T instance, Supplier<T> supplier) {
        if(instance != null) {
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

    public HttpExecutor getHttpExecutor(SuplaCloudServer server) {
        return get(httpExecutor, () -> new SuplaHttpExecutor(server));
    }

    public void setHttpExecutor(HttpExecutor httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public TokenManager getTokenManager(SuplaCloudServer server) {
        return get(tokenManager,  ()-> new SuplaTokenManager(new JsonMapper(), getHttpExecutor(server), server));
    }

    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }
}
