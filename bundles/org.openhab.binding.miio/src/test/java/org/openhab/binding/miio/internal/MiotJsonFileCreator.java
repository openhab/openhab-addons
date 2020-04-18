/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.miio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Disabled;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.miot.MiotParseException;
import org.openhab.binding.miio.internal.miot.MiotParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support creation of the miio readme doc
 *
 * Run after adding devices or changing database entries of basic devices
 *
 * Run in IDE with 'run as java application'
 * or run in command line as:
 * mvn exec:java -Dexec.mainClass="org.openhab.binding.miio.internal.ReadmeHelper" -Dexec.classpathScope="test"
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiotJsonFileCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiotJsonFileCreator.class);

    private static final String BASEDIR = "./src/main/resources/database/";

    @Disabled
    public static void main(String[] args) {

        String model = "zhimi.heater.zb1";
        if (args.length > 0) {
            model = args[0];
        }
        LOGGER.info("Processing: {}", model);
        try {
            MiotParser miotParser = MiotParser.parse(model);
            LOGGER.info("urn: ", miotParser.getUrn());
            LOGGER.info("{}", miotParser.getUrnData());
            MiIoBasicDevice device = miotParser.getDevice();
            if (device != null) {
                LOGGER.info("Device: {}", device);
                miotParser.writeDevice(BASEDIR + model + "-miot.json", device);
                // miotParser.writeEmuDevice(BASEDIR + model + "-miotEMU.json", device);
            }
            LOGGER.info("finished");
        } catch (MiotParseException e) {
            LOGGER.info("Error processing model {}: {}", model, e.getMessage());
        } catch (Exception e) {
            LOGGER.info("Failed to initiate http Client: {}", e.getMessage());
        }
    }

}
