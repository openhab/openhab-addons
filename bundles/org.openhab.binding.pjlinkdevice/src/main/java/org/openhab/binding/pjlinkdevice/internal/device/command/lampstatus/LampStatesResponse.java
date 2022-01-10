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
package org.openhab.binding.pjlinkdevice.internal.device.command.lampstatus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * The response part of {@link LampStatesCommand}
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class LampStatesResponse extends PrefixedResponse<List<LampStatesResponse.LampState>> {
    static final Pattern RESPONSE_VALIDATION_PATTERN = Pattern.compile("^((\\d+) ([01]))( (\\d+) ([01]))*$");
    static final Pattern RESPONSE_PARSING_PATTERN = Pattern.compile("(?<hours>\\d+) (?<active>[01])");

    @NonNullByDefault
    public class LampState {
        private boolean active;
        private int lampHours;

        public LampState(boolean active, int lampHours) {
            this.active = active;
            this.lampHours = lampHours;
        }

        public int getLampHours() {
            return lampHours;
        }

        public boolean isActive() {
            return active;
        }
    }

    public LampStatesResponse(String response) throws ResponseException {
        super("LAMP=", response);
    }

    @Override
    protected List<LampStatesResponse.LampState> parseResponseWithoutPrefix(String responseWithoutPrefix)
            throws ResponseException {
        // validate if response fully matches specification
        if (!RESPONSE_VALIDATION_PATTERN.matcher(responseWithoutPrefix).matches()) {
            throw new ResponseException(
                    MessageFormat.format("Lamp status response could not be parsed: ''{0}''", responseWithoutPrefix));
        }

        // go through individual matches for each lamp
        List<LampStatesResponse.LampState> result = new ArrayList<>();
        Matcher matcher = RESPONSE_PARSING_PATTERN.matcher(responseWithoutPrefix);
        while (matcher.find()) {
            int lampHours = Integer.parseInt(matcher.group("hours"));
            boolean active = matcher.group("active").equals("1");
            result.add(new LampState(active, lampHours));
        }
        return result;
    }
}
