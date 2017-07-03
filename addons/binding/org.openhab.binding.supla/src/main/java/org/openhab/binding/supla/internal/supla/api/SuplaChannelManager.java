package org.openhab.binding.supla.internal.supla.api;

import com.google.common.collect.ImmutableMap;
import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.mappers.Mapper;
import org.openhab.binding.supla.internal.server.http.HttpExecutor;
import org.openhab.binding.supla.internal.server.http.JsonBody;
import org.openhab.binding.supla.internal.server.http.Request;
import org.openhab.binding.supla.internal.server.http.Response;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class SuplaChannelManager implements ChannelManager {
    private static final Map<String, String> TURN_ON_PARAMS = ImmutableMap.<String, String>builder().put("action", "turn-on").build();
    private static final Map<String, String> TURN_OFF_PARAMS = ImmutableMap.<String, String>builder().put("action", "turn-off").build();
    private final Logger logger = LoggerFactory.getLogger(SuplaChannelManager.class);

    private final HttpExecutor httpExecutor;
    private final Mapper mapper;

    public SuplaChannelManager(HttpExecutor httpExecutor, Mapper mapper) {
        this.httpExecutor = checkNotNull(httpExecutor);
        this.mapper = checkNotNull(mapper);
    }

    @Override
    public void turnOn(SuplaChannel channel) {
        turn(channel.getId(), TURN_ON_PARAMS);
    }

    @Override
    public void turnOff(SuplaChannel channel) {
        turn(channel.getId(), TURN_OFF_PARAMS);
    }

    private Response turn(long id, Map<String, String> paramsMap) {
        return httpExecutor.patch(new Request("/channels/" + id), new JsonBody(paramsMap, (JsonMapper) mapper));
    }

    @Override
    public Optional<SuplaChannelStatus> obtainChannelStatus(SuplaChannel channel) {
        final Response response = httpExecutor.get(new Request("/channels/" + channel.getId()));
        if (response.success()) {
            return Optional.of(mapper.to(SuplaChannelStatus.class, response.getResponse()));
        } else {
            return Optional.empty();
        }
    }
}
