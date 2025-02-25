package org.openhab.binding.bambulab.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.grzeslowski.jbambuapi.PrinterClient;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.AmsControlCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.AmsFilamentSettingCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.AmsUserSettingCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.ChangeFilamentCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.Command;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.InfoCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.IpCamRecordCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.PrintSpeedCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.PushingCommand;
import pl.grzeslowski.jbambuapi.PrinterClient.Channel.SystemCommand;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.verify;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.LedMode.*;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.LedNode.*;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class PrinterActionsTest {
    PrinterActions printerActions = new PrinterActions();
    @Mock
    @Nullable
    PrinterHandler printerHandler;

    @BeforeEach
    void setUp() {
        printerActions.setThingHandler(requireNonNull(printerHandler));
    }

    @ParameterizedTest(name = "{index}: should {0}")
    @MethodSource
    void shouldRunCommand(String command, Command expectedCommand) {
        // when
        printerActions.sendCommand(command);

        // then
        verify(requireNonNull(printerHandler)).sendCommand(expectedCommand);
    }

    static Stream<Arguments> shouldRunCommand() {
        var infoCommandStream = Arrays.stream(InfoCommand.values())//
                .map(value -> Arguments.of("InfoCommand:" + value.name(), value));
        var pushingCommandStream = stream(Arguments.of("PushingCommand:11:22", new PushingCommand(11, 22)));
        var printCommandStream = Arrays.stream(PrinterClient.Channel.PrintCommand.values())//
                .map(value -> Arguments.of("PrintCommand:" + value.name(), value));
        var changeFilamentCommandStream = stream(Arguments.of("ChangeFilamentCommand:11:22:33", new ChangeFilamentCommand(11, 22, 33)));
        var amsUserSettingCommandStream = stream(Arguments.of("AmsUserSettingCommand:11:tRuE:FaLsE", new AmsUserSettingCommand(11, true, false)));
        var amsFilamentSettingCommandStream = stream(Arguments.of("AmsFilamentSettingCommand:11:22:s3:s4:55:66:s7", new AmsFilamentSettingCommand(11, 22, "s3", "s4", 55, 66, "s7")));
        var amsControlCommandStream = Arrays.stream(AmsControlCommand.values())//
                .map(value -> Arguments.of("AmsControlCommand:" + value.name(),value));
        var printSpeedCommandStream = Arrays.stream(PrintSpeedCommand.values())//
                .map(value -> Arguments.of("PrintSpeedCommand:" + value.name(), value));
        var gCodeFileCommandStream = stream(Arguments.of("GCodeFileCommand:s1", new PrinterClient.Channel.GCodeFileCommand("s1")));
        var gCodeLineCommandStream = stream(Arguments.of("GCodeLineCommand:s1:l1:l2:l3", new PrinterClient.Channel.GCodeLineCommand(List.of("l1", "l2", "l3"), "s1")));
        var ledControlCommandStream = stream(//
                Arguments.of("LedControlCommand:CHAMBER_LIGHT:ON", new LedControlCommand(CHAMBER_LIGHT, ON, null, null, null, null)),//
                Arguments.of("LedControlCommand:WORK_LIGHT:OFF", new LedControlCommand(WORK_LIGHT, OFF, null, null, null, null)),//
                Arguments.of("LedControlCommand:CHAMBER_LIGHT:FLASHING:11:22:33:44", new LedControlCommand(CHAMBER_LIGHT, FLASHING, 11, 22, 33, 44))//
        );
        var systemCommandStream = Arrays.stream(SystemCommand.values())//
                .map(value ->Arguments.of( "SystemCommand:" + value.name(), value));
        var ipCamRecordCommandStream = stream(//
                Arguments.of("IpCamRecordCommand:tRue", new IpCamRecordCommand(true)),//
                Arguments.of("IpCamRecordCommand:fAlSe", new IpCamRecordCommand(false))//
        );
        var ipCamTimelapsCommandStream = stream(//
                Arguments.of("IpCamTimelapsCommand:tRue", new PrinterClient.Channel.IpCamTimelapsCommand(true)),//
                Arguments.of("IpCamTimelapsCommand:fAlSe", new PrinterClient.Channel.IpCamTimelapsCommand(false))//
        );
        var xCamControlCommandStream =
                Arrays.stream(PrinterClient.Channel.XCamControlCommand.Module.values())//
                        .map(moduleValue -> Arguments.of("XCamControlCommand:%s:trUE:FAlse".formatted(moduleValue),
                                new PrinterClient.Channel.XCamControlCommand(moduleValue, true, false)));

        return concat(infoCommandStream, pushingCommandStream, printCommandStream,
                changeFilamentCommandStream,
                amsUserSettingCommandStream,
                amsFilamentSettingCommandStream,
                amsControlCommandStream,
                printSpeedCommandStream,
                gCodeFileCommandStream,
                gCodeLineCommandStream,
                ledControlCommandStream,
                systemCommandStream, ipCamRecordCommandStream, ipCamTimelapsCommandStream, xCamControlCommandStream);
    }

    @SafeVarargs
    static Stream<Arguments> concat(Stream<Arguments>... streams) {
        return Stream.of(streams).flatMap(s -> s);
    }

    @SafeVarargs
    static <T> Stream<T> stream(T... values) {
        return Stream.of(values);
    }

}
