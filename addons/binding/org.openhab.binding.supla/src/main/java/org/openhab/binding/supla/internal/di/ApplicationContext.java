package org.openhab.binding.supla.internal.di;

import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.api.TokenManager;
import org.openhab.binding.supla.internal.channels.ChannelBuilder;
import org.openhab.binding.supla.internal.channels.ChannelBuilderImpl;
import org.openhab.binding.supla.internal.commands.CommandExecutorFactory;
import org.openhab.binding.supla.internal.commands.CommandExecutorFactoryImpl;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.mappers.Mapper;
import org.openhab.binding.supla.internal.server.http.HttpExecutor;
import org.openhab.binding.supla.internal.server.http.OAuthApiHttpExecutor;
import org.openhab.binding.supla.internal.server.http.SuplaHttpExecutor;
import org.openhab.binding.supla.internal.supla.api.SuplaChannelManager;
import org.openhab.binding.supla.internal.supla.api.SuplaIoDevicesManager;
import org.openhab.binding.supla.internal.supla.api.SuplaTokenManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ApplicationContext {

    private final SuplaCloudServer suplaCloudServer;

    // mappers
    private Mapper mapper;

    // http
    private HttpExecutor httpExecutor;
    private HttpExecutor noOAuthHttpExecutor;

    // API
    private TokenManager tokenManager;
    private IoDevicesManager ioDevicesManager;
    private ChannelManager channelManager;

    // OpenHab
    private ChannelBuilder channelBuilder;
    private CommandExecutorFactory commandExecutorFactory;

    public ApplicationContext(SuplaCloudServer suplaCloudServer) {
        this.suplaCloudServer = checkNotNull(suplaCloudServer);
    }

    public SuplaCloudServer getSuplaCloudServer() {
        return suplaCloudServer;
    }

    private <T> T get(T instance, Supplier<T> supplier, Consumer<T> setter) {
        if (instance != null) {
            return instance;
        } else {
            final T t = supplier.get();
            setter.accept(t);
            return t;
        }
    }

    public Mapper getMapper() {
        return get(mapper, JsonMapper::new, x -> this.mapper = x);
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public HttpExecutor getHttpExecutor() {
        final HttpExecutor httpExecutor = get(this.httpExecutor, () -> new SuplaHttpExecutor(suplaCloudServer), x -> this.httpExecutor = x);
        if (httpExecutor instanceof OAuthApiHttpExecutor) {
            return httpExecutor;
        } else {
            return new OAuthApiHttpExecutor(httpExecutor, getTokenManager());
        }
    }

    public HttpExecutor getNoOAuthHttpExecutor() {
        return get(noOAuthHttpExecutor, () -> new SuplaHttpExecutor(suplaCloudServer), x -> this.noOAuthHttpExecutor = x);
    }

    public void setHttpExecutor(HttpExecutor httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    public TokenManager getTokenManager() {
        return get(tokenManager, () -> new SuplaTokenManager(new JsonMapper(), getNoOAuthHttpExecutor(), suplaCloudServer), x -> this.tokenManager = x);
    }

    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public IoDevicesManager getIoDevicesManager() {
        return get(ioDevicesManager, () -> new SuplaIoDevicesManager(getHttpExecutor(), new JsonMapper()), x -> this.ioDevicesManager = x);
    }

    public void setIoDevicesManager(IoDevicesManager ioDevicesManager) {
        this.ioDevicesManager = ioDevicesManager;
    }

    public ChannelManager getChannelManager() {
        return get(channelManager, () -> new SuplaChannelManager(getHttpExecutor(), getMapper()), x -> this.channelManager = x);
    }

    public void setChannelManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public ChannelBuilder getChannelBuilder() {
        return get(channelBuilder, () -> new ChannelBuilderImpl(), (x) -> this.channelBuilder = x);
    }

    public void setChannelBuilder(ChannelBuilder channelBuilder) {
        this.channelBuilder = channelBuilder;
    }

    public CommandExecutorFactory getCommandExecutorFactory() {
        return get(commandExecutorFactory, () -> new CommandExecutorFactoryImpl(), x -> this.commandExecutorFactory = x);
    }

    public void setCommandExecutorFactory(CommandExecutorFactory commandExecutorFactory) {
        this.commandExecutorFactory = commandExecutorFactory;
    }
}
