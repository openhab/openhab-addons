/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.internal;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class describing transformation of a command or state.
 *
 * Inspired from other openHAB binding "Transformation" classes.
 *
 * @author Sami Salonen
 *
 */
public class Transformation {

    interface TransformationHelperWrapper {
        public TransformationService getTransformationService(BundleContext context, String transformationServiceName);
    }

    private static final class TransformationHelperWrapperImpl implements TransformationHelperWrapper {
        @Override
        public TransformationService getTransformationService(BundleContext context, String transformationServiceName) {
            return TransformationHelper.getTransformationService(context, transformationServiceName);
        }
    }

    public static final String TRANSFORM_DEFAULT = "default";
    public static final Transformation IDENTITY_TRANSFORMATION = new Transformation(TRANSFORM_DEFAULT, null, null);
    private static final TransformationHelperWrapper DEFAULT_TRANSFORMATION_HELPER = new TransformationHelperWrapperImpl();

    /** RegEx to extract and parse a function String <code>'(.*?)\((.*)\)'</code> */
    private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern.compile("(?<service>.*?)\\((?<arg>.*)\\)");

    /**
     * Ordered list of types that are tried out first when trying to parse transformed command
     */
    private final static List<Class<? extends Command>> DEFAULT_TYPES = new ArrayList<>();
    static {
        DEFAULT_TYPES.add(DecimalType.class);
        DEFAULT_TYPES.add(OpenClosedType.class);
        DEFAULT_TYPES.add(OnOffType.class);
    }

    private final Logger logger = LoggerFactory.getLogger(Transformation.class);

    private static StandardToStringStyle toStringStyle = new StandardToStringStyle();

    static {
        toStringStyle.setUseShortClassName(true);
    }

    private final String transformation;
    private final String transformationServiceName;
    private final String transformationServiceParam;

    private TransformationHelperWrapper transformationHelper = DEFAULT_TRANSFORMATION_HELPER;

    /**
     *
     * @param transformation either FUN(VAL) (standard transformation syntax), default (identity transformation
     *            (output equals input)) or some other value (output is a constant). Futhermore, empty string is
     *            considered the same way as "default".
     */
    public Transformation(String transformation) {
        this.transformation = transformation;
        //
        // Parse transformation configuration here on construction, but delay the
        // construction of TransformationService to call-time
        if (isEmpty(transformation) || transformation.equalsIgnoreCase(TRANSFORM_DEFAULT)) {
            // no-op (identity) transformation
            transformationServiceName = null;
            transformationServiceParam = null;
        } else {
            Matcher matcher = EXTRACT_FUNCTION_PATTERN.matcher(transformation);
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
     * For testing. Package level visibility on purpose
     *
     * @param transformation
     * @param transformationHelper
     */
    Transformation(String transformation, TransformationHelperWrapper transformationHelper) {
        this(transformation);
        this.transformationHelper = transformationHelper;
    }

    /**
     * For testing, thus package visibility by design
     *
     * @param transformation
     * @param transformationServiceName
     * @param transformationServiceParam
     */
    Transformation(String transformation, String transformationServiceName, String transformationServiceParam) {
        this.transformation = transformation;
        this.transformationServiceName = transformationServiceName;
        this.transformationServiceParam = transformationServiceParam;
    }

    public String transform(BundleContext context, String value) {
        String transformedResponse;

        if (hasTransformationService()) {
            try {
                TransformationService transformationService = this.transformationHelper
                        .getTransformationService(context, transformationServiceName);
                if (transformationService != null) {
                    transformedResponse = transformationService.transform(transformationServiceParam, value);
                } else {
                    transformedResponse = value;
                    logger.warn("couldn't transform response because transformationService of type '{}' is unavailable",
                            transformationServiceName);
                }
            } catch (Exception te) {
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

        logger.debug("transformed response is '{}'", transformedResponse);

        return transformedResponse;
    }

    public boolean isIdentityTransform() {
        return TRANSFORM_DEFAULT.equalsIgnoreCase(this.transformation);
    }

    /**
     * Transform command to another command using this transformation
     *
     *
     * @param context
     * @param command
     * @param context
     * @return Transformed command, or empty if no transformation was possible
     */
    @Deprecated
    public Optional<Command> transformCommand(BundleContext context, Command command) {
        if (isIdentityTransform()) {
            // optimization, do not convert command->string->command if the transformation is identity transform
            return Optional.ofNullable(command);
        }
        final String commandAsString = command.toString();
        final String transformed = transform(context, commandAsString);

        Optional<Command> transformedCommand = Optional.ofNullable(TypeParser.parseCommand(DEFAULT_TYPES, transformed));
        // if (!transformedCommand.isPresent()) {
        // transformedCommand = Optional.ofNullable(TypeParser.parseCommand(types, transformed));
        // }
        if (transformedCommand.isPresent()) {
            logger.debug(
                    "Transformed item command '{}' to a command {}. Command as string '{}', "
                            + "transformed string '{}', transformation '{}'",
                    command, transformedCommand, commandAsString, transformed, transformation);
        } else {
            logger.debug(
                    "Could not transform item command '{}' to a Command. Command as string '{}', "
                            + "transformed string '{}', transformation '{}'",
                    command, commandAsString, transformed, transformation);
        }
        return transformedCommand;
    }

    public static Optional<Command> tryConvertToCommand(String transformed) {
        Optional<Command> transformedCommand = Optional.ofNullable(TypeParser.parseCommand(DEFAULT_TYPES, transformed));
        return transformedCommand;
    }

    /**
     * Transform state to another state using this transformation
     *
     * @param context
     * @param types types to used to parse the transformation result
     * @param command
     * @return Transformed command, or null if no transformation was possible
     */
    public State transformState(BundleContext context, List<Class<? extends State>> types, State state) {
        // Note that even identity transformations go through the State -> String -> State steps. This does add some
        // overhead but takes care of DecimalType -> PercentType conversions, for example.
        final String stateAsString = state.toString();
        final String transformed = transform(context, stateAsString);
        State transformedState = TypeParser.parseState(types, transformed);
        if (transformedState == null) {
            logger.debug(
                    "Could not transform item state '{}' (of type {}) to a State (tried the following types: {})! Input state as string '{}', "
                            + "transformed string '{}', transformation '{}'",
                    state, state.getClass().getSimpleName(), types, stateAsString, transformed, transformation);
        } else {
            logger.debug(
                    "Transformed item state '{}' (of type {}) to a state {} (of type {}). Input state as string '{}', "
                            + "transformed string '{}', transformation '{}'",
                    state, state.getClass().getSimpleName(), transformedState,
                    transformedState.getClass().getSimpleName(), stateAsString, transformed, transformation);
        }
        return transformedState;
    }

    public boolean hasTransformationService() {
        return transformationServiceName != null;
    }

    /**
     * For testing only. Package level visibility on purpose
     *
     * @param transformationHelper
     */
    void setTransformationHelper(TransformationHelperWrapper transformationHelper) {
        this.transformationHelper = transformationHelper;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Transformation)) {
            return false;
        }
        Transformation that = (Transformation) obj;
        EqualsBuilder eb = new EqualsBuilder();
        if (hasTransformationService()) {
            eb.append(this.transformationServiceName, that.transformationServiceName);
            eb.append(this.transformationServiceParam, that.transformationServiceParam);
        } else {
            eb.append(this.transformation, that.transformation);
        }
        return eb.isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, toStringStyle).append("tranformation", transformation)
                .append("transformationServiceName", transformationServiceName)
                .append("transformationServiceParam", transformationServiceParam).toString();
    }
}
