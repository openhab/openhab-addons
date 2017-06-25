package org.openhab.binding.supla.internal.di;

import org.openhab.binding.supla.internal.server.SuplaCloudServer;
import org.openhab.binding.supla.internal.api.TokenManager;
import org.openhab.binding.supla.internal.api.SuplaTokenManager;
import org.openhab.binding.supla.internal.server.http.HttpExecutorFactory;
import org.openhab.binding.supla.internal.server.http.SuplaHttpExecutorFactory;
import org.openhab.binding.supla.internal.server.mappers.JsonMapper;
import org.openhab.binding.supla.internal.server.mappers.Mapper;

import java.util.function.Supplier;

public class ApplicationContext {
    public static final ApplicationContext INSTANCE = new ApplicationContext();

    // mappers
    private Mapper mapper;

    // http
    private HttpExecutorFactory httpExecutorFactory;

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

    public HttpExecutorFactory getHttpExecutorFactory() {
        return get(httpExecutorFactory, () -> new SuplaHttpExecutorFactory(getMapper()));
    }

    public void setHttpExecutorFactory(HttpExecutorFactory httpExecutorFactory) {
        this.httpExecutorFactory = httpExecutorFactory;
    }

    public TokenManager getTokenManager(SuplaCloudServer server) {
        return get(tokenManager,  ()-> new SuplaTokenManager(new JsonMapper(), getHttpExecutorFactory(), server));
    }

    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }
}
