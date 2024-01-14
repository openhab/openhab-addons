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
package org.openhab.binding.modbus.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class describing transformation of a command or state.
 *
 * Inspired from other openHAB binding "Transformation" classes.
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class SingleValueTransformation implements ValueTransformation {

    public static final String TRANSFORM_DEFAULT = "default";
    public static final ValueTransformation IDENTITY_TRANSFORMATION = new SingleValueTransformation(TRANSFORM_DEFAULT,
            null, null);

    /** RegEx to extract and parse a function String <code>'(.*?)\((.*)\)'</code> */
    private static final Pattern EXTRACT_FUNCTION_PATTERN_OLD = Pattern.compile("(?<service>.*?)\\((?<arg>.*)\\)");
    private static final Pattern EXTRACT_FUNCTION_PATTERN_NEW = Pattern.compile("(?<service>.*?):(?<arg>.*)");

    /**
     * Ordered list of types that are tried out first when trying to parse transformed command
     */
    private static final List<Class<? extends Command>> DEFAULT_TYPES = new ArrayList<>();
    static {
        DEFAULT_TYPES.add(DecimalType.class);
        DEFAULT_TYPES.add(OpenClosedType.class);
        DEFAULT_TYPES.add(OnOffType.class);
    }

    private final Logger logger = LoggerFactory.getLogger(SingleValueTransformation.class);

    private final @Nullable String transformation;
    final @Nullable String transformationServiceName;
    final @Nullable String transformationServiceParam;

    /**
     *
     * @param transformation either FUN(VAL) (standard transformation syntax), default (identity transformation
     *            (output equals input)) or some other value (output is a constant). Futhermore, empty string is
     *            considered the same way as "default".
     */
    public SingleValueTransformation(@Nullable String transformation) {
        this.transformation = transformation;
        //
        // Parse transformation configuration here on construction, but delay the
        // construction of TransformationService to call-time
        if (transformation == null || transformation.isEmpty() || transformation.equalsIgnoreCase(TRANSFORM_DEFAULT)) {
            // no-op (identity) transformation
            transformationServiceName = null;
            transformationServiceParam = null;
        } else {
            int colonIndex = transformation.indexOf(":");
            int parenthesisOpenIndex = transformation.indexOf("(");

            final Matcher matcher;
            if (parenthesisOpenIndex != -1 && (colonIndex == -1 || parenthesisOpenIndex < colonIndex)) {
                matcher = EXTRACT_FUNCTION_PATTERN_OLD.matcher(transformation);
            } else {
                matcher = EXTRACT_FUNCTION_PATTERN_NEW.matcher(transformation);
            }
            if (matcher.matches()) {
                matcher.reset();
                matcher.find();
                transformationServiceName = matcher.group("service");
                transformationServiceParam = matcher.group("arg");
            } else {
                logger.debug(
                        "Given transformation configuration '{}' did not match the FUN(VAL) pattern. Transformation output will be constant '{}'",
                        transformation, transformation);
                transformationServiceName = null;
                transformationServiceParam = null;
            }
        }
    }

    /**
     * For testing, thus package visibility by design
     *
     * @param transformation
     * @param transformationServiceName
     * @param transformationServiceParam
     */
    SingleValueTransformation(String transformation, @Nullable String transformationServiceName,
            @Nullable String transformationServiceParam) {
        this.transformation = transformation;
        this.transformationServiceName = transformationServiceName;
        this.transformationServiceParam = transformationServiceParam;
    }

    @Override
    public String transform(BundleContext context, String value) {
        String transformedResponse;
        String transformationServiceName = this.transformationServiceName;
        String transformationServiceParam = this.transformationServiceParam;

        if (transformationServiceName != null) {
            try {
                if (transformationServiceParam == null) {
                    throw new TransformationException(
                            "transformation service parameter is missing! Invalid transform?");
                }
                @Nullable
                TransformationService transformationService = TransformationHelper.getTransformationService(context,
                        transformationServiceName);
                if (transformationService != null) {
                    transformedResponse = transformationService.transform(transformationServiceParam, value);
                } else {
                    transformedResponse = value;
                    logger.warn("couldn't transform response because transformationService of type '{}' is unavailable",
                            transformationServiceName);
                }
            } catch (TransformationException te) {
                logger.error("transformation throws exception [transformation={}, response={}]", transformation, value,
                        te);

                // in case of an error we return the response without any
                // transformation
                transformedResponse = value;
            }
        } else if (isIdentityTransform()) {
            // identity transformation
            transformedResponse = value;
        } else {
            // pass value as is
            transformedResponse = this.transformation;
        }

        return transformedResponse == null ? "" : transformedResponse;
    }

    @Override
    public boolean isIdentityTransform() {
        return TRANSFORM_DEFAULT.equalsIgnoreCase(this.transformation);
    }

    public static Optional<Command> tryConvertToCommand(String transformed) {
        return Optional.ofNullable(TypeParser.parseCommand(DEFAULT_TYPES, transformed));
    }

    @Override
    public String toString() {
        return "SingleValueTransformation [transformation=" + transformation + ", transformationServiceName="
                + transformationServiceName + ", transformationServiceParam=" + transformationServiceParam + "]";
    }
}
