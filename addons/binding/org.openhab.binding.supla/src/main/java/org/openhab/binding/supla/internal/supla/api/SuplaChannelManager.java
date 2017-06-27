package org.openhab.binding.supla.internal.supla.api;

import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.mappers.Mapper;
import org.openhab.binding.supla.internal.server.http.HttpExecutor;
import org.openhab.binding.supla.internal.server.http.Request;
import org.openhab.binding.supla.internal.server.http.Response;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class SuplaChannelManager implements ChannelManager {
    private final Logger logger = LoggerFactory.getLogger(SuplaChannelManager.class);

    private final HttpExecutor httpExecutor;
    private final Mapper mapper;

    public SuplaChannelManager(HttpExecutor httpExecutor, Mapper mapper) {
        this.httpExecutor = checkNotNull(httpExecutor);
        this.mapper = checkNotNull(mapper);
    }

    @Override
    public void turnOn(long channelId) {
        logger.warn("turnOn({}) not implemented!", channelId);
    }

    @Override
    public void turnOff(long channelId) {
        logger.warn("turnOff({}) not implemented!", channelId);
    }

    @Override
    public SuplaChannelStatus obtainChannelStatus(long channelId) {
        final Response response = httpExecutor.get(new Request("/channels/" + channelId));
        return mapper.to(SuplaChannelStatus.class, response.getResponse());
    }
}
