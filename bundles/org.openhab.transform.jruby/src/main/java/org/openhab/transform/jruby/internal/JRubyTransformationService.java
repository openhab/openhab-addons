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
package org.openhab.transform.jruby.internal;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link TransformationService} which transforms the
 * input by Ruby
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
@Component(service = { TransformationService.class }, property = { "openhab.transform=RUBY" })
public class JRubyTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(JRubyTransformationService.class);

    private static final String SCRIPT_DATA_WORD = "input";

    private final JRubyEngineManager manager;

    @Activate
    public JRubyTransformationService(final @Reference JRubyEngineManager manager) {
        this.manager = manager;
    }

    /**
     * Transforms the input <code>source</code> by Ruby. If script is a filename, it expects the
     * transformation rule to be read from a file which is stored under the
     * 'configurations/transform' folder. To organize the various
     * transformations one should use subfolders.
     *
     * @param filenameOrInlineScript parameter can be 1) the name of the file which contains the Java script
     *            transformation rule. Filename can also include additional
     *            variables in URI query variable format which will be injected
     *            to script engine. 2) inline script when starting with '|' character.
     *            Transformation service inject input (source) to 'input' variable.
     * @param source the input to transform
     */
    @Override
    public @Nullable String transform(String filenameOrInlineScript, String source) throws TransformationException {
        final long startTime = System.currentTimeMillis();

        Map<String, String> vars = Collections.emptyMap();
        String result = "";

        CompiledScript cScript;

        if (filenameOrInlineScript.startsWith("|")) {
            // inline java script
            cScript = manager.getCompiledScriptByInlineScript(filenameOrInlineScript.substring(1));
        } else {
            String filename = filenameOrInlineScript;

            if (filename.contains("?")) {
                String[] parts = filename.split("\\?");
                if (parts.length > 2) {
                    throw new TransformationException("Questionmark should be defined only once in the filename");
                }
                filename = parts[0];
                try {
                    vars = splitQuery(parts[1]);
                } catch (IllegalArgumentException e) {
                    throw new TransformationException("Illegal filename syntax");
                }
                if (isReservedWordUsed(vars)) {
                    throw new TransformationException(
                            "'" + SCRIPT_DATA_WORD + "' word is reserved and can't be used in additional parameters");
                }
            }

            cScript = manager.getCompiledScriptByFilename(filename);
        }

        final Bindings bindings = cScript.getEngine().createBindings();
        try {
            bindings.put("@" + SCRIPT_DATA_WORD, source);
            vars.forEach((k, v) -> bindings.put("@" + k, v));
            final ScriptContext context = new SimpleScriptContext();
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("org.jruby.embed.clear.variables", true, ScriptContext.ENGINE_SCOPE);
            result = String.valueOf(cScript.eval(context));
            return result;
        } catch (ScriptException e) {
            throw new TransformationException("An error occurred while executing script. " + e.getMessage(), e);
        } finally {
            logger.trace("Ruby execution elapsed {} ms. Result: {}", System.currentTimeMillis() - startTime, result);
        }
    }

    private boolean isReservedWordUsed(Map<String, String> map) {
        for (String key : map.keySet()) {
            if (SCRIPT_DATA_WORD.equals(key)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> splitQuery(@Nullable String query) throws IllegalArgumentException {
        Map<String, String> result = new LinkedHashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyval = pair.split("=");
                if (keyval.length != 2) {
                    throw new IllegalArgumentException();
                } else {
                    result.put(URLDecoder.decode(keyval[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(keyval[1], StandardCharsets.UTF_8));
                }
            }
        }
        return result;
    }
}
