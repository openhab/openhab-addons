package org.openhab.binding.supla.internal.supla.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.SuplaTest;
import org.openhab.binding.supla.internal.http.HttpExecutor;
import org.openhab.binding.supla.internal.http.Request;
import org.openhab.binding.supla.internal.http.Response;
import org.openhab.binding.supla.internal.mappers.GsonMapper;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class SuplaIoDevicesManagerTest extends SuplaTest {

    private SuplaIoDevicesManager manager;
    @Mock private HttpExecutor httpExecutor;
    private final JsonMapper jsonMapper = new GsonMapper();

    @Before
    public void init() {
        manager = new SuplaIoDevicesManager(httpExecutor, jsonMapper);
    }

    @Test
    public void shouldObtainAllDevices() {

        // given
        final SuplaIoDevice device1 = randomSuplaIoDevice();
        final SuplaIoDevice device2 = randomSuplaIoDevice();

        final String json = "{\"iodevices\": ["+jsonMapper.map(device1)+", "+jsonMapper.map(device2)+"]}";
        given(httpExecutor.get(new Request("/iodevices"))).willReturn(new Response(200, json));

        // when
        final List<SuplaIoDevice> suplaIoDevices = manager.obtainIoDevices();

        // then
        assertThat(suplaIoDevices).containsExactly(device1, device2);
    }

    @Test
    public void shouldReturnIoDeviceWhenHttpExecutorReturnsOneInstance() {

        // given
        final int id = 100;
        final String json = "[{\"id\":" + id + ", \"channels\": []}]";
        given(httpExecutor.get(new Request("/iodevices/" + id))).willReturn(new Response(200, json));

        // when
        final Optional<SuplaIoDevice> suplaIoDevice = manager.obtainIoDevice(id);

        // then
        assertThat(suplaIoDevice).contains(new SuplaIoDevice(id, 0, false, null, null, null, null, null, null, 0, null));
    }

    private SuplaIoDevice randomSuplaIoDevice() {
        final Random random = new Random();
        return new SuplaIoDevice(random.nextInt(100),
                random.nextInt(100),
                random.nextBoolean(),
                "name",
                "comment",
                null,
                null,
                "guid",
                random.nextInt(5) + ".0",
                random.nextInt(5),
                null);
    }

}
