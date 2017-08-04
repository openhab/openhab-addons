package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openhab.binding.supla.SuplaBindingConstants.LIGHT_CHANNEL_ID;
import static org.openhab.binding.supla.SuplaBindingConstants.SWITCH_CHANNEL_ID;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@RunWith(MockitoJUnitRunner.class)
public class CommandExecutorFactoryImplTest {
    @InjectMocks
    CommandExecutorFactoryImpl executorFactory;
    @Mock
    ChannelManager channelManager;
    SuplaChannel channel = new SuplaChannel(1, 2, "", null, null);

    @Test
    public void shouldFindCommandExecutorForSwitch() {

        // given
        ChannelUID channelUID = new ChannelUID("1:2:3:" + SWITCH_CHANNEL_ID);

        // when
        final Optional<CommandExecutor> command = executorFactory.findCommand(channel, channelUID);

        // then
        assertThat(command).isPresent();
        assertThat(command).containsInstanceOf(SwitchChannelCommandExecutor.class);
    }

    @Test
    public void shouldFindCommandExecutorForLight() {

        // given
        ChannelUID channelUID = new ChannelUID("1:2:3:" + LIGHT_CHANNEL_ID);

        // when
        final Optional<CommandExecutor> command = executorFactory.findCommand(channel, channelUID);

        // then
        assertThat(command).isPresent();
        assertThat(command).containsInstanceOf(SwitchChannelCommandExecutor.class);
    }

    @Test
    public void shouldReturnEmptyOnUnknownId() throws Exception {

        // given
        ChannelUID channelUID = new ChannelUID("1:2:3:unknown_id");

        // when
        final Optional<CommandExecutor> command = executorFactory.findCommand(channel, channelUID);

        // then
        assertThat(command).isEmpty();
    }
}
