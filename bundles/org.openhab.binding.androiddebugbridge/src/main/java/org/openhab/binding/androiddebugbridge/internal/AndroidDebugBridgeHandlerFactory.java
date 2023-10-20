/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.androiddebugbridge.internal;

import static org.openhab.binding.androiddebugbridge.internal.AndroidDebugBridgeBindingConstants.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbCrypto;

/**
 * The {@link AndroidDebugBridgeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = BINDING_CONFIGURATION_PID, service = { ThingHandlerFactory.class,
        AndroidDebugBridgeHandlerFactory.class })
public class AndroidDebugBridgeHandlerFactory extends BaseThingHandlerFactory {
    private static final Path ADB_FOLDER = Path.of(OpenHAB.getUserDataFolder(), ".adb");
    private final Logger logger = LoggerFactory.getLogger(AndroidDebugBridgeHandlerFactory.class);
    private @Nullable AdbCrypto adbCrypto;
    private final AndroidDebugBridgeDynamicCommandDescriptionProvider commandDescriptionProvider;

    @Activate
    public AndroidDebugBridgeHandlerFactory(
            final @Reference AndroidDebugBridgeDynamicCommandDescriptionProvider commandDescriptionProvider) {
        this.commandDescriptionProvider = commandDescriptionProvider;
        initADBCrypto();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_ANDROID_DEVICE.equals(thingTypeUID)) {
            return new AndroidDebugBridgeHandler(thing, commandDescriptionProvider, adbCrypto);
        }
        return null;
    }

    public @Nullable AdbCrypto getAdbCrypto() {
        return adbCrypto;
    }

    private void initADBCrypto() {
        try {
            if (!Files.exists(ADB_FOLDER) || !Files.isDirectory(ADB_FOLDER)) {
                Files.createDirectory(ADB_FOLDER);
                logger.info("Binding folder {} created", ADB_FOLDER);
            }
            adbCrypto = loadKeyPair(ADB_FOLDER.resolve("adb_pub.key"), ADB_FOLDER.resolve("adb.key"));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            logger.warn("Unable to setup adb keys: {}", e.getMessage());
        }
    }

    private static AdbCrypto loadKeyPair(Path pubKey, Path privKey)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        AdbCrypto adbCrypto = null;
        Charset asciiCharset = StandardCharsets.US_ASCII;
        AdbBase64 bytesToString = bytes -> new String(Base64.getEncoder().encode(bytes), asciiCharset);
        // load key pair
        if (Files.exists(pubKey) && Files.exists(privKey)) {
            try {
                adbCrypto = AdbCrypto.loadAdbKeyPair(bytesToString, privKey.toFile(), pubKey.toFile());
            } catch (IOException ignored) {
                // Keys don't exits
            }
        }
        if (adbCrypto == null) {
            // generate key pair
            adbCrypto = AdbCrypto.generateAdbKeyPair(bytesToString);
            adbCrypto.saveAdbKeyPair(privKey.toFile(), pubKey.toFile());
        }
        return adbCrypto;
    }
}
