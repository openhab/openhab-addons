/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.transform.javascript.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by Java Script.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Thomas Kordelle - pre compiled scripts
 */
@NonNullByDefault
@Component(service = { TransformationService.class, ConfigOptionProvider.class }, property = { "openhab.transform=JS" })
public class JavaScriptTransformationService implements TransformationService, ConfigOptionProvider {

    private final Logger logger = LoggerFactory.getLogger(JavaScriptTransformationService.class);

    private static final char EXTENSION_SEPARATOR = '.';

    private static final String PROFILE_CONFIG_URI = "profile:transform:JS";
    private static final String CONFIG_PARAM_FUNCTION = "function";
    private static final String[] FILE_NAME_EXTENSIONS = { "js" };

    private final JavaScriptEngineManager manager;

    @Activate
    public JavaScriptTransformationService(final @Reference JavaScriptEngineManager manager) {
        this.manager = manager;
    }

    /**
     * Transforms the input <code>source</code> by Java Script. It expects the
     * transformation rule to be read from a file which is stored under the
     * 'configurations/transform' folder. To organize the various
     * transformations one should use subfolders.
     *
     * @param filename the name of the file which contains the Java script
     *            transformation rule. Filename can also include additional
     *            variables in URI query variable format which will be injected
     *            to script engine. Transformation service inject input (source)
     *            to 'input' variable.
     * @param source the input to transform
     */
    @Override
    public @Nullable String transform(String filename, String source) throws TransformationException {
        final long startTime = System.currentTimeMillis();
        logger.debug("about to transform '{}' by the JavaScript '{}'", source, filename);

        Map<String, String> vars = Collections.emptyMap();

        String fn = filename;

        try {
            URI uri = new URI("file://" + filename);
            fn = uri.getHost();
            vars = splitQuery(uri.getQuery());
            vars.forEach((k, v) -> {
                if ("input".equals(k)) {
                    throw new RuntimeException();
                }
                logger.debug("Inject additional variable {}={}", k, v);
            });
        } catch (RuntimeException e) {
            throw new TransformationException("'input' word is reserved and can't be used in additional parameters");
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            // fallback to given filename
        }

        String result = "";

        try {
            final CompiledScript cScript = manager.getScript(fn);
            final Bindings bindings = cScript.getEngine().createBindings();
            bindings.put("input", source);
            vars.forEach((k, v) -> bindings.put(k, v));
            result = String.valueOf(cScript.eval(bindings));
            return result;
        } catch (ScriptException e) {
            throw new TransformationException("An error occurred while executing script. " + e.getMessage(), e);
        } finally {
            logger.trace("JavaScript execution elapsed {} ms. Result: {}", System.currentTimeMillis() - startTime,
                    result);
        }
    }

    private Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> result = new LinkedHashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyval = pair.split("=");
            result.put(URLDecoder.decode(keyval[0], "UTF-8"), URLDecoder.decode(keyval[1], "UTF-8"));
        }
        return result;
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (PROFILE_CONFIG_URI.equals(uri.toString())) {
            switch (param) {
                case CONFIG_PARAM_FUNCTION:
                    return getFilenames(FILE_NAME_EXTENSIONS).stream().map(f -> new ParameterOption(f, f))
                            .collect(Collectors.toList());
            }
        }
        return null;
    }

    /**
     * Returns a list of all files with the given extensions in the transformation folder
     */
    private List<String> getFilenames(String[] validExtensions) {
        File path = new File(TransformationScriptWatcher.TRANSFORM_FOLDER + File.separator);
        return Arrays.asList(path.listFiles(new FileExtensionsFilter(validExtensions))).stream().map(f -> f.getName())
                .collect(Collectors.toList());
    }

    private class FileExtensionsFilter implements FilenameFilter {

        private final String[] validExtensions;

        public FileExtensionsFilter(String[] validExtensions) {
            this.validExtensions = validExtensions;
        }

        @Override
        public boolean accept(@Nullable File dir, @Nullable String name) {
            if (name != null) {
                for (String extension : validExtensions) {
                    if (name.toLowerCase().endsWith(EXTENSION_SEPARATOR + extension)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
