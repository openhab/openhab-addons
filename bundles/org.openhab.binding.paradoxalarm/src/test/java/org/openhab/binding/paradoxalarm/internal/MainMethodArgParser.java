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
package org.openhab.binding.paradoxalarm.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MainMethodArgParser} Helper class to parse the arguments of main method, which is used for direct
 * communication testing.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class MainMethodArgParser {

    private final Logger logger = LoggerFactory.getLogger(MainMethodArgParser.class);

    private static final int NUMBER_OF_ARGUMENTS = 8;
    private static final String USAGE_TEXT = "Usage: application --password <YOUR_PASSWORD_FOR_IP150> --pc_password <your PC_password> --ip_address <address of IP150> --port <port of Paradox>\n (pc password default is 0000, can be obtained by checking section 3012), default port is 10000";

    private static final String KEY_PREFIX = "--";

    private static final String PASSWORD_KEY = "--password";
    private static final String PC_PASSWORD_KEY = "--pc_password";
    private static final String IP_ADDRESS_KEY = "--ip_address";
    private static final String PORT_KEY = "--port";
    private static final List<String> ARGUMENT_KEYS = Arrays.asList(PASSWORD_KEY, PC_PASSWORD_KEY, IP_ADDRESS_KEY,
            PORT_KEY);

    private Map<String, String> argumentsMap;

    public MainMethodArgParser(String[] args) {
        this.argumentsMap = parseArguments(args);
        validateArguments();
    }

    private Map<String, String> parseArguments(String[] args) {
        if (args == null || args.length < NUMBER_OF_ARGUMENTS) {
            logger.error(USAGE_TEXT);
            throw new IllegalArgumentException("Arguments= " + Arrays.asList(args));
        }

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < args.length;) {
            if (!args[i].startsWith(KEY_PREFIX)) {
                throw new IllegalArgumentException("Argument " + args[i] + " does not start with --");
            }
            String key = args[i];
            String value;
            if (args[i + 1] != null && args[i + 1].startsWith(KEY_PREFIX)) {
                value = null;
                i++;
            } else {
                value = args[i + 1];
                i += 2;
            }
            result.put(key, value);

        }
        return result;
    }

    private void validateArguments() {
        for (String argKey : ARGUMENT_KEYS) {
            String value = argumentsMap.get(argKey);
            if (value == null) {
                logger.error(USAGE_TEXT);
                throw new IllegalArgumentException("Argument " + argKey + "is mandatory");
            }
        }
    }

    public String getPassword() {
        return argumentsMap.get(PASSWORD_KEY);
    }

    public String getPcPassword() {
        return argumentsMap.get(PC_PASSWORD_KEY);
    }

    public String getIpAddress() {
        return argumentsMap.get(IP_ADDRESS_KEY);
    }

    public String getPort() {
        return argumentsMap.get(PORT_KEY);
    }
}
