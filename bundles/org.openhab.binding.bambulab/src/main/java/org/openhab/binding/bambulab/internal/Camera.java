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
package org.openhab.binding.bambulab.internal;

import static java.lang.Thread.interrupted;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.NO_CAMERA_CERT;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.PrinterChannel.*;
import static org.openhab.binding.bambulab.internal.PrinterConfiguration.Series.X;
import static org.openhab.core.library.types.OnOffType.*;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.openhab.core.types.UnDefType.*;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;

import pl.grzeslowski.jbambuapi.camera.ASeriesCamera;
import pl.grzeslowski.jbambuapi.camera.CameraConfig;
import pl.grzeslowski.jbambuapi.camera.PSeriesCamera;
import pl.grzeslowski.jbambuapi.camera.TlsCamera;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class Camera implements AutoCloseable {
    private final PrinterConfiguration config;
    private final BambuHandler thingHandler;
    private final Logger logger;
    private @Nullable Future<?> cameraFuture;

    Camera(PrinterConfiguration config, BambuHandler thingHandler) {
        this.config = config;
        this.thingHandler = thingHandler;
        this.logger = thingHandler.getLogger();
    }

    void handleCommand(OnOffType command) {
        if (this.config.series == X) {
            logger.warn("Bambulab binding does not support camera for X series!");
            thingHandler.updateState(CHANNEL_CAMERA_RECORD.getName(), OFF);
            thingHandler.updateState(CHANNEL_CAMERA_IMAGE.getName(), UNDEF);
            return;
        }
        close();
        if (command == ON) {
            var config = new CameraConfig(//
                    this.config.hostname, //
                    this.config.cameraPort, //
                    this.config.username, //
                    this.config.accessCode.getBytes(UTF_8), //
                    findCertificate());
            var camera = switch (this.config.series) {
                case A -> new ASeriesCamera(config);
                case P -> new PSeriesCamera(config);
                case X ->
                    throw new UnsupportedOperationException("Bambulab binding does not support camera for X series!");
            };
            try {
                camera.connect();
                this.cameraFuture = thingHandler.getScheduler().submit(() -> job(camera));
            } catch (Exception e) {
                logger.debug("Cannot connect to camera!", e);
                thingHandler.updateState(CHANNEL_CAMERA_RECORD.getName(), OFF);
                thingHandler.updateState(CHANNEL_CAMERA_IMAGE.getName(), UNDEF);
                thingHandler.updateStatus(OFFLINE, COMMUNICATION_ERROR,
                        "@text/printer.handler.init.cannotConnectCamera[\"%s\"]".formatted(e.getLocalizedMessage()));
                return;
            }
        }
        if (command == OFF) {
            thingHandler.updateState(CHANNEL_CAMERA_IMAGE.getName(), NULL);
        }

        thingHandler.updateStatus(ONLINE);
    }

    private void job(TlsCamera camera) {
        try (camera) {
            for (var iterator = camera.iterator(); iterator.hasNext() && !interrupted();) {
                var bytes = iterator.next();
                var state = bytes != null ? new RawType(bytes, "image/jpeg") : NULL;
                thingHandler.updateState(CHANNEL_CAMERA_IMAGE.getName(), state);
            }
        } catch (IOException e) {
            logger.debug("Could not close camera object", e);
        } catch (NoSuchElementException e) {
            logger.debug("No more elements in iterator. Probably socket got broken...", e);
        } catch (Exception e) {
            logger.debug("Generic exception occurred", e);
        } finally {
            logger.debug("The camera has no more elements, turning off the image channel");
            thingHandler.updateState(CHANNEL_CAMERA_RECORD.getName(), OFF);
            thingHandler.updateState(CHANNEL_CAMERA_IMAGE.getName(), UNDEF);
        }
    }

    @Nullable
    private String findCertificate() {
        var cert = this.config.cameraCertificate//
                .stream()//
                .filter(line -> !line.isBlank())//
                .collect(Collectors.joining("\n"));
        if (cert.equalsIgnoreCase(NO_CAMERA_CERT)) {
            return null;
        }
        if (cert.isBlank()) {
            return CameraConfig.BAMBU_CERTIFICATE;
        }
        return cert;
    }

    @Override
    public void close() {
        var localCameraFuture = cameraFuture;
        cameraFuture = null;
        if (localCameraFuture != null) {
            localCameraFuture.cancel(true);
        }
    }
}
