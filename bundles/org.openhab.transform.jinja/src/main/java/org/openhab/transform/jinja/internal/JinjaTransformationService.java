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
package org.openhab.transform.jinja.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.python.embedding.GraalPyResources;
import org.graalvm.python.embedding.VirtualFileSystem;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The implementation of {@link TransformationService} which transforms the input by Jinja2 Expressions.
 *
 * @author Cody Cutrer - Initial contribution
 *
 */
@NonNullByDefault
@Component(property = { "openhab.transform=JINJA" })
public class JinjaTransformationService implements TransformationService {
    private static final String PYTHON = "python";

    private final Logger logger = LoggerFactory.getLogger(JinjaTransformationService.class);

    private final Context context;
    private final Value bindings;
    private final Source transformSource;

    @Activate
    public JinjaTransformationService() {
        VirtualFileSystem vfs = VirtualFileSystem.newBuilder().resourceLoadingClass(JinjaTransformationService.class)
                .build();
        context = GraalPyResources.contextBuilder(vfs).build();
        bindings = context.getBindings(PYTHON);

        context.eval(PYTHON, """
                import jinja2
                import json

                environment = jinja2.Environment(extensions=['jinja2.ext.loopcontrols'])
                """);

        transformSource = Source.newBuilder(PYTHON, """
                template = environment.from_string(template_string)
                try:
                    value_json = json.loads(value)
                except json.JSONDecodeError:
                    value_json = None
                result = template.render(value=value, value_json=value_json)
                result
                """, "transform.py").buildLiteral();
    }

    /**
     * Transforms the input <code>value</code> by Jinja template.
     *
     * @param template Jinja template
     * @param value String may contain JSON
     * @throws TransformationException
     */
    @Override
    public @Nullable String transform(String template, String value) throws TransformationException {
        String transformationResult;

        logger.debug("About to transform '{}' with template '{}'", value, template);

        bindings.putMember("template_string", template);
        bindings.putMember("value", value);

        try {
            transformationResult = context.eval(transformSource).asString();
        } catch (PolyglotException e) {
            String message = e.getMessage();
            if (message == null) {
                message = "Unknown error";
            }
            throw new TransformationException(message, e);
        } finally {
            bindings.removeMember("template_string");
            bindings.removeMember("value");
            bindings.removeMember("template");
            bindings.removeMember("value_json");
        }

        logger.debug("Result: '{}'", transformationResult);

        return transformationResult;
    }
}
