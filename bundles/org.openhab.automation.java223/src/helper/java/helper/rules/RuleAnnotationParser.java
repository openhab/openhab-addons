/**
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

package helper.rules;

import static org.openhab.automation.java223.common.Java223Constants.ANNOTATION_DEFAULT;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedAutomationManager;
import org.openhab.core.automation.util.ModuleBuilder;
import org.openhab.core.automation.util.TriggerBuilder;
import org.openhab.core.config.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helper.rules.annotations.ChannelEventTrigger;
import helper.rules.annotations.CronTrigger;
import helper.rules.annotations.DateTimeTrigger;
import helper.rules.annotations.DayOfWeekCondition;
import helper.rules.annotations.EphemerisDaysetCondition;
import helper.rules.annotations.EphemerisHolidayCondition;
import helper.rules.annotations.EphemerisNotHolidayCondition;
import helper.rules.annotations.EphemerisWeekdayCondition;
import helper.rules.annotations.EphemerisWeekendCondition;
import helper.rules.annotations.GenericAutomationTrigger;
import helper.rules.annotations.GenericCompareCondition;
import helper.rules.annotations.GenericEventCondition;
import helper.rules.annotations.GenericEventTrigger;
import helper.rules.annotations.GroupStateChangeTrigger;
import helper.rules.annotations.GroupStateUpdateTrigger;
import helper.rules.annotations.ItemCommandTrigger;
import helper.rules.annotations.ItemStateChangeTrigger;
import helper.rules.annotations.ItemStateCondition;
import helper.rules.annotations.ItemStateUpdateTrigger;
import helper.rules.annotations.Rule;
import helper.rules.annotations.SystemStartlevelTrigger;
import helper.rules.annotations.ThingStatusChangeTrigger;
import helper.rules.annotations.ThingStatusUpdateTrigger;
import helper.rules.annotations.TimeOfDayCondition;
import helper.rules.annotations.TimeOfDayTrigger;

/**
 * Parse annotated method in a script and create rule accordingly
 *
 * @author Gwendal Roulleau - Initial contribution, based on work from Jürgen Weber and Jan N. Klug
 */
@NonNullByDefault
public class RuleAnnotationParser {

    private static final Logger logger = LoggerFactory.getLogger(RuleAnnotationParser.class);

    public static final Map<Class<? extends Annotation>, String> TRIGGER_FROM_ANNOTATION = Map.ofEntries(
            Map.entry(ItemCommandTrigger.class, "core.ItemCommandTrigger"),
            Map.entry(ItemStateChangeTrigger.class, "core.ItemStateChangeTrigger"),
            Map.entry(ItemStateUpdateTrigger.class, "core.ItemStateUpdateTrigger"),
            Map.entry(GroupStateChangeTrigger.class, "core.GroupStateChangeTrigger"),
            Map.entry(GroupStateUpdateTrigger.class, "core.GroupStateUpdateTrigger"),
            Map.entry(ChannelEventTrigger.class, "core.ChannelEventTrigger"),
            Map.entry(CronTrigger.class, "timer.GenericCronTrigger"),
            Map.entry(DateTimeTrigger.class, "timer.DateTimeTrigger"),
            Map.entry(TimeOfDayTrigger.class, "timer.TimeOfDayTrigger"),
            Map.entry(GenericEventTrigger.class, "core.GenericEventTrigger"),
            Map.entry(ThingStatusUpdateTrigger.class, "core.ThingStatusUpdateTrigger"),
            Map.entry(ThingStatusChangeTrigger.class, "core.ThingStatusChangeTrigger"),
            Map.entry(SystemStartlevelTrigger.class, "core.SystemStartlevelTrigger"));

    public static final Map<Class<? extends Annotation>, String> CONDITION_FROM_ANNOTATION = Map.ofEntries(
            Map.entry(ItemStateCondition.class, "core.ItemStateCondition"),
            Map.entry(GenericCompareCondition.class, "core.GenericCompareCondition"),
            Map.entry(DayOfWeekCondition.class, "timer.DayOfWeekCondition"),
            Map.entry(EphemerisWeekdayCondition.class, "ephemeris.WeekdayCondition"),
            Map.entry(EphemerisWeekendCondition.class, "ephemeris.WeekendCondition"),
            Map.entry(EphemerisHolidayCondition.class, "ephemeris.HolidayCondition"),
            Map.entry(EphemerisNotHolidayCondition.class, "ephemeris.NotHolidayCondition"),
            Map.entry(EphemerisDaysetCondition.class, "ephemeris.DaysetCondition"),
            Map.entry(GenericEventCondition.class, "core.GenericEventCondition"),
            Map.entry(TimeOfDayCondition.class, "core.TimeOfDayCondition"));

