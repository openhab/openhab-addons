package org.openhab.binding.openthermgateway.internal;

import java.util.HashMap;

public class Command {
    private CommandType commandType;
    private String code;
    private String validationSet;

    public CommandType getCommandType() {
        return commandType;
    }

    public String getCode() {
        return code;
    }

    public String getValidationSet() {
        return validationSet;
    }

    public Command(CommandType commandType, String code) {
        this.commandType = commandType;
        this.code = code;
    }

    public Command(CommandType commandType, String code, String validationSet) {
        this(commandType, code);
        this.validationSet = validationSet;
    }

    public String getMessage(String message) {
        return this.code + "=" + message;
    }

    public static final HashMap<CommandType, Command> commands = createCommands();

    private static HashMap<CommandType, Command> createCommands() {
        HashMap<CommandType, Command> c = new HashMap<CommandType, Command>();

        c.put(CommandType.TemperatureTemporary, new Command(CommandType.TemperatureTemporary, "TT"));
        c.put(CommandType.TemperatureConstant, new Command(CommandType.TemperatureConstant, "TC"));
        c.put(CommandType.TemperatureOutside, new Command(CommandType.TemperatureOutside, "OT"));
        c.put(CommandType.SetClock, new Command(CommandType.SetClock, "ST"));
        c.put(CommandType.HotWater, new Command(CommandType.HotWater, "HW"));
        c.put(CommandType.PrintReport, new Command(CommandType.PrintReport, "PR", "A,B,C,G,I,L,M,O,P,R,S,T,V,W"));
        c.put(CommandType.PrintSummary, new Command(CommandType.PrintSummary, "PS", "0,1"));
        c.put(CommandType.GateWay, new Command(CommandType.GateWay, "GW", "0,1,R"));
        c.put(CommandType.LedA, new Command(CommandType.LedA, "LA", "R,X,T,B,O,F,H,W,C,E,M,P"));
        c.put(CommandType.LedB, new Command(CommandType.LedB, "LB", "R,X,T,B,O,F,H,W,C,E,M,P"));
        c.put(CommandType.LedC, new Command(CommandType.LedC, "LC", "R,X,T,B,O,F,H,W,C,E,M,P"));
        c.put(CommandType.LedD, new Command(CommandType.LedD, "LD", "R,X,T,B,O,F,H,W,C,E,M,P"));
        c.put(CommandType.LedE, new Command(CommandType.LedE, "LE", "R,X,T,B,O,F,H,W,C,E,M,P"));
        c.put(CommandType.LedF, new Command(CommandType.LedF, "LF", "R,X,T,B,O,F,H,W,C,E,M,P"));
        c.put(CommandType.GpioA, new Command(CommandType.GpioA, "GA", "0,1,2,3,4,5,6,7"));
        c.put(CommandType.GpioB, new Command(CommandType.GpioB, "GB", "0,1,2,3,4,5,6,7"));
        c.put(CommandType.SetBack, new Command(CommandType.SetBack, "SB"));
        c.put(CommandType.AddAlternative, new Command(CommandType.AddAlternative, "AA"));
        c.put(CommandType.DeleteAlternative, new Command(CommandType.DeleteAlternative, "DA"));
        c.put(CommandType.UnknownID, new Command(CommandType.UnknownID, "UI"));
        c.put(CommandType.KnownID, new Command(CommandType.KnownID, "KI"));
        c.put(CommandType.PriorityMessage, new Command(CommandType.PriorityMessage, "PM"));
        c.put(CommandType.SetResponse, new Command(CommandType.SetResponse, "SR"));
        c.put(CommandType.ClearResponse, new Command(CommandType.ClearResponse, "CR"));
        c.put(CommandType.SetpointHeating, new Command(CommandType.SetpointHeating, "SH"));
        c.put(CommandType.SetpointWater, new Command(CommandType.SetpointWater, "SW"));
        c.put(CommandType.MaximumModulation, new Command(CommandType.MaximumModulation, "MM"));
        c.put(CommandType.ControlSetpoint, new Command(CommandType.ControlSetpoint, "CS"));
        c.put(CommandType.CentralHeating, new Command(CommandType.CentralHeating, "CH", "0,1"));
        c.put(CommandType.VentilationSetpoint, new Command(CommandType.VentilationSetpoint, "VS"));
        c.put(CommandType.Reset, new Command(CommandType.Reset, "RS"));
        c.put(CommandType.IgnoreTransition, new Command(CommandType.IgnoreTransition, "IT", "0,1"));
        c.put(CommandType.OverrideHighbyte, new Command(CommandType.OverrideHighbyte, "OH", "0,1"));
        c.put(CommandType.ForceThermostat, new Command(CommandType.ForceThermostat, "FT", "0,1"));
        c.put(CommandType.VoltageReference, new Command(CommandType.VoltageReference, "VR", "0,1,2,3,4,5,6,7,8,9"));
        c.put(CommandType.DebugPointer, new Command(CommandType.DebugPointer, "DP"));

        return c;
    }
}