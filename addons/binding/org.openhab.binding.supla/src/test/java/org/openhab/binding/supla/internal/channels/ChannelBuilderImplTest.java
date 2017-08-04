package org.openhab.binding.supla.internal.channels;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.junit.Test;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaFunction;
import org.openhab.binding.supla.internal.supla.entities.SuplaType;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openhab.binding.supla.SuplaBindingConstants.*;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class ChannelBuilderImplTest {
    ChannelBuilderImpl channelBuilder = new ChannelBuilderImpl();
    ThingUID thingUID = new ThingUID("q:a:z");

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldBuildSwitchChannel() {

        // given
        SuplaChannel suplaChannel = new SuplaChannel(1, 2, "ca",
                new SuplaType(2, RELAY_CHANNEL_TYPE), new SuplaFunction(4, "s"));

        // when
        final Optional<Map.Entry<Channel, SuplaChannel>> channels = channelBuilder.buildChannel(thingUID, suplaChannel);

        // then
        assertThat(channels).isPresent();
        final Map.Entry<Channel, SuplaChannel> entry = channels.get();
        assertThat(entry.getValue()).isEqualTo(suplaChannel);
        final Channel key = entry.getKey();
        assertThat(key.getAcceptedItemType()).isEqualTo("Switch");
        assertThat(key.getChannelTypeUID()).isEqualTo(new ChannelTypeUID(BINDING_ID, SWITCH_CHANNEL_ID));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldBuildLightChannel() {

        // given
        SuplaChannel suplaChannel = new SuplaChannel(1, 2, "ca",
                new SuplaType(2, RELAY_CHANNEL_TYPE), new SuplaFunction(4, LIGHT_CHANNEL_FUNCTION));

        // when
        final Optional<Map.Entry<Channel, SuplaChannel>> channels = channelBuilder.buildChannel(thingUID, suplaChannel);

        // then
        assertThat(channels).isPresent();
        final Map.Entry<Channel, SuplaChannel> entry = channels.get();
        assertThat(entry.getValue()).isEqualTo(suplaChannel);
        final Channel key = entry.getKey();
        assertThat(key.getAcceptedItemType()).isEqualTo("Switch");
        assertThat(key.getChannelTypeUID()).isEqualTo(new ChannelTypeUID(BINDING_ID, LIGHT_CHANNEL_ID));
    }

    @Test
    public void shouldReturnEmptyIfChannelTypeIsNotRelay() {
        // given
        SuplaChannel suplaChannel = new SuplaChannel(1, 2, "ca",
                new SuplaType(2, "not relay type!!!"), new SuplaFunction(4, "dd"));

        // when
        final Optional<Map.Entry<Channel, SuplaChannel>> channels = channelBuilder.buildChannel(thingUID, suplaChannel);

        // then
        assertThat(channels).isEmpty();
    }
}
