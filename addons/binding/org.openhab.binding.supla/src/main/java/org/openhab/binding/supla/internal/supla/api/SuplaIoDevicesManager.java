package org.openhab.binding.supla.internal.supla.api;

import com.google.gson.reflect.TypeToken;
import org.openhab.binding.supla.internal.api.IoDevicesManager;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.server.http.HttpExecutor;
import org.openhab.binding.supla.internal.server.http.Request;
import org.openhab.binding.supla.internal.server.http.Response;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class SuplaIoDevicesManager implements IoDevicesManager {
    private static final Type MAP_TYPE = new TypeToken<Map<String, List<SuplaIoDevice>>>() {
    }.getType();
    private static final Type LIST_TYPE = new TypeToken<List<SuplaIoDevice>>() {
    }.getType();
    private static final String KEY_FOR_IO_DEVICES = "iodevices";

    private final Logger logger = LoggerFactory.getLogger(SuplaIoDevicesManager.class);
    private final HttpExecutor httpExecutor;
    private final JsonMapper jsonMapper;

    public SuplaIoDevicesManager(HttpExecutor httpExecutor, JsonMapper jsonMapper) {
        this.httpExecutor = checkNotNull(httpExecutor);
        this.jsonMapper = checkNotNull(jsonMapper);
    }

    @Override
    public List<SuplaIoDevice> obtainIoDevices() {
        final Response response = httpExecutor.get(new Request("/iodevices"));
        final Map<String, List<SuplaIoDevice>> map = jsonMapper.to(MAP_TYPE, response.getResponse());

        return map.get(KEY_FOR_IO_DEVICES);
    }

    @Override
    public Optional<SuplaIoDevice> obtainIoDevice(long id) {
        logger.trace("SuplaIoDevicesManager.obtainIoDevice({})", id);
        final Response response = httpExecutor.get(new Request("/iodevices/" + id));
        final List<SuplaIoDevice> list = jsonMapper.to(LIST_TYPE, response.getResponse());
        switch (list.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(list.get(0));
            default:
                throw new IllegalArgumentException(format("List size was %s, full list: %s", list.size(), list));
        }
    }
}
