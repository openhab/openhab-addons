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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyCULHandler} handles roller shutter commands.
 *
 * Properties are persisted in the user data folder per Thing UID.
 * Initialization is async and repeated on enable/disable.
 *
 * @author Marc Klasser - Initial contribution
 */
@NonNullByDefault
public class SomfyCULHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyCULHandler.class);
    private final Bundle bundle;
    private final LocaleProvider localeProvider;
    private final TranslationProvider i18nProvider;
    private @Nullable File propertyFile = null;
    private @Nullable Properties properties = null;

    /**
     * Initializes the thing. As persistent state is necessary, the properties are stored in the user data directory and
     * fetched within the initialization.
     *
     * @param thing the Thing instance to be handled
     * @param localeProvider the provider for locale information
     * @param i18nProvider the provider for internationalization/translation
     */
    public SomfyCULHandler(Thing thing, LocaleProvider localeProvider, TranslationProvider i18nProvider) {
        super(thing);
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
        this.bundle = FrameworkUtil.getBundle(CULHandler.class);
    }

    /**
     * The roller shutter is initialized and set to online by default, as there is no feedback that can check if the
     * shutter is available, other than being able to read the properties file.
     */
    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                String somfyFolderName = OpenHAB.getUserDataFolder() + File.separator + "somfycul";
                File folder = new File(somfyFolderName);
                if (!folder.exists() && !folder.mkdirs()) {
                    throw new IOException("Cannot create directory: " + folder.getAbsolutePath());
                }

                propertyFile = new File(somfyFolderName + File.separator
                        + getThing().getUID().getAsString().replace(':', '_') + ".properties");

                loadOrCreateProperties();

                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                logger.warn("Failed to initialize SomfyCULHandler for {}: {}", getThing().getUID(), e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        i18nProvider.getText(bundle, "offline.init-error", "Initialization failed: {0}",
                                localeProvider.getLocale(), e.getMessage()));
            }
        });
    }

    private void loadOrCreateProperties() throws IOException {
        File file = propertyFile;
        if (file == null) {
            throw new IOException("Property file not initialized");
        }

        Properties p = new Properties();

        if (!file.exists()) {

            File parent = file.getParentFile();
            if (parent == null) {
                throw new IOException("Cannot access parent directory for property files");
            }

            long newAddress = computeNewAddress(parent);

            p.setProperty("rollingCode", "0000");
            p.setProperty("address", String.format("%06X", newAddress));

            try (FileWriter fw = new FileWriter(file)) {
                p.store(fw, "Initialized fields");
            }
        } else {
            try (FileReader fr = new FileReader(file)) {
                p.load(fr);
            }
        }

        this.properties = p;
    }

    private long computeNewAddress(File directory) throws IOException {
        File[] files = directory.listFiles((d, name) -> name != null && name.endsWith(".properties"));
        if (files == null) {
            throw new IOException("Cannot list files in " + directory.getAbsolutePath());
        }

        long maxAddr = 0;
        for (File f : files) {
            if (f.equals(propertyFile)) {
                continue;
            }

            Properties other = new Properties();
            try (FileReader fr = new FileReader(f)) {
                other.load(fr);
                String addr = other.getProperty("address");
                if (addr != null) {
                    try {
                        long val = Long.decode("0x" + addr);
                        maxAddr = Math.max(maxAddr, val);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return maxAddr + 1;
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);

        this.propertyFile = null;
        this.properties = null;

        initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Properties p = properties;
        File file = propertyFile;

        if (p == null || file == null) {
            logger.warn("Ignoring command — properties not yet loaded");
            return;
        }

        SomfyCommand somfyCommand = null;

        switch (channelUID.getId()) {
            case POSITION:
                if (command instanceof UpDownType upDownCommand) {
                    switch (upDownCommand) {
                        case UP -> somfyCommand = SomfyCommand.UP;
                        case DOWN -> somfyCommand = SomfyCommand.DOWN;
                    }
                } else if (command instanceof StopMoveType stopMoveCommand) {
                    if (stopMoveCommand == StopMoveType.STOP) {
                        somfyCommand = SomfyCommand.MY;
                    }
                }
                break;

            case PROGRAM:
                if (command instanceof OnOffType) {
                    // Don't check for on/off - always trigger program mode
                    somfyCommand = SomfyCommand.PROG;
                }
                break;
        }

        if (somfyCommand == null) {
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        ThingHandler handler = bridge.getHandler();
        if (!(handler instanceof CULHandler cul)) {
            return;
        }

        String rollingCode = p.getProperty("rollingCode");
        String address = p.getProperty("address");

        if (rollingCode == null || address == null) {
            return;
        }

        final SomfyCommand finalCommand = somfyCommand;

        boolean ok = cul.executeCULCommand(getThing(), somfyCommand, rollingCode, address);
        if (!ok) {
            return;
        }

        if (command instanceof State state) {
            updateState(channelUID, state);
        }

        long newRolling = (Long.decode("0x" + rollingCode) + 1) & 0xFFFF;
        String newStr = String.format("%04X", newRolling);
        p.setProperty("rollingCode", newStr);

        scheduler.execute(() -> {
            try (FileWriter fw = new FileWriter(file)) {
                p.store(fw, "Last command: " + finalCommand);
            } catch (IOException e) {
                logger.warn("Error writing property file: {}", e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        properties = null;
        propertyFile = null;
        super.dispose();
    }
}
