/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.somfycul.internal;

import static org.openhab.binding.somfycul.internal.SomfyCULBindingConstants.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyCULHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marc Klasser - Initial contribution
 */
@NonNullByDefault
public class SomfyCULHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyCULHandler.class);

    // private @Nullable SomfyCULConfiguration config;
    private File propertyFile;
    private Properties p;

    /**
     * Initializes the thing. As persistent state is necessary the properties are stored in the user data directory and
     * fetched within the constructor.
     *
     * @param thing
     */
    public SomfyCULHandler(Thing thing) {
        super(thing);
        String somfyFolderName = OpenHAB.getUserDataFolder() + File.separator + "somfycul";
        File folder = new File(somfyFolderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        propertyFile = new File(
                somfyFolderName + File.separator + thing.getUID().getAsString().replace(':', '_') + ".properties");
        p = initProperties();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("channelUID: {}, command: {}", channelUID, command);
        SomfyCommand somfyCommand = null;
        if (channelUID.getId().equals(POSITION)) {
            if (command instanceof UpDownType) {
                switch ((UpDownType) command) {
                    case UP:
                        somfyCommand = SomfyCommand.UP;
                        break;
                    case DOWN:
                        somfyCommand = SomfyCommand.DOWN;
                        break;
                }
            } else if (command instanceof StopMoveType) {
                switch ((StopMoveType) command) {
                    case STOP:
                        somfyCommand = SomfyCommand.MY;
                        break;
                    default:
                        break;
                }

            }
        } else if (channelUID.getId().equals(PROGRAM)) {
            if (command instanceof OnOffType) {
                // Don't check for on/off - always trigger program mode
                somfyCommand = SomfyCommand.PROG;
            }
        }
        Bridge bridge = getBridge();
        if (somfyCommand != null && bridge != null) {
            // We delegate the execution to the bridge handler
            ThingHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler instanceof CULHandler) {
                logger.debug("rolling code before command {}", p.getProperty("rollingCode"));

                String rollingCode = String.valueOf(p.getProperty("rollingCode"));
                String address = String.valueOf(p.getProperty("address"));
                boolean executedSuccessfully = ((CULHandler) bridgeHandler).executeCULCommand(getThing(), somfyCommand,
                        rollingCode, address);
                if (executedSuccessfully && command instanceof State) {
                    updateState(channelUID, (State) command);

                    String rollingCodeStr = String.valueOf(p.getProperty("rollingCode"));
                    long newRollingCode = Long.decode("0x" + rollingCodeStr) + 1;
                    String newRollingCodeStr = String.format("%04X", newRollingCode);
                    p.setProperty("rollingCode", newRollingCodeStr);
                    logger.debug("Updated rolling code to {}", newRollingCodeStr);
                    p.setProperty("address", String.valueOf(p.getProperty("address")));

                    try {
                        p.store(new FileWriter(propertyFile), "Last command: " + somfyCommand);
                    } catch (IOException e) {
                        logger.error("Error occurred on writing the property file.", e);
                    }
                }
            }
        }
    }

    /**
     * The roller shutter is by default initialized and set to online, as there is no feedback that can check if the
     * shutter is available.
     */
    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        updateStatus(ThingStatus.ONLINE);
        logger.debug("Finished initializing!");
    }

    /**
     * Initializes the properties for the thing (shutter).
     *
     * @return Valid properties (address and rollingCode)
     */
    private Properties initProperties() {
        p = new Properties();

        try {
            if (!propertyFile.exists()) {
                logger.debug("Trying to create file {}.", propertyFile);
                FileWriter fileWriter = new FileWriter(propertyFile);
                p.setProperty("rollingCode", "0000");
                p.setProperty("address", String.format("%06X", getNewAddressForShutter()));
                p.store(fileWriter, "Initialized fields");
                fileWriter.close();
                logger.info("Created new property file {}", propertyFile);
            } else {
                FileReader fileReader = new FileReader(propertyFile);
                p.load(fileReader);
                fileReader.close();
                logger.info("Read properties from file {}", propertyFile);
            }
        } catch (IOException e) {
            logger.error("Error occurred on writing the property file.", e);
        }
        return p;
    }

    /**
     * Calculates a new address for the shutter. Therefore all property files are read and a new address is calculated.
     *
     * @return New 6-digit address for the shutter
     * @throws IOException
     */
    private long getNewAddressForShutter() throws IOException {
        File directory = propertyFile.getParentFile();
        if (directory == null) {
            throw new IOException("Cannot access parent directory for property files");
        }

        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Cannot list files in directory: " + directory.getAbsolutePath());
        }

        long maxAddress = 0;
        for (File file : files) {
            String extension = null;
            // Get file extension
            if (file.getName().contains(".")) {
                extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            }
            if (extension != null && "properties".equals(extension) && !file.equals(propertyFile)) {
                logger.info("Parsing properties from file {}", file);
                Properties other = new Properties();
                try (FileReader fileReader = new FileReader(file)) {
                    other.load(fileReader);
                    String addressStr = other.getProperty("address");
                    if (addressStr != null) {
                        long currentAddress = Long.decode("0x" + addressStr);
                        if (currentAddress > maxAddress) {
                            maxAddress = currentAddress;
                        }
                    }
                }
            }
        }
        logger.info("Current max address is {}", maxAddress);
        return maxAddress + 1;
    }
}
