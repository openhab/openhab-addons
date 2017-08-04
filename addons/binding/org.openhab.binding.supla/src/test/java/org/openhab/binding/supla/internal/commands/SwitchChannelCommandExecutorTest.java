package org.openhab.binding.supla.internal.commands;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;

import java.util.Optional;
import java.util.function.Consumer;

import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "Duplicates", "WeakerAccess"})
@RunWith(MockitoJUnitRunner.class)
public class SwitchChannelCommandExecutorTest {
    SwitchChannelCommandExecutor executor;
    @Mock
    ChannelManager channelManager;
    SuplaChannel channel = new SuplaChannel(1, 2, "", null, null);

    @Before
    public void init() {
        executor = new SwitchChannelCommandExecutor(channelManager, channel);
        given(channelManager.obtainChannelStatus(channel)).willReturn(Optional.of(new SuplaChannelStatus(true, true)));
    }

    @Test
    public void shouldObtainChannelStatusForRefresh() {

        // given
        Consumer<State> updateState = state -> {
        };
        Command command = REFRESH;

        // when
        executor.execute(updateState, command);

        // then
        verify(channelManager).obtainChannelStatus(channel);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRefreshChannelWithOnCommand() {

        // given
        Consumer<State> updateState = mock(Consumer.class);
        Command command = REFRESH;

        SuplaChannelStatus status = new SuplaChannelStatus(true, true);
        given(channelManager.obtainChannelStatus(channel)).willReturn(Optional.of(status));

        // when
        executor.execute(updateState, command);

        // then
        verify(updateState).accept(ON);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRefreshChannelWithOffCommand() {

        // given
        Consumer<State> updateState = mock(Consumer.class);
        Command command = REFRESH;

        SuplaChannelStatus status = new SuplaChannelStatus(true, false);
        given(channelManager.obtainChannelStatus(channel)).willReturn(Optional.of(status));

        // when
        executor.execute(updateState, command);

        // then
        verify(updateState).accept(OFF);
    }

    @Test
    public void shouldHandleOnCommand() {

        // given
        Consumer<State> updateState = state -> {
        };
        Command command = ON;

        // when
        executor.execute(updateState, command);

        // then
        verify(channelManager).turnOn(channel);
    }

    @Test
    public void shouldHandleOffCommand() {

        // given
        Consumer<State> updateState = state -> {
        };
        Command command = OFF;

        // when
        executor.execute(updateState, command);

        // then
        verify(channelManager).turnOff(channel);
    }
}
