/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.supla.api;

import com.google.common.collect.ImmutableMap;
import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.http.HttpExecutor;
import org.openhab.binding.supla.internal.http.JsonBody;
import org.openhab.binding.supla.internal.http.Request;
import org.openhab.binding.supla.internal.http.Response;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public final class SuplaChannelManager implements ChannelManager {
    private static final Map<String, String> TURN_ON_PARAMS = ImmutableMap.<String, String>builder().put("action", "turn-on").build();
    private static final Map<String, String> TURN_OFF_PARAMS = ImmutableMap.<String, String>builder().put("action", "turn-off").build();

    private final Logger logger = LoggerFactory.getLogger(SuplaChannelManager.class);

    private final HttpExecutor httpExecutor;
    private final JsonMapper jsonMapper;

    public SuplaChannelManager(HttpExecutor httpExecutor, JsonMapper jsonMapper) {
        this.httpExecutor = checkNotNull(httpExecutor);
        this.jsonMapper = checkNotNull(jsonMapper);
    }

    @Override
    public boolean turnOn(SuplaChannel channel) {
        logger.debug("Turning channel {} ON", channel);
        return turn(channel.getId(), TURN_ON_PARAMS).success();
    }

    @Override
    public boolean turnOff(SuplaChannel channel) {
        logger.debug("Turning channel {} OFF", channel);
        return turn(channel.getId(), TURN_OFF_PARAMS).success();
    }

    private Response turn(long id, Map<String, String> paramsMap) {
        return httpExecutor.patch(new Request("/channels/" + id), new JsonBody(paramsMap, jsonMapper));
    }

    @Override
    public Optional<SuplaChannelStatus> obtainChannelStatus(SuplaChannel channel) {
        final Response response = httpExecutor.get(new Request("/channels/" + channel.getId()));
        if (response.success()) {
            return Optional.of(jsonMapper.to(SuplaChannelStatus.class, response.getResponse()));
        } else {
            return Optional.empty();
        }
    }
}