    /**
     * Parse annotated method in a script and create rule accordingly
     * @param script the script to parse
     * @param name the name of the caller. Used for rule id unique name generation, so should also be unique.
     * @param automationManager the automation manager to add the rule to
     */
    @SuppressWarnings({ "unused", "null" })
    public static void parse(Object script, String name, ScriptedAutomationManager automationManager)
            throws IllegalArgumentException, RuleParserException {
        Class<?> c = script.getClass();

        logger.debug("Parsing: {}", c.getName());

        List<AccessibleObject> members = new ArrayList<>();
        Collections.addAll(members, c.getDeclaredFields());
        Collections.addAll(members, c.getDeclaredMethods());

        for (AccessibleObject member : members) {

            Rule ra = member.getAnnotation(Rule.class);
            if (ra == null) {
                continue;
            }
            if (ra.disabled()) {
                logger.debug("Ignoring disabled rule");
                continue;
            }

            // extract an action from the annotated member
            Java223Rule simpleRule;
            String memberName;
            if (member instanceof Field fieldMember) {
                simpleRule = new Java223Rule(script, fieldMember);
                memberName = fieldMember.getName();
            } else if (member instanceof Method methodMember) {
                simpleRule = new Java223Rule(script, methodMember);
                memberName = methodMember.getName();
            } else {
                continue;
            }

            // name and description
            String ruleName = chooseFirstOk(ra.name(), memberName);
            String ruleDescription = chooseFirstOk(ra.description(),
                    script.getClass().getSimpleName() + "/" + ruleName);
            simpleRule.setName(ruleName);
            simpleRule.setDescription(ruleDescription);

            // uid
            String defaultUid = sanitizeId(ruleName + "_" + md5Hex(name));
            simpleRule.setUid(chooseFirstOk(ra.uid(), defaultUid));

            // tags
            simpleRule.setTags(Set.of(ra.tags()));

            // triggers
            List<Trigger> triggers = new ArrayList<>();
            TRIGGER_FROM_ANNOTATION
                    .entrySet().stream().map(annotation -> getModuleForAnnotation(member, annotation.getKey(),
                            annotation.getValue(), ModuleBuilder::createTrigger, ruleName))
                    .flatMap(Collection::stream).forEach(triggers::add);
            Arrays.stream(member.getDeclaredAnnotationsByType(GenericAutomationTrigger.class))
                    .map(annotation -> getGenericAutomationTrigger(annotation, ruleName)).forEach(triggers::add);
            simpleRule.setTriggers(triggers);

            // condition
            List<Condition> conditions = CONDITION_FROM_ANNOTATION.entrySet().stream()
                    .map(annotationClazz -> getModuleForAnnotation(member, annotationClazz.getKey(),
                            annotationClazz.getValue(), ModuleBuilder::createCondition, ruleName))
                    .flatMap(Collection::stream).collect(Collectors.toList());
            simpleRule.setConditions(conditions);

            // log everything
            if (logger.isDebugEnabled()) {
                logger.debug("Parsing result field {}", memberName);
                logger.debug("Parsing result @Rule(name = {})", ruleName);
                for (Trigger trigger : simpleRule.getTriggers()) {
                    logger.debug("Parsing result Trigger(id = {}, uid = {})", trigger.getId(), trigger.getTypeUID());
                    logger.debug("Parsing result Configuration: {}", trigger.getConfiguration());
                }
            }

            // create rule
            automationManager.addRule(simpleRule);
        }
    }

    private static String chooseFirstOk(String... choices) {
        for (String choice : choices) {
            if (choice != null && !choice.isBlank() && !choice.equals(ANNOTATION_DEFAULT)) {
                return choice;
            }
        }
        return "";
    }

    private static Trigger getGenericAutomationTrigger(GenericAutomationTrigger annotation, String ruleName) {
        String typeUid = annotation.typeUid();
        Configuration configuration = new Configuration();
        for (String param : annotation.params()) {
            String[] parts = param.split("=");
            if (parts.length != 2) {
                logger.warn("Ignoring '{}' in trigger for '{}', can not determine key and value", param, ruleName);
                continue;
            }
            configuration.put(parts[0], parts[1]);
        }
        return TriggerBuilder.create().withTypeUID(typeUid).withId(sanitizeTriggerId(ruleName, annotation.hashCode()))
                .withConfiguration(configuration).build();
    }

    private static <T extends Annotation, R extends Module> List<R> getModuleForAnnotation(
            AccessibleObject accessibleObject, Class<T> clazz, String typeUid, Supplier<ModuleBuilder<?, R>> builder,
            String ruleName) {
        T[] annotations = accessibleObject.getDeclaredAnnotationsByType(clazz);
        return Arrays.stream(annotations)
                .map(annotation -> builder.get().withId(sanitizeTriggerId(ruleName, annotation.hashCode()))
                        .withTypeUID(typeUid).withConfiguration(getAnnotationConfiguration(annotation)).build())
                .collect(Collectors.toList());
    }

    private static Configuration getAnnotationConfiguration(Annotation annotation) {
        Map<String, Object> configuration = new HashMap<>();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            try {
                if (method.getParameterCount() == 0) {
                    Object parameterValue = method.invoke(annotation);
                    if (parameterValue == null || ANNOTATION_DEFAULT.equals(parameterValue)) {
                        continue;
                    }
                    if (parameterValue instanceof String[]) {
                        configuration.put(method.getName(), Arrays.asList((String[]) parameterValue));
                    } else if (parameterValue instanceof Integer) {
                        configuration.put(method.getName(), BigDecimal.valueOf((Integer) parameterValue));
                    } else {
                        configuration.put(method.getName(), parameterValue);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                // ignore private fields
            }
        }
        return new Configuration(configuration);
    }

    private static String sanitizeTriggerId(String ruleName, int index) {
        return sanitizeId(ruleName + "_" + index);
    }

    private static String sanitizeId(String id) {
        return id.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    /**
     * Encodes a string with MD5 and returns the hexadecimal result.
     *
     * @param input The string to encode.
     * @return The MD5 hash of the input string as a hexadecimal string.
     */
    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // This should ideally not happen as MD5 is a standard algorithm
           return ("MD5 algorithm not found. Should not happen");
        }
    }
}
