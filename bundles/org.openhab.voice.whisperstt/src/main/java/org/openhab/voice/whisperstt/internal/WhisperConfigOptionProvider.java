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
package org.openhab.voice.whisperstt.internal;

import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_CATEGORY;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_ID;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_NAME;
import static org.openhab.voice.whisperstt.internal.WhisperSTTConstants.SERVICE_PID;
import static org.openhab.voice.whisperstt.internal.WhisperSTTService.WHISPER_FOLDER;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.config.core.ParameterOption;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link WhisperConfigOptionProvider} class provides some dynamic configuration options
 *
 * @author Miguel Álvarez - Initial contribution
 */
@Component(service = ConfigOptionProvider.class, configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "="
        + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Speech-to-Text", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
@NonNullByDefault
public class WhisperConfigOptionProvider implements ConfigOptionProvider {
    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (context == null && (SERVICE_CATEGORY + ":" + SERVICE_ID).equals(uri.toString())) {
            if ("modelName".equals(param)) {
                return getAvailableModelOptions();
            }
        }
        return null;
    }

    private List<ParameterOption> getAvailableModelOptions() {
        var folderFile = WHISPER_FOLDER.toFile();
        var files = folderFile.listFiles();
        if (!folderFile.exists() || !folderFile.isDirectory() || files == null) {
            return List.of();
        }
        String modelExtension = ".bin";
        return Stream.of(files).filter(file -> !file.isDirectory() && file.getName().endsWith(modelExtension))
                .map(file -> {
                    String fileName = file.getName();
                    String optionName = file.getName();
                    String optionalPrefix = "ggml-";
                    if (optionName.startsWith(optionalPrefix)) {
                        optionName = optionName.substring(optionalPrefix.length());
                    }
                    optionName = optionName.substring(0, optionName.length() - modelExtension.length());
                    return new ParameterOption(fileName, optionName);
                }).collect(Collectors.toList());
    }
}
