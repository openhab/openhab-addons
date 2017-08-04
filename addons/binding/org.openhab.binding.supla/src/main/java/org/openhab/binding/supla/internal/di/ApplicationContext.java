/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.di;

import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.api.ServerInfoManager;
import org.openhab.binding.supla.internal.api.TokenManager;
import org.openhab.binding.supla.internal.channels.ChannelBuilder;
import org.openhab.binding.supla.internal.channels.ChannelBuilderImpl;
import org.openhab.binding.supla.internal.commands.CommandExecutorFactory;
import org.openhab.binding.supla.internal.commands.CommandExecutorFactoryImpl;
import org.openhab.binding.supla.internal.http.HttpExecutor;
import org.openhab.binding.supla.internal.http.JettyHttpExecutor;
import org.openhab.binding.supla.internal.http.OAuthApiHttpExecutor;
import org.openhab.binding.supla.internal.mappers.GsonMapper;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.supla.api.SuplaChannelManager;
import org.openhab.binding.supla.internal.supla.api.SuplaIoDevicesManager;
import org.openhab.binding.supla.internal.supla.api.SuplaServerInfoManager;
import org.openhab.binding.supla.internal.supla.api.SuplaTokenManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public final class ApplicationContext {
    public interface Builder {
        ApplicationContext build(SuplaCloudServer server);
    }

    private final SuplaCloudServer suplaCloudServer;

    // mappers
    private JsonMapper jsonMapper;

    // http
    private HttpExecutor httpExecutor;
    private HttpExecutor noOAuthHttpExecutor;

    // API
    private TokenManager tokenManager;
    private IoDevicesManager ioDevicesManager;
    private ChannelManager channelManager;
    private ServerInfoManager serverInfoManager;

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

    public JsonMapper getJsonMapper() {
        return get(jsonMapper, GsonMapper::new, x -> this.jsonMapper = x);
    }

    public void setJsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public HttpExecutor getHttpExecutor() {
        final HttpExecutor httpExecutor = get(this.httpExecutor, () -> new JettyHttpExecutor(suplaCloudServer), x -> this.httpExecutor = x);
        if (httpExecutor instanceof OAuthApiHttpExecutor) {
            return httpExecutor;
        } else {
            return new OAuthApiHttpExecutor(httpExecutor, getTokenManager());
        }
    }

    public HttpExecutor getNoOAuthHttpExecutor() {
        return get(noOAuthHttpExecutor, () -> new JettyHttpExecutor(suplaCloudServer), x -> this.noOAuthHttpExecutor = x);
    }

    public void setHttpExecutor(HttpExecutor httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    public TokenManager getTokenManager() {
        return get(tokenManager, () -> new SuplaTokenManager(getJsonMapper(), getNoOAuthHttpExecutor(), suplaCloudServer), x -> this.tokenManager = x);
    }

    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public IoDevicesManager getIoDevicesManager() {
        return get(ioDevicesManager, () -> new SuplaIoDevicesManager(getHttpExecutor(), getJsonMapper()), x -> this.ioDevicesManager = x);
    }

    public void setIoDevicesManager(IoDevicesManager ioDevicesManager) {
        this.ioDevicesManager = ioDevicesManager;
    }

    public ChannelManager getChannelManager() {
        return get(channelManager, () -> new SuplaChannelManager(getHttpExecutor(), getJsonMapper()), x -> this.channelManager = x);
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
        return get(commandExecutorFactory, () -> new CommandExecutorFactoryImpl(getChannelManager()), x -> this.commandExecutorFactory = x);
    }

    public void setCommandExecutorFactory(CommandExecutorFactory commandExecutorFactory) {
        this.commandExecutorFactory = commandExecutorFactory;
    }

    public ServerInfoManager getServerInfoManager() {
        return get(serverInfoManager, () -> new SuplaServerInfoManager(getHttpExecutor(), getJsonMapper()), (x) -> this.serverInfoManager = x);
    }

    public void setServerInfoManager(ServerInfoManager serverInfoManager) {
        this.serverInfoManager = serverInfoManager;
    }
}
