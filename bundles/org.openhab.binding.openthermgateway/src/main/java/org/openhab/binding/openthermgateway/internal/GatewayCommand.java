/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.openthermgateway.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GatewayCommand} is used to validate and match commands send through the binding
 * to the OpenTherm gateway device.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class GatewayCommand {
    private static final Map<String, @Nullable String> supportedCommands = getSupportedCommands();

    private String code;
    private String validationSet;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getValidationSet() {
        return validationSet;
    }

    public String toFullString() {
        return this.code + "=" + this.message;
    }

    private GatewayCommand(String code, String message, String validationSet) throws IllegalArgumentException {
        this.code = code;
        this.message = message;
        this.validationSet = validationSet;

        if (!validate()) {
            throw new IllegalArgumentException(
                    String.format("Invalid value '%s' for code '%s'", this.message, this.code));
        }
    }

    private boolean validate() {
        if (this.validationSet.isEmpty()) {
            return true;
        }

        String[] validations = this.validationSet.split(",");

        for (String validation : validations) {
            if (this.message.equals(validation)) {
                return true;
            }
        }

        return false;
    }

    public static GatewayCommand parse(@Nullable String code, String message) throws IllegalArgumentException {
        if ((code == null || code.isEmpty()) && message.length() > 2 && message.charAt(2) == '=') {
            return parse(message.substring(0, 2), message.substring(3));
        }

        if (code != null && code.length() == 2) {
            String codeUpperCase = code.toUpperCase();

            if (supportedCommands.containsKey(codeUpperCase)) {
                String validateSet = supportedCommands.get(codeUpperCase);

                if (validateSet == null) {
                    validateSet = "";
                }

                return new GatewayCommand(codeUpperCase, message, validateSet);
            } else {
                throw new IllegalArgumentException(String.format("Unsupported gateway code '%s'", code.toUpperCase()));
            }
        }

        throw new IllegalArgumentException(
                String.format("Unable to parse gateway command with code '%s' and message '%s'", code, message));
    }

    private static Map<String, @Nullable String> getSupportedCommands() {
        Map<String, @Nullable String> c = new HashMap<>();

        c.put(GatewayCommandCode.TemperatureTemporary, null);
        c.put(GatewayCommandCode.TemperatureConstant, null);
        c.put(GatewayCommandCode.TemperatureOutside, null);
        c.put(GatewayCommandCode.SetClock, null);
        c.put(GatewayCommandCode.HotWater, null);
        c.put(GatewayCommandCode.PrintReport, "A,B,C,G,I,L,M,O,P,R,S,T,V,W");
        c.put(GatewayCommandCode.PrintSummary, "0,1");
        c.put(GatewayCommandCode.GateWay, "0,1,R");
        c.put(GatewayCommandCode.LedA, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LedB, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LedC, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LedD, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LedE, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LedF, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.GpioA, "0,1,2,3,4,5,6,7");
        c.put(GatewayCommandCode.GpioB, "0,1,2,3,4,5,6,7");
        c.put(GatewayCommandCode.SetBack, null);
        c.put(GatewayCommandCode.TemperatureSensor, "O,R");
        c.put(GatewayCommandCode.AddAlternative, null);
        c.put(GatewayCommandCode.DeleteAlternative, null);
        c.put(GatewayCommandCode.UnknownID, null);
        c.put(GatewayCommandCode.KnownID, null);
        c.put(GatewayCommandCode.PriorityMessage, null);
        c.put(GatewayCommandCode.SetResponse, null);
        c.put(GatewayCommandCode.ClearResponse, null);
        c.put(GatewayCommandCode.SetpointHeating, null);
        c.put(GatewayCommandCode.SetpointWater, null);
        c.put(GatewayCommandCode.MaximumModulation, null);
        c.put(GatewayCommandCode.ControlSetpoint, null);
        c.put(GatewayCommandCode.ControlSetpoint2, null);
        c.put(GatewayCommandCode.CentralHeating, "0,1");
        c.put(GatewayCommandCode.CentralHeating2, "0,1");
        c.put(GatewayCommandCode.VentilationSetpoint, null);
        c.put(GatewayCommandCode.Reset, null);
        c.put(GatewayCommandCode.IgnoreTransition, "0,1");
        c.put(GatewayCommandCode.OverrideHighbyte, "0,1");
        c.put(GatewayCommandCode.ForceThermostat, "0,1");
        c.put(GatewayCommandCode.VoltageReference, "0,1,2,3,4,5,6,7,8,9");
        c.put(GatewayCommandCode.DebugPointer, null);

        return c;
    }
}
