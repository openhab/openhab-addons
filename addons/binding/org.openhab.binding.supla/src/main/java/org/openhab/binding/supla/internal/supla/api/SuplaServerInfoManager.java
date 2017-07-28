/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.supla.api;

import com.google.gson.reflect.TypeToken;
import org.openhab.binding.supla.internal.api.ServerInfoManager;
import org.openhab.binding.supla.internal.http.HttpExecutor;
import org.openhab.binding.supla.internal.http.Request;
import org.openhab.binding.supla.internal.http.Response;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.supla.entities.SuplaServerInfo;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public final class SuplaServerInfoManager implements ServerInfoManager {
    private static final Type MAP_TYPE = new TypeToken<Map<String, SuplaServerInfo>>() {
    }.getType();
    private static final String KEY_FOR_SERVER_INFO = "data";
    private final HttpExecutor httpExecutor;
    private final JsonMapper jsonMapper;

    public SuplaServerInfoManager(HttpExecutor httpExecutor, JsonMapper jsonMapper) {
        this.httpExecutor = checkNotNull(httpExecutor);
        this.jsonMapper = checkNotNull(jsonMapper);
    }

    @Override
    public Optional<SuplaServerInfo> obtainServerInfo() {
        final Response response = httpExecutor.get(new Request("/server-info"));
        final Map<String, SuplaServerInfo> map = jsonMapper.to(MAP_TYPE, response.getResponse());
        if (map.containsKey(KEY_FOR_SERVER_INFO)) {
            return Optional.of(map.get(KEY_FOR_SERVER_INFO));
        } else {
            return Optional.empty();
        }
    }
}
