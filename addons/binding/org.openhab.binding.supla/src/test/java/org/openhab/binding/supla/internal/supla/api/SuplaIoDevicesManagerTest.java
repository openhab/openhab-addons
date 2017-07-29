package org.openhab.binding.supla.internal.supla.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.SuplaTest;
import org.openhab.binding.supla.internal.http.HttpExecutor;
import org.openhab.binding.supla.internal.http.Request;
import org.openhab.binding.supla.internal.http.Response;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.supla.entities.SuplaIoDevice;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class SuplaIoDevicesManagerTest extends SuplaTest {

    @InjectMocks private SuplaIoDevicesManager manager;

    @Mock private JsonMapper jsonMapper;
    @Mock private HttpExecutor httpExecutor;

    @Test
    public void shouldObtainAllDevices() {

        // given
        List<SuplaIoDevice> devices = new ArrayList<>();
        devices.add(randomSuplaIoDevice());
        devices.add(randomSuplaIoDevice());

        given(httpExecutor.get(new Request("/iodevices"))).willReturn(new Response(200, "resp"));

        Map<String, List<SuplaIoDevice>> map = ImmutableMap.<String, List<SuplaIoDevice>>builder().put("iodevices", devices).build();
        given(jsonMapper.to(any(Type.class), eq("resp"))).willReturn(map);

        // when
        final List<SuplaIoDevice> suplaIoDevices = manager.obtainIoDevices();

        // then
        assertThat(suplaIoDevices).isEqualTo(devices);
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
