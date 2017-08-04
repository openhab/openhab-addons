package org.openhab.binding.supla.internal.commands;

import com.google.common.collect.Lists;
import org.eclipse.smarthome.core.library.types.*;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.eclipse.smarthome.core.library.types.OnOffType.OFF;
import static org.eclipse.smarthome.core.library.types.OnOffType.ON;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "Duplicates", "WeakerAccess", "unchecked"})
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

    @Test
    public void shouldUpdateStateWithOffValueWhenThereWasAnErrorDuringChannelUpdate() {

        // given
        Consumer<State> updateState = mock(Consumer.class);
        Command command = ON;
        given(channelManager.turnOn(channel)).willReturn(false);

        // when
        executor.execute(updateState, command);

        // then
        verify(updateState).accept(OFF);
    }

    @Test
    public void shouldUpdateStateWithOnValueWhenThereWasAnErrorDuringChannelUpdate() {

        // given
        Consumer<State> updateState = mock(Consumer.class);
        Command command = OFF;
        given(channelManager.turnOff(channel)).willReturn(false);

        // when
        executor.execute(updateState, command);

        // then
        verify(updateState).accept(ON);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldDoNothingOnNullCommand() {

        // given
        Consumer<State> updateState = mock(Consumer.class);
        Command command = null;

        // when
        executor.execute(updateState, command);

        // then
        verifyZeroInteractions(channelManager);
        verifyZeroInteractions(updateState);
    }

    @Test
    public void shouldDoNothingOnUnKnownCommand() {

        // given
        Consumer<State> updateState = mock(Consumer.class);
        List<Command> unknownCommands = Lists.newArrayList(new DateTimeType(), new StringListType(),
                StopMoveType.MOVE, StopMoveType.STOP, RewindFastforwardType.REWIND, RewindFastforwardType.FASTFORWARD,
                new PointType(), PlayPauseType.PLAY, PlayPauseType.PAUSE, PercentType.ZERO, PercentType.HUNDRED,
                OpenClosedType.OPEN, OpenClosedType.CLOSED, NextPreviousType.NEXT, NextPreviousType.PREVIOUS,
                IncreaseDecreaseType.INCREASE, IncreaseDecreaseType.DECREASE);

        // when
        unknownCommands.forEach(command -> executor.execute(updateState, command));

        // then
        verifyZeroInteractions(channelManager);
        verifyZeroInteractions(updateState);
    }
}
