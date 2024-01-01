/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
    private static final Map<String, @Nullable String> SUPPORTEDCOMMANDS = getSupportedCommands();

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

            if (SUPPORTEDCOMMANDS.containsKey(codeUpperCase)) {
                String validateSet = SUPPORTEDCOMMANDS.get(codeUpperCase);

                if (validateSet == null) {
                    validateSet = "";
                }

                return new GatewayCommand(codeUpperCase, message, validateSet);
            }
            throw new IllegalArgumentException(String.format("Unsupported gateway code '%s'", code.toUpperCase()));
        }
        throw new IllegalArgumentException(
                String.format("Unable to parse gateway command with code '%s' and message '%s'", code, message));
    }

    private static Map<String, @Nullable String> getSupportedCommands() {
        Map<String, @Nullable String> c = new HashMap<>();

        c.put(GatewayCommandCode.TEMPERATURETEMPORARY, null);
        c.put(GatewayCommandCode.TEMPERATURECONSTANT, null);
        c.put(GatewayCommandCode.TEMPERATUREOUTSIDE, null);
        c.put(GatewayCommandCode.SETCLOCK, null);
        c.put(GatewayCommandCode.HOTWATER, null);
        c.put(GatewayCommandCode.PRINTREPORT, "A,B,C,G,I,L,M,O,P,R,S,T,V,W");
        c.put(GatewayCommandCode.PRINTSUMMARY, "0,1");
        c.put(GatewayCommandCode.GATEWAY, "0,1,R");
        c.put(GatewayCommandCode.LEDA, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LEDB, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LEDC, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LEDD, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LEDE, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.LEDF, "R,X,T,B,O,F,H,W,C,E,M,P");
        c.put(GatewayCommandCode.GPIOA, "0,1,2,3,4,5,6,7");
        c.put(GatewayCommandCode.GPIOB, "0,1,2,3,4,5,6,7");
        c.put(GatewayCommandCode.SETBACK, null);
        c.put(GatewayCommandCode.TEMPERATURESENSOR, "O,R");
        c.put(GatewayCommandCode.ADDALTERNATIVE, null);
        c.put(GatewayCommandCode.DELETEALTERNATIVE, null);
        c.put(GatewayCommandCode.UNKNOWNID, null);
        c.put(GatewayCommandCode.KNOWNID, null);
        c.put(GatewayCommandCode.PRIORITYMESSAGE, null);
        c.put(GatewayCommandCode.SETRESPONSE, null);
        c.put(GatewayCommandCode.CLEARRESPONSE, null);
        c.put(GatewayCommandCode.SETPOINTHEATING, null);
        c.put(GatewayCommandCode.SETPOINTWATER, null);
        c.put(GatewayCommandCode.MAXIMUMMODULATION, null);
        c.put(GatewayCommandCode.CONTROLSETPOINT, null);
        c.put(GatewayCommandCode.CONTROLSETPOINT2, null);
        c.put(GatewayCommandCode.CENTRALHEATING, "0,1");
        c.put(GatewayCommandCode.CENTRALHEATING2, "0,1");
        c.put(GatewayCommandCode.VENTILATIONSETPOINT, null);
        c.put(GatewayCommandCode.RESET, null);
        c.put(GatewayCommandCode.IGNORETRANSITION, "0,1");
        c.put(GatewayCommandCode.OVERRIDEHIGHBYTE, "0,1");
        c.put(GatewayCommandCode.FORCETHERMOSTAT, "0,1");
        c.put(GatewayCommandCode.VOLTAGEREFERENCE, "0,1,2,3,4,5,6,7,8,9");
        c.put(GatewayCommandCode.DEBUGPOINTER, null);

        return c;
    }
}
