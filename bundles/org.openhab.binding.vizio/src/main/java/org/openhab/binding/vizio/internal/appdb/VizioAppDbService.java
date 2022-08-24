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
package org.openhab.binding.vizio.internal.appdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vizio.internal.dto.applist.VizioApps;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link VizioAppDbService} class makes available a list of known apps on Vizio TVs.
 *
 * @author Michael Lobstein - Initial Contribution
 */

@Component(service = VizioAppDbService.class)
@NonNullByDefault
public class VizioAppDbService {
    private final Logger logger = LoggerFactory.getLogger(VizioAppDbService.class);
    private VizioApps vizioApps;

    @Activate
    public VizioAppDbService() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/db/apps.json");
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);) {
            Gson gson = new Gson();
            vizioApps = gson.fromJson(reader, VizioApps.class);
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            logger.warn("Unable to load Vizio app list : {}", e.getMessage());
            vizioApps = new VizioApps();
        }
    }

    public VizioApps getVizioApps() {
        return vizioApps;
    }
}
