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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.module.script.rulesupport.internal.ScriptedCustomModuleHandlerFactory;
import org.openhab.core.automation.module.script.rulesupport.internal.ScriptedCustomModuleTypeProvider;
import org.openhab.core.automation.module.script.rulesupport.internal.ScriptedPrivateModuleHandlerFactory;
import org.openhab.core.automation.module.script.rulesupport.shared.RuleSupportRuleRegistryDelegate;
import org.openhab.core.automation.module.script.rulesupport.shared.ScriptedAutomationManager;

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
import helper.rules.annotations.GroupCommandTrigger;
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
 * Unit tests for {@link RuleAnnotationParser}.
 *
 * The central test of this class is intentionally <em>dynamic</em>: instead of hardcoding the list of the trigger and
 * condition annotations, the test discovers every annotation of the {@code helper.rules.annotations} package whose
 * simple name ends with {@code Trigger} or {@code Condition}. This way, adding a new annotation to the helper library
 * without registering it in {@link RuleAnnotationParser} immediately makes this test fail.
 * Plus, test about parsing annotations.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class RuleAnnotationParserTest {

    /**
     * The package holding the annotations of the helper library.
     */
    private static final String ANNOTATIONS_PACKAGE = "helper.rules.annotations";

    /**
     * The suffix of the annotations describing a trigger.
     */
    private static final String TRIGGER_SUFFIX = "Trigger";

    /**
     * The suffix of the annotations describing a condition.
     */
    private static final String CONDITION_SUFFIX = "Condition";

    /**
     * All the trigger annotations of the helper library, discovered dynamically.
     */
    private static List<Class<? extends Annotation>> triggerAnnotations = List.of();

    /**
     * All the condition annotations of the helper library, discovered dynamically.
     */
    private static List<Class<? extends Annotation>> conditionAnnotations = List.of();

    @BeforeAll
    public static void discoverAnnotations() {
        /**
         * All the annotations of the helper library, discovered dynamically.
         */
        List<Class<? extends Annotation>> allAnnotations = discoverHelperAnnotations();
        triggerAnnotations = allAnnotations.stream().filter(RuleAnnotationParserTest::isTriggerAnnotation).toList();
        conditionAnnotations = allAnnotations.stream().filter(RuleAnnotationParserTest::isConditionAnnotation).toList();
    }

    /**
     * Discovers all the annotations located in the {@code helper.rules.annotations} package.
     *
     * The helper library is compiled into a dedicated jar, so the annotations are discovered by scanning the class path
     * for the classes of that package. This makes the discovery independent from the way the helper jar is built and
     * from the working directory the tests are executed from. As a fallback, the source directory of the helper library
     * is scanned when the classes cannot be found on the class path.
     */
    private static List<Class<? extends Annotation>> discoverHelperAnnotations() {
        List<Class<? extends Annotation>> annotations = discoverAnnotationsFromClassPath();
        if (annotations.isEmpty()) {
            annotations = discoverAnnotationsFromSources();
        }
        if (annotations.isEmpty()) {
            throw new IllegalStateException(
                    "Unable to discover the annotations of the helper library in the package " + ANNOTATIONS_PACKAGE);
        }
        return annotations;
    }

    /**
     * Discovers the annotations by scanning the class path for the classes of the annotations package.
     */
    private static List<Class<? extends Annotation>> discoverAnnotationsFromClassPath() {
        Set<String> classNames = new TreeSet<>();
        ClassLoader classLoader = RuleAnnotationParserTest.class.getClassLoader();

        // the classes are either in a directory or in the helper jar, both are handled by the URL class loader
        try {
            Enumeration<URL> resources = classLoader.getResources(ANNOTATIONS_PACKAGE.replace('.', '/'));
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    collectClassNamesFromDirectory(Path.of(resource.toURI()), classNames);
                } else if ("jar".equals(resource.getProtocol())) {
                    collectClassNamesFromJar(resource, classNames);
                }
            }
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            return List.of();
        }

        List<Class<? extends Annotation>> annotations = new ArrayList<>();
        for (String className : classNames) {
            Class<?> clazz = loadClass(className);
            if (clazz != null && clazz.isAnnotation()) {
                annotations.add(clazz.asSubclass(Annotation.class));
            }
        }
        return annotations;
    }

    /**
     * Collects the names of the classes located in the given directory.
     */
    private static void collectClassNamesFromDirectory(Path directory, Set<String> classNames) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(file -> file.getFileName().toString().endsWith(".class")).map(file -> {
                String simpleName = file.getFileName().toString().replace(".class", "");
                return ANNOTATIONS_PACKAGE + "." + simpleName;
            }).forEach(classNames::add);
        }
    }

    /**
     * Collects the names of the classes located in the jar pointed by the given URL.
     */
    private static void collectClassNamesFromJar(URL resource, Set<String> classNames) throws IOException {
        String path = resource.getPath();
        int separator = path.indexOf('!');
        if (separator < 0) {
            return;
        }
        String jarPath = path.substring(0, separator);
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring("file:".length());
        }
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(ANNOTATIONS_PACKAGE.replace('.', '/') + "/") && name.endsWith(".class")
                        && !name.contains("$")) {
                    classNames.add(name.replace('/', '.').replace(".class", ""));
                }
            }
        }
    }

    /**
     * Discovers the annotations by scanning the source directory of the helper library.
     *
     * This is used as a fallback when the classes of the helper library cannot be found on the class path.
     */
    private static List<Class<? extends Annotation>> discoverAnnotationsFromSources() {
        Path annotationsDirectory = findHelperAnnotationsDirectory();
        if (annotationsDirectory == null) {
            return List.of();
        }

        List<Class<? extends Annotation>> annotations = new ArrayList<>();
        try (Stream<Path> files = Files.list(annotationsDirectory)) {
            files.filter(file -> file.getFileName().toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString)).forEach(file -> {
                        String simpleName = file.getFileName().toString().replace(".java", "");
                        Class<?> clazz = loadClass(ANNOTATIONS_PACKAGE + "." + simpleName);
                        if (clazz != null && clazz.isAnnotation()) {
                            annotations.add(clazz.asSubclass(Annotation.class));
                        }
                    });
        } catch (IOException e) {
            return List.of();
        }
        return annotations;
    }

    /**
     * Locates the directory holding the annotations of the helper library.
     *
     * The helper library is compiled from a dedicated source directory, so the sources are not copied next to the
     * compiled classes. The directory is therefore looked up relatively to the current working directory, which is the
     * root of the bundle when the tests are executed by Maven.
     */
    private static @Nullable Path findHelperAnnotationsDirectory() {
        List<Path> candidates = List.of(Path.of("src", "helper", "java", "helper", "rules", "annotations"),
                Path.of("src", "helper", "java").resolve(ANNOTATIONS_PACKAGE.replace('.', '/')),
                Path.of("..", "..", "src", "helper", "java", "helper", "rules", "annotations"));
        return candidates.stream().filter(Files::isDirectory).findFirst().orElse(null);
    }

    private static @Nullable Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, RuleAnnotationParserTest.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    private static boolean isTriggerAnnotation(Class<? extends Annotation> annotation) {
        return annotation.getSimpleName().endsWith(TRIGGER_SUFFIX);
    }

    private static boolean isConditionAnnotation(Class<? extends Annotation> annotation) {
        return annotation.getSimpleName().endsWith(CONDITION_SUFFIX);
    }

    /**
     * Every trigger annotation of the helper library must be registered in
     * {@link RuleAnnotationParser#TRIGGER_FROM_ANNOTATION}.
     *
     * The {@link GenericAutomationTrigger} is excluded on purpose: it is a generic escape hatch carrying its own type
     * UID, so it is not bound to a fixed type UID and is handled separately by the parser.
     */
    @Test
    public void everyTriggerAnnotationIsRegistered() {
        assertFalse(triggerAnnotations.isEmpty(), "No trigger annotation was discovered, the discovery is broken");

        for (Class<? extends Annotation> annotation : triggerAnnotations) {
            if (annotation.equals(GenericAutomationTrigger.class)) {
                continue;
            }
            assertTrue(RuleAnnotationParser.TRIGGER_FROM_ANNOTATION.containsKey(annotation),
                    "The trigger annotation " + annotation.getSimpleName()
                            + " is not registered in RuleAnnotationParser.TRIGGER_FROM_ANNOTATION");
        }
    }

    /**
     * Every condition annotation of the helper library must be registered in
     * {@link RuleAnnotationParser#CONDITION_FROM_ANNOTATION}.
     */
    @Test
    public void everyConditionAnnotationIsRegistered() {
        assertFalse(conditionAnnotations.isEmpty(), "No condition annotation was discovered, the discovery is broken");

        for (Class<? extends Annotation> annotation : conditionAnnotations) {
            assertTrue(RuleAnnotationParser.CONDITION_FROM_ANNOTATION.containsKey(annotation),
                    "The condition annotation " + annotation.getSimpleName()
                            + " is not registered in RuleAnnotationParser.CONDITION_FROM_ANNOTATION");
        }
    }

    /**
     * The two registries must not overlap: an annotation cannot be a trigger and a condition at the same time.
     */
    @Test
    public void noAnnotationIsRegisteredAsBothTriggerAndCondition() {
        for (Class<? extends Annotation> annotation : triggerAnnotations) {
            assertFalse(RuleAnnotationParser.CONDITION_FROM_ANNOTATION.containsKey(annotation),
                    "The annotation " + annotation.getSimpleName() + " is registered as a trigger and as a condition");
        }
    }

    /**
     * The registered type UIDs must be usable, i.e. they must be namespaced by the bundle providing the implementation.
     */
    @Test
    public void registeredTypeUidsAreNamespaced() {
        for (Map.Entry<Class<? extends Annotation>, String> entry : RuleAnnotationParser.TRIGGER_FROM_ANNOTATION
                .entrySet()) {
            assertTrue(entry.getValue().contains("."), "The type UID '" + entry.getValue() + "' of "
                    + entry.getKey().getSimpleName() + " is not namespaced");
        }
        for (Map.Entry<Class<? extends Annotation>, String> entry : RuleAnnotationParser.CONDITION_FROM_ANNOTATION
                .entrySet()) {
            assertTrue(entry.getValue().contains("."), "The type UID '" + entry.getValue() + "' of "
                    + entry.getKey().getSimpleName() + " is not namespaced");
        }
    }

    /**
     * Parsing a script using every known trigger annotation must produce one trigger per annotation, with the
     * expected type UID.
     */
    @Test
    public void everyTriggerAnnotationProducesATrigger() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithAllTriggers());

        for (Class<? extends Annotation> annotation : triggerAnnotations) {
            String typeUid = resolveTriggerTypeUid(annotation);
            assertNotNull(typeUid, "No type UID registered for " + annotation.getSimpleName());

            long expectedCount = countUsages(annotation, ScriptWithAllTriggers.class);
            long actualCount = rules.stream().flatMap(rule -> rule.getTriggers().stream())
                    .filter(trigger -> typeUid.equals(trigger.getTypeUID())).count();
            assertEquals(expectedCount, actualCount, "Unexpected number of triggers of type '" + typeUid
                    + "' for the annotation " + annotation.getSimpleName());

            rules.stream().flatMap(rule -> rule.getTriggers().stream()).filter(trigger -> typeUid.equals(trigger.getTypeUID()))
                    .forEach(trigger -> assertFalse(trigger.getId().isEmpty(),
                            "The trigger of type '" + typeUid + "' has no id"));
        }
    }

    /**
     * Parsing a script using every known condition annotation must produce one condition per annotation, with the
     * expected type UID.
     */
    @Test
    public void everyConditionAnnotationProducesACondition() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithAllConditions());

        for (Class<? extends Annotation> annotation : conditionAnnotations) {
            String typeUid = RuleAnnotationParser.CONDITION_FROM_ANNOTATION.get(annotation);
            assertNotNull(typeUid, "No type UID registered for " + annotation.getSimpleName());

            long expectedCount = countUsages(annotation, ScriptWithAllConditions.class);
            long actualCount = rules.stream().flatMap(rule -> rule.getConditions().stream())
                    .filter(condition -> typeUid.equals(condition.getTypeUID())).count();
            assertEquals(expectedCount, actualCount, "Unexpected number of conditions of type '" + typeUid
                    + "' for the annotation " + annotation.getSimpleName());

            rules.stream().flatMap(rule -> rule.getConditions().stream())
                    .filter(condition -> typeUid.equals(condition.getTypeUID()))
                    .forEach(condition -> assertFalse(condition.getId().isEmpty(),
                            "The condition of type '" + typeUid + "' has no id"));
        }
    }

    /**
     * Every trigger annotation must be usable on a method as well as on a field.
     */
    @Test
    public void triggerAnnotationsAreSupportedOnMethods() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithTriggerOnMethod());

        assertEquals(1, rules.size(), "Exactly one rule must be created");
        org.openhab.core.automation.Rule rule = rules.get(0);

        assertEquals(1, rule.getTriggers().size(), "Exactly one trigger must be created");
        Trigger trigger = rule.getTriggers().get(0);
        assertEquals("core.ItemStateChangeTrigger", trigger.getTypeUID(), "The type UID must be the expected one");
        assertEquals("Light", trigger.getConfiguration().get("itemName"), "The itemName must be taken from the annotation");
    }

    /**
     * The configuration of a trigger must be filled with the values of the annotation.
     */
    @Test
    public void triggerConfigurationIsTakenFromTheAnnotation() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithAllTriggers());

        Trigger trigger = findTrigger(rules, "core.ItemStateChangeTrigger");
        assertNotNull(trigger, "The ItemStateChangeTrigger was not created");

        Map<String, Object> configuration = trigger.getConfiguration().getProperties();
        assertEquals("Light", configuration.get("itemName"), "The itemName of the annotation must be used");
        assertEquals("ON", configuration.get("state"), "The state of the annotation must be used");
        assertEquals("OFF", configuration.get("previousState"), "The previousState of the annotation must be used");
    }

    /**
     * The configuration of a condition must be filled with the values of the annotation.
     */
    @Test
    public void conditionConfigurationIsTakenFromTheAnnotation() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithAllConditions());

        Condition condition = findCondition(rules, "core.ItemStateCondition");
        assertNotNull(condition, "The ItemStateCondition was not created");

        Map<String, Object> configuration = condition.getConfiguration().getProperties();
        assertEquals("Light", configuration.get("itemName"), "The itemName of the annotation must be used");
        assertEquals("==", configuration.get("operator"), "The operator of the annotation must be used");
        assertEquals("ON", configuration.get("state"), "The state of the annotation must be used");
    }

    /**
     * The attributes left to their default value must not be added to the configuration, because they would override
     * the default value of the module implementation.
     */
    @Test
    public void defaultAttributeValuesAreNotAddedToTheConfiguration() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithPartialAttributes());

        Trigger trigger = findTrigger(rules, "core.ItemStateChangeTrigger");
        assertNotNull(trigger, "The ItemStateChangeTrigger was not created");

        Map<String, Object> configuration = trigger.getConfiguration().getProperties();
        assertEquals("Light", configuration.get("itemName"), "The itemName of the annotation must be used");
        assertFalse(configuration.containsKey("state"), "The state must not be part of the configuration");
        assertFalse(configuration.containsKey("previousState"), "The previousState must not be part of the configuration");
    }

    /**
     * A repeatable annotation used several times must produce as many modules.
     */
    @Test
    public void repeatableAnnotationsProduceSeveralModules() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithRepeatedAnnotations());

        long triggers = rules.stream().flatMap(rule -> rule.getTriggers().stream())
                .filter(trigger -> "core.ItemStateChangeTrigger".equals(trigger.getTypeUID())).count();
        assertEquals(2, triggers, "A repeated trigger annotation must produce one trigger per usage");

        long conditions = rules.stream().flatMap(rule -> rule.getConditions().stream())
                .filter(condition -> "core.ItemStateCondition".equals(condition.getTypeUID())).count();
        assertEquals(2, conditions, "A repeated condition annotation must produce one condition per usage");
    }

    /**
     * The generic automation trigger must use the type UID carried by the annotation.
     */
    @Test
    public void genericAutomationTriggerUsesTheGivenTypeUid() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithAllTriggers());

        Trigger trigger = findTrigger(rules, "custom:trigger");
        assertNotNull(trigger, "The GenericAutomationTrigger was not created");
        assertEquals("value", trigger.getConfiguration().get("key"), "The parameters must be taken from the annotation");
    }

    /**
     * The {@code int} attributes must be converted to a number usable by the configuration.
     */
    @Test
    public void integerAttributesAreConvertedToNumbers() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithAllTriggers());

        Trigger trigger = findTrigger(rules, "core.SystemStartlevelTrigger");
        assertNotNull(trigger, "The SystemStartlevelTrigger was not created");
        assertEquals(new java.math.BigDecimal(20), trigger.getConfiguration().get("startlevel"),
                "The startlevel must be converted to a number");
    }

    /**
     * The {@link String[]} attributes must be converted to a list usable by the configuration.
     */
    @Test
    public void stringArrayAttributesAreConvertedToLists() {
        List<org.openhab.core.automation.Rule> rules = parse(new ScriptWithAllConditions());

        Condition condition = findCondition(rules, "timer.DayOfWeekCondition");
        assertNotNull(condition, "The DayOfWeekCondition was not created");
        assertEquals(List.of("MONDAY", "TUESDAY"), condition.getConfiguration().get("days"),
                "The days must be converted to a list");
    }

    /**
     * Resolves the type UID to use for the given trigger annotation.
     */
    private static @Nullable String resolveTriggerTypeUid(Class<? extends Annotation> annotation) {
        if (annotation.equals(GenericAutomationTrigger.class)) {
            // the generic trigger carries its own type UID, it is verified separately
            return "custom:trigger";
        }
        return RuleAnnotationParser.TRIGGER_FROM_ANNOTATION.get(annotation);
    }

    private static @Nullable Trigger findTrigger(List<org.openhab.core.automation.Rule> rules, String typeUid) {
        return rules.stream().flatMap(rule -> rule.getTriggers().stream()).filter(trigger -> typeUid.equals(trigger.getTypeUID()))
                .findFirst().orElse(null);
    }

    private static @Nullable Condition findCondition(List<org.openhab.core.automation.Rule> rules, String typeUid) {
        return rules.stream().flatMap(rule -> rule.getConditions().stream())
                .filter(condition -> typeUid.equals(condition.getTypeUID())).findFirst().orElse(null);
    }

    /**
     * Counts how many times the given annotation is used on the given class.
     */
    private static long countUsages(Class<? extends Annotation> annotation, Class<?> clazz) {
        return Stream.of(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(annotation)).count()
                + Stream.of(clazz.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(annotation)).count();
    }

    /**
     * Parses the given script and returns the rules that have been created.
     */
    private static List<org.openhab.core.automation.Rule> parse(Object script) {
        List<org.openhab.core.automation.Rule> rules = new ArrayList<>();
        ScriptedAutomationManager automationManager = createAutomationManager(rules);

        try {
            RuleAnnotationParser.parse(script, "test-script", automationManager);
        } catch (IllegalArgumentException | RuleParserException e) {
            throw new AssertionError("Unable to parse the script " + script.getClass().getName(), e);
        }

        return rules;
    }

    /**
     * Creates a {@link ScriptedAutomationManager} collecting the rules that are added to it.
     */
    private static ScriptedAutomationManager createAutomationManager(List<org.openhab.core.automation.Rule> collectedRules) {
        RuleSupportRuleRegistryDelegate registryDelegate = mock(RuleSupportRuleRegistryDelegate.class);
        when(registryDelegate.add(any(org.openhab.core.automation.Rule.class))).thenAnswer(invocation -> {
            collectedRules.add(invocation.getArgument(0));
            return invocation.getArgument(0);
        });

        return new ScriptedAutomationManager(registryDelegate, mock(ScriptedCustomModuleHandlerFactory.class),
                mock(ScriptedCustomModuleTypeProvider.class), mock(ScriptedPrivateModuleHandlerFactory.class));
    }

    /**
     * A script using every trigger annotation of the helper library.
     */
    @NonNullByDefault({})
    public static class ScriptWithAllTriggers {
        @Rule
        @ChannelEventTrigger(channelUID = "astro:sun:rise")
        public Runnable channelEvent = () -> {
        };

        @Rule
        @CronTrigger(cronExpression = "0 0 * * * ?")
        public Runnable cron = () -> {
        };

        @Rule
        @DateTimeTrigger(itemName = "Alarm")
        public Runnable dateTime = () -> {
        };

        @Rule
        @GenericAutomationTrigger(typeUid = "custom:trigger", params = { "key=value" })
        public Runnable genericAutomation = () -> {
        };

        @Rule
        @GenericEventTrigger(topic = "openhab/items/light", source = "light", types = "On", payload = "ON")
        public Runnable genericEvent = () -> {
        };

        @Rule
        @GroupCommandTrigger(groupName = "Lights", command = "ON")
        public Runnable groupCommand = () -> {
        };

        @Rule
        @GroupStateChangeTrigger(groupName = "Lights", state = "ON")
        public Runnable groupStateChange = () -> {
        };

        @Rule
        @GroupStateUpdateTrigger(groupName = "Lights", state = "ON")
        public Runnable groupStateUpdate = () -> {
        };

        @Rule
        @ItemCommandTrigger(itemName = "Light", command = "ON")
        public Runnable itemCommand = () -> {
        };

        @Rule
        @ItemStateChangeTrigger(itemName = "Light", state = "ON", previousState = "OFF")
        public Runnable itemStateChange = () -> {
        };

        @Rule
        @ItemStateUpdateTrigger(itemName = "Light", state = "ON")
        public Runnable itemStateUpdate = () -> {
        };

        @Rule
        @SystemStartlevelTrigger(startlevel = 20)
        public Runnable systemStartlevel = () -> {
        };

        @Rule
        @ThingStatusChangeTrigger(thingUID = "astro:sun:rise", status = "ON")
        public Runnable thingStatusChange = () -> {
        };

        @Rule
        @ThingStatusUpdateTrigger(thingUID = "astro:sun:rise", status = "ON")
        public Runnable thingStatusUpdate = () -> {
        };

        @Rule
        @TimeOfDayTrigger(time = "08:00")
        public Runnable timeOfDay = () -> {
        };
    }

    /**
     * A script using every condition annotation of the helper library.
     */
    @NonNullByDefault({})
    public static class ScriptWithAllConditions {
        @Rule
        @DayOfWeekCondition(days = { "MONDAY", "TUESDAY" })
        public Runnable dayOfWeek = () -> {
        };

        @Rule
        @EphemerisDaysetCondition(dayset = "SUNRISE")
        public Runnable ephemerisDayset = () -> {
        };

        @Rule
        @EphemerisHolidayCondition
        public Runnable ephemerisHoliday = () -> {
        };

        @Rule
        @EphemerisNotHolidayCondition
        public Runnable ephemerisNotHoliday = () -> {
        };

        @Rule
        @EphemerisWeekdayCondition
        public Runnable ephemerisWeekday = () -> {
        };

        @Rule
        @EphemerisWeekendCondition
        public Runnable ephemerisWeekend = () -> {
        };

        @Rule
        @GenericCompareCondition(input = "light", operator = "==", right = "ON")
        public Runnable genericCompare = () -> {
        };

        @Rule
        @GenericEventCondition(topic = "openhab/items/light")
        public Runnable genericEvent = () -> {
        };

        @Rule
        @ItemStateCondition(itemName = "Light", operator = "==", state = "ON")
        public Runnable itemState = () -> {
        };

        @Rule
        @TimeOfDayCondition(startTime = "08:00", endTime = "20:00")
        public Runnable timeOfDay = () -> {
        };
    }

    /**
     * A script using a trigger annotation on a method instead of a field.
     */
    @NonNullByDefault({})
    public static class ScriptWithTriggerOnMethod {
        @Rule
        @ItemStateChangeTrigger(itemName = "Light", state = "ON")
        public void itemStateChange() {
        }
    }

    /**
     * A script using a trigger annotation where only some of the attributes are set.
     */
    @NonNullByDefault({})
    public static class ScriptWithPartialAttributes {
        @Rule
        @ItemStateChangeTrigger(itemName = "Light")
        public Runnable itemStateChange = () -> {
        };
    }

    /**
     * A script using repeatable annotations several times.
     */
    @NonNullByDefault({})
    public static class ScriptWithRepeatedAnnotations {
        @Rule
        @ItemStateChangeTrigger(itemName = "Light1", state = "ON")
        @ItemStateChangeTrigger(itemName = "Light2", state = "OFF")
        @ItemStateCondition(itemName = "Light1", operator = "==", state = "ON")
        @ItemStateCondition(itemName = "Light2", operator = "==", state = "OFF")
        public Runnable repeated = () -> {
        };
    }
}
