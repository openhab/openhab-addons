/*
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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.knx.internal.project.ProgramFixture.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Tests for the names of group objects: placeholders filled from parameters and module arguments, the
 * fallback, and the language of the texts.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectNamesTest {

    private static final long LARGE = 1L << 30;

    private final List<String> opened = Collections.synchronizedList(new ArrayList<>());
    private final KnxProjectReader reader = new KnxProjectReader(ReadLimits.DEFAULT, opened::add);

    private KnxProject read(TestContainer container) throws Exception {
        return reader.read(container.write(), null);
    }

    private KnxProject read(TestContainer container, @Nullable String language) throws Exception {
        return reader.read(container.write(), null, language);
    }

    private static KnxGroupObject object(KnxProject project, String refId) {
        return project.groupObjects().stream().filter(o -> refId.equals(o.instance().refId())).findFirst()
                .orElseThrow();
    }

    private static String name(KnxProject project, String refId) {
        return object(project, refId).name();
    }

    private static TestContainer generation(String fixture, String part, String folder, String program) {
        return TestContainer.of(fixture, part)
                .put(folder + "/Hardware.xml", TestContainer.resource("manufacturer/" + fixture + "/Hardware.xml"))
                .put(folder + "/" + program + ".xml",
                        TestContainer.resource("manufacturer/" + fixture + "/" + program + ".xml"));
    }

    /** A {@code ComObjectRef} of object n naming the text parameter {@code <APP>_P-<n>_R-<n>}. */
    private static String textReference(int n) {
        return reference(n, "TextParameterRefId=\"" + APP + "_P-" + n + "_R-" + n + "\"");
    }

    private static String parameter(int n, @Nullable String value) {
        return "<Parameter Id=\"" + APP + "_P-" + n + "\" Name=\"P" + n + "\""
                + (value == null ? "" : " Value=\"" + value + "\"") + " />";
    }

    private static String parameterRef(int n, @Nullable String value) {
        return "<ParameterRef Id=\"" + APP + "_P-" + n + "_R-" + n + "\" RefId=\"" + APP + "_P-" + n + "\""
                + (value == null ? "" : " Value=\"" + value + "\"") + " />";
    }

    /** {@code Static} in the usual order: parameters, parameter references, objects, object references. */
    private static String usualOrder(String parameters, String parameterRefs, String objects, String references) {
        return "<Static><Parameters>" + parameters + "</Parameters><ParameterRefs>" + parameterRefs
                + "</ParameterRefs><ComObjectTable>" + objects + "</ComObjectTable><ComObjectRefs>" + references
                + "</ComObjectRefs></Static>";
    }

    private static String parameterValues(String values) {
        return "<ParameterInstanceRefs>" + values + "</ParameterInstanceRefs>";
    }

    private static String moduleInstance(String id, String arguments) {
        int end = id.lastIndexOf("_MI-");
        return "<ModuleInstance Id=\"" + id + "\" RefId=\"" + id.substring(0, end) + "\"><Arguments>" + arguments
                + "</Arguments></ModuleInstance>";
    }

    private static String argument(String refId, String value) {
        return "<Argument RefId=\"" + refId + "\" Value=\"" + value + "\" />";
    }

    private static String moduleObject(String instance, String object) {
        return "<ComObjectInstanceRef RefId=\"" + instance + "_" + object + "_R-1\" Links=\"GA-1\" />\n";
    }

    /** A module definition {@code MD-1} with these arguments and this further {@code Static} content. */
    private static String moduleDef(String arguments, String objects, String references, String moreStatic) {
        return "<ModuleDefs><ModuleDef Id=\"" + APP + "_MD-1\" Name=\"Kanaal\"><Arguments>" + arguments
                + "</Arguments><Static>" + moreStatic + "<ComObjects>" + objects + "</ComObjects><ComObjectRefs>"
                + references + "</ComObjectRefs></Static></ModuleDef></ModuleDefs>";
    }

    private static String argumentDef(String id, String name, @Nullable String allocates) {
        return "<Argument Id=\"" + APP + "_" + id + "\" Name=\"" + name + "\""
                + (allocates == null ? "" : " Allocates=\"" + allocates + "\"") + " />";
    }

    private static String moduleComObject(String id, String text) {
        return "<ComObject Id=\"" + APP + "_MD-1_" + id + "\" Number=\"1\" Text=\"" + text + "\" />";
    }

    private static String moduleReference(String id, String attributes) {
        return "<ComObjectRef Id=\"" + APP + "_MD-1_" + id + "_R-1\" RefId=\"" + APP + "_MD-1_" + id + "\" "
                + attributes + " />";
    }

    @Test
    void theNotesExamplesOfAParameterPlaceholder() throws Exception {
        String instances = instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-2\"");
        String device = parameterValues(
                parameterValue(APP + "_P-1_R-1", "Hall") + parameterValue(APP + "_P-2_R-2", ""));
        String content = usualOrder(parameter(1, "") + parameter(2, ""), parameterRef(1, null) + parameterRef(2, null),
                comObject(1, "Text=\"Output {{0:A}} switch\"") + comObject(2, "Text=\"Output {{0:A}} switch\""),
                textReference(1) + textReference(2));

        KnxProject project = read(ProgramFixture.container(instances, device, content, ""));

        assertEquals("Output Hall switch", name(project, "O-1_R-1"));
        assertEquals("Output A switch", name(project, "O-2_R-2"));
        assertEquals("Output {{0:A}} switch", object(project, "O-1_R-1").text(), "the text stays as written");
    }

    @Test
    void theParameterValueComesFromTheDeviceThenTheReferenceThenTheParameter() throws Exception {
        String instances = instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-1\"") + instance(3, "Links=\"GA-1\"")
                + instance(4, "Links=\"GA-1\"") + instance(5, "Links=\"GA-1\"");
        String device = parameterValues(parameterValue(APP + "_P-1_R-1", "Device")
                + parameterValue(APP + "_P-4_R-4", null) + parameterValue(APP + "_P-5_R-5", ""));
        String parameters = parameter(1, "Parameter") + parameter(2, "Parameter") + "<Union SizeInBit=\"8\">"
                + parameter(3, "In union") + "</Union>" + parameter(4, "Parameter four")
                + parameter(5, "Parameter five");
        String parameterRefs = parameterRef(1, "Reference") + parameterRef(2, "Reference") + parameterRef(3, null)
                + parameterRef(4, null) + parameterRef(5, "Reference");
        StringBuilder objects = new StringBuilder();
        StringBuilder references = new StringBuilder();
        for (int n = 1; n <= 5; n++) {
            objects.append(comObject(n, "Text=\"Name {{0:Default}}\""));
            references.append(textReference(n));
        }

        KnxProject project = read(ProgramFixture.container(instances, device,
                usualOrder(parameters, parameterRefs, objects.toString(), references.toString()), ""));

        assertEquals("Name Device", name(project, "O-1_R-1"));
        assertEquals("Name Reference", name(project, "O-2_R-2"));
        assertEquals("Name In union", name(project, "O-3_R-3"));
        assertEquals("Name Parameter four", name(project, "O-4_R-4"), "a stored value without Value is no value");
        assertEquals("Name Default", name(project, "O-5_R-5"), "an empty device value gives the default");
    }

    @Test
    void aPlaceholderWithoutDefault() throws Exception {
        String instances = instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-1\" Text=\"Stored\"")
                + instance(3, "Links=\"GA-1\" Text=\"Stored\"");
        String device = parameterValues(
                parameterValue(APP + "_P-1_R-1", "Keuken") + parameterValue(APP + "_P-2_R-2", ""));
        String content = usualOrder(parameter(1, "") + parameter(2, ""), parameterRef(1, null) + parameterRef(2, null),
                comObject(1, "Text=\"Dimmer {{0}}\"") + comObject(2, "Text=\"Dimmer {{0}}\"")
                        + comObject(3, "Text=\"Dimmer {{0}}\""),
                textReference(1) + textReference(2) + reference(3, ""));

        KnxProject project = read(ProgramFixture.container(instances, device, content, ""));

        assertEquals("Dimmer Keuken", name(project, "O-1_R-1"));
        assertEquals("Dimmer ", name(project, "O-2_R-2"), "an empty value fills the placeholder");
        assertEquals("Stored", name(project, "O-3_R-3"), "without a text parameter there is nothing to fill it");
    }

    @Test
    void aTextParameterInsideAModuleIsKeyedByTheModuleInstance() throws Exception {
        String instances = moduleObject("MD-1_M-1_MI-1", "O-2-1") + moduleObject("MD-1_M-1_MI-2", "O-2-1");
        String device = parameterValues(parameterValue(APP + "_MD-1_P-4_R-4", "Not this one")
                + parameterValue(APP + "_MD-1_M-1_MI-1_P-4_R-4", "Eerste")
                + parameterValue(APP + "_MD-1_M-1_MI-2_P-4_R-4", "Tweede"));
        String content = moduleDef("", moduleComObject("O-2-1", "Channel {{0:X}}"),
                moduleReference("O-2-1", "TextParameterRefId=\"" + APP + "_MD-1_P-4_R-4\""),
                "<Parameters><Parameter Id=\"" + APP + "_MD-1_P-4\" Value=\"\" /></Parameters><ParameterRefs>"
                        + "<ParameterRef Id=\"" + APP + "_MD-1_P-4_R-4\" RefId=\"" + APP + "_MD-1_P-4\" />"
                        + "</ParameterRefs>");

        KnxProject project = read(ProgramFixture.container(instances, device, content, ""));

        assertEquals("Channel Eerste", name(project, "MD-1_M-1_MI-1_O-2-1_R-1"));
        assertEquals("Channel Tweede", name(project, "MD-1_M-1_MI-2_O-2-1_R-1"));
    }

    @Test
    void allocatorArgumentsCountUpWithTheModuleInstance() throws Exception {
        StringBuilder instances = new StringBuilder();
        StringBuilder modules = new StringBuilder("<ModuleInstances>");
        for (int k = 1; k <= 3; k++) {
            String id = "MD-1_M-1_MI-" + k;
            instances.append(moduleObject(id, "O-1-1")).append(moduleObject(id, "O-2-1"));
            modules.append(moduleInstance(id, argument("MD-1_A-1", "L-1") + argument("MD-1_A-2", "MD-1_L-2")));
        }
        modules.append("</ModuleInstances>");
        String content = "<Static><Allocators><Allocator Id=\"" + APP
                + "_L-1\" Name=\"Kanalen\" Start=\"1\" maxInclusive=\"8\" /></Allocators></Static>"
                + moduleDef(argumentDef("MD-1_A-1", "ch", "1") + argumentDef("MD-1_A-2", "out", "4"),
                        moduleComObject("O-1-1", "{{ch}}: status") + moduleComObject("O-2-1", "Out {{out}}"),
                        moduleReference("O-1-1", "") + moduleReference("O-2-1", ""),
                        "<Allocators><Allocator Id=\"" + APP + "_MD-1_L-2\" Start=\"10\" /></Allocators>");

        KnxProject project = read(ProgramFixture.container(instances.toString(), modules.toString(), content, ""));

        for (int k = 1; k <= 3; k++) {
            assertEquals(k + ": status", name(project, "MD-1_M-1_MI-" + k + "_O-1-1_R-1"));
            assertEquals("Out " + (10 + (k - 1) * 4), name(project, "MD-1_M-1_MI-" + k + "_O-2-1_R-1"));
        }
    }

    @Test
    void aPlainArgumentValueIsUsedAsItIs() throws Exception {
        String modules = "<ModuleInstances>" + moduleInstance("MD-1_M-1_MI-3", argument("MD-1_A-1", "3"))
                + moduleInstance("MD-1_M-1_MI-4", argument("MD-1_A-1", "Hal")) + "</ModuleInstances>";
        String content = moduleDef(argumentDef("MD-1_A-1", "ch", "1"), moduleComObject("O-1-1", "{{ch}}: status"),
                moduleReference("O-1-1", ""), "");

        KnxProject project = read(ProgramFixture.container(
                moduleObject("MD-1_M-1_MI-3", "O-1-1") + moduleObject("MD-1_M-1_MI-4", "O-1-1"), modules, content, ""));

        assertEquals("3: status", name(project, "MD-1_M-1_MI-3_O-1-1_R-1"));
        assertEquals("Hal: status", name(project, "MD-1_M-1_MI-4_O-1-1_R-1"));
    }

    /** A submodule definition {@code MD-1_SM-1} inside {@code MD-1}. */
    private static String withSubmodule(String moduleArguments, String subArguments, String subText) {
        return "<ModuleDefs><ModuleDef Id=\"" + APP + "_MD-1\" Name=\"Kanaal\"><Arguments>" + moduleArguments
                + "</Arguments><SubModuleDefs><ModuleDef Id=\"" + APP + "_MD-1_SM-1\" Name=\"Logica\"><Arguments>"
                + subArguments + "</Arguments><Static><ComObjects><ComObject Id=\"" + APP
                + "_MD-1_SM-1_O-3-1\" Number=\"3\" Text=\"" + subText + "\" /></ComObjects><ComObjectRefs>"
                + "<ComObjectRef Id=\"" + APP + "_MD-1_SM-1_O-3-1_R-1\" RefId=\"" + APP + "_MD-1_SM-1_O-3-1\" />"
                + "</ComObjectRefs></Static></ModuleDef></SubModuleDefs></ModuleDef></ModuleDefs>";
    }

    private static final String SUB_ONE = "MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-1";
    private static final String SUB_TWO = "MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-2";

    private static String subObject(String instance, String attributes) {
        return "<ComObjectInstanceRef RefId=\"" + instance + "_SM-1_O-3-1_R-1\" Links=\"GA-1\" " + attributes + " />\n";
    }

    @Test
    void argumentsAreFoundAtTheNearestModuleInstanceFirstThenItsParents() throws Exception {
        String modules = "<ModuleInstances>"
                + moduleInstance("MD-1_M-1_MI-1", argument("MD-1_A-1", "Hal") + argument("MD-1_A-2", "Gelijkvloers"))
                + moduleInstance(SUB_ONE, argument("SM-1_A-1", "Sub")) + moduleInstance(SUB_TWO, "")
                + "</ModuleInstances>";
        String content = withSubmodule(argumentDef("MD-1_A-1", "name", null) + argumentDef("MD-1_A-2", "room", null),
                argumentDef("MD-1_SM-1_A-1", "name", null), "{{name}} in {{room}}");

        KnxProject project = read(
                ProgramFixture.container(subObject(SUB_ONE, "") + subObject(SUB_TWO, ""), modules, content, ""));

        assertEquals("Sub in Gelijkvloers", name(project, SUB_ONE + "_SM-1_O-3-1_R-1"),
                "the submodule's own argument, matched by its short id, wins over the parent's");
        assertEquals("Hal in Gelijkvloers", name(project, SUB_TWO + "_SM-1_O-3-1_R-1"));
    }

    @Test
    void anAllocatorOfANestedSubmoduleInstanceIsLeftOpen() throws Exception {
        String modules = "<ModuleInstances>" + moduleInstance("MD-1_M-1_MI-1", "")
                + moduleInstance(SUB_ONE, argument("SM-1_A-1", "L-1")) + "</ModuleInstances>";
        String content = "<Static><Allocators><Allocator Id=\"" + APP + "_L-1\" Start=\"1\" /></Allocators></Static>"
                + withSubmodule("", argumentDef("MD-1_SM-1_A-1", "nr", "1"), "Logic {{nr}}");

        KnxProject project = read(ProgramFixture.container(
                subObject(SUB_ONE, "Text=\"Logica een\"") + subObject(SUB_ONE.replace("MI-1-1-1", "MI-1-1-3"), ""),
                modules, content, ""));

        assertEquals("Logica een", name(project, SUB_ONE + "_SM-1_O-3-1_R-1"));
        assertEquals("Logic ", name(project, SUB_ONE.replace("MI-1-1-1", "MI-1-1-3") + "_SM-1_O-3-1_R-1"));
    }

    @Test
    void nestedPlaceholdersAreFilledFromTheInside() throws Exception {
        String instances = moduleObject("MD-1_M-1_MI-1", "O-1-1") + moduleObject("MD-1_M-1_MI-2", "O-1-1");
        String device = "<ParameterInstanceRefs>" + parameterValue(APP + "_MD-1_M-1_MI-1_P-1_R-1", "Eigen")
                + "</ParameterInstanceRefs><ModuleInstances>"
                + moduleInstance("MD-1_M-1_MI-1", argument("MD-1_A-1", "4"))
                + moduleInstance("MD-1_M-1_MI-2", argument("MD-1_A-1", "5")) + "</ModuleInstances>";
        String content = moduleDef(argumentDef("MD-1_A-1", "Num", null), moduleComObject("O-1-1", "{{0:Dev {{Num}}}}"),
                moduleReference("O-1-1", "TextParameterRefId=\"" + APP + "_MD-1_P-1_R-1\""),
                "<Parameters><Parameter Id=\"" + APP + "_MD-1_P-1\" Value=\"\" /></Parameters><ParameterRefs>"
                        + "<ParameterRef Id=\"" + APP + "_MD-1_P-1_R-1\" RefId=\"" + APP + "_MD-1_P-1\" />"
                        + "</ParameterRefs>");

        KnxProject project = read(ProgramFixture.container(instances, device, content, ""));

        assertEquals("Eigen", name(project, "MD-1_M-1_MI-1_O-1-1_R-1"));
        assertEquals("Dev 5", name(project, "MD-1_M-1_MI-2_O-1-1_R-1"));
    }

    private static String nested(int depth) {
        return "{{0:".repeat(depth) + "x" + "}}".repeat(depth);
    }

    @Test
    void theNumberOfRoundsIsBounded() throws Exception {
        String instances = instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-1\" Text=\"Stored\"")
                + instance(3, "Links=\"GA-1\"");
        String content = objectsThenReferences(
                comObject(1, "Text=\"" + nested(ObjectNames.MAX_ROUNDS) + "\"")
                        + comObject(2, "Text=\"" + nested(ObjectNames.MAX_ROUNDS + 1) + "\"")
                        + comObject(3, "Text=\"a" + nested(ObjectNames.MAX_ROUNDS + 1) + "b\""),
                reference(1, "") + reference(2, "") + reference(3, ""));

        KnxProject project = read(ProgramFixture.container(instances, "", content, ""));

        assertEquals("x", name(project, "O-1_R-1"));
        assertEquals("Stored", name(project, "O-2_R-2"));
        assertEquals("ab", name(project, "O-3_R-3"), "what is left unfilled is removed");
    }

    @Test
    void theLengthOfANameIsBounded() throws Exception {
        String fits = "v".repeat(ObjectNames.MAX_LENGTH - 2);
        String tooLong = "v".repeat(ObjectNames.MAX_LENGTH - 1);
        String instances = instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-1\" Text=\"Stored\"")
                + instance(3, "Links=\"GA-1\"");
        String device = parameterValues(parameterValue(APP + "_P-1_R-1", fits)
                + parameterValue(APP + "_P-2_R-2", tooLong) + parameterValue(APP + "_P-3_R-3", tooLong));
        String content = usualOrder(parameter(1, "") + parameter(2, "") + parameter(3, ""),
                parameterRef(1, null) + parameterRef(2, null) + parameterRef(3, null),
                comObject(1, "Text=\"[{{0}}]\"") + comObject(2, "Text=\"[{{0}}]\"") + comObject(3, "Text=\"[{{0}}]\""),
                textReference(1) + textReference(2) + textReference(3));

        KnxProject project = read(ProgramFixture.container(instances, device, content, ""));

        assertEquals("[" + fits + "]", name(project, "O-1_R-1"));
        assertEquals("Stored", name(project, "O-2_R-2"));
        assertEquals("[]", name(project, "O-3_R-3"));
    }

    @Test
    void aPlaceholderThatCannotBeFilledGivesTheInstanceTextElseItIsRemoved() throws Exception {
        String instances = instance(1, "Links=\"GA-1\" Text=\"Stored\"") + instance(2, "Links=\"GA-1\"")
                + instance(3, "Links=\"GA-1\" Text=\"Old stored text\"") + instance(4, "Links=\"GA-1\" Text=\"\"");
        String device = parameterValues(parameterValue(APP + "_P-3_R-3", "Hall"));
        String content = usualOrder(parameter(3, ""), parameterRef(3, null),
                comObject(1, "Text=\"Output {{missing}} switch\"") + comObject(2, "Text=\"Output {{missing}} switch\"")
                        + comObject(3, "Text=\"Output {{0}}\"") + comObject(4, "Text=\"Output {{}}\""),
                reference(1, "") + reference(2, "") + textReference(3) + reference(4, ""));

        KnxProject project = read(ProgramFixture.container(instances, device, content, ""));

        assertEquals("Stored", name(project, "O-1_R-1"));
        assertEquals("Output  switch", name(project, "O-2_R-2"));
        assertEquals("Output Hall", name(project, "O-3_R-3"), "the computed name wins over a stored text");
        assertEquals("Output ", name(project, "O-4_R-4"), "an empty stored text is no text");
        assertEquals("Old stored text", object(project, "O-3_R-3").text());
    }

    @Test
    void theNameStartsWithTheReferenceTextElseTheObjectText() throws Exception {
        String instances = instance(1, "Links=\"GA-1\" Text=\"Stored\"") + instance(2, "Links=\"GA-1\"")
                + instance(3, "Links=\"GA-1\" Text=\"Stored\"") + instance(4, "Links=\"GA-1\"");
        String content = objectsThenReferences(
                comObject(1, "Text=\"Object\"") + comObject(2, "Text=\"Object\"") + comObject(3, "")
                        + comObject(4, "Text=\"\""),
                reference(1, "Text=\"Reference\"") + reference(2, "Text=\"\"") + reference(3, "") + reference(4, ""));

        KnxProject project = read(ProgramFixture.container(instances, "", content, ""));

        assertEquals("Reference", name(project, "O-1_R-1"));
        assertEquals("Object", name(project, "O-2_R-2"));
        assertEquals("Stored", name(project, "O-3_R-3"), "no program text: the instance text");
        assertEquals("", name(project, "O-4_R-4"));
    }

    private static final String LANGUAGE_INSTANCES = instance(1, "Links=\"GA-1\"") + instance(2, "Links=\"GA-1\"")
            + instance(3, "Links=\"GA-1\"");

    private static final String LANGUAGE_PROGRAM = objectsThenReferences(
            comObject(1, "Text=\"Switch\" FunctionText=\"Light\"") + comObject(2, "Text=\"Value\" FunctionText=\"Dim\"")
                    + comObject(3, "Text=\"Alarm\" FunctionText=\"Device\""),
            reference(1, "") + reference(2, "Text=\"Brightness\" FunctionText=\"Dimming\"") + reference(3, ""));

    private static final String LANGUAGES = language("de-AT",
            translation(APP + "_O-1", "Text", "Schalter AT") + translation(APP + "_O-2_R-2", "Text", "Helligkeit AT"))
            + language("de-DE",
                    translation(APP + "_O-1", "Text", "Schalten DE")
                            + translation(APP + "_O-1", "FunctionText", "Licht DE")
                            + translation(APP + "_O-1_R-1", "Text", "Not the element of the text")
                            + translation(APP + "_O-2", "Text", "Wert DE")
                            + translation(APP + "_O-2_R-2", "Text", "Helligkeit DE")
                            + translation(APP + "_O-2_R-2", "FunctionText", "Dimmen DE"))
            + language("de-DE", translation(APP + "_O-3", "Text", "Second de-DE is not read"));

    private KnxProject readLanguages(@Nullable String language) throws Exception {
        return read(ProgramFixture.container(LANGUAGE_INSTANCES, "", LANGUAGE_PROGRAM, LANGUAGES), language);
    }

    @Test
    void theLanguageEqualToTheTagIsTaken() throws Exception {
        for (String tag : List.of("de-DE", "DE-de")) {
            KnxProject project = readLanguages(tag);

            assertEquals("Schalten DE", name(project, "O-1_R-1"), tag);
            assertEquals("Licht DE", object(project, "O-1_R-1").functionText(), tag);
            assertEquals("Helligkeit DE", name(project, "O-2_R-2"), "the translation of the reference");
            assertEquals("Dimmen DE", object(project, "O-2_R-2").functionText());
            assertEquals("Alarm", name(project, "O-3_R-3"), "no translation: the default text");
            assertEquals("Device", object(project, "O-3_R-3").functionText());
            assertEquals("Switch", object(project, "O-1_R-1").text(), "the text stays as written");
        }
    }

    @Test
    void elseTheFirstLanguageWithTheSameLanguagePart() throws Exception {
        for (String tag : List.of("de-CH", "de", "DE-ch")) {
            KnxProject project = readLanguages(tag);

            assertEquals("Schalter AT", name(project, "O-1_R-1"), tag);
            assertEquals("Light", object(project, "O-1_R-1").functionText(), "no translation: the default text");
            assertEquals("Helligkeit AT", name(project, "O-2_R-2"));
        }
    }

    @Test
    void elseAndWithoutALanguageTheDefaultTexts() throws Exception {
        for (KnxProject project : List.of(readLanguages("fr-FR"), readLanguages(null), readLanguages(""),
                read(ProgramFixture.container(LANGUAGE_INSTANCES, "", LANGUAGE_PROGRAM, LANGUAGES)))) {
            assertEquals("Switch", name(project, "O-1_R-1"));
            assertEquals("Light", object(project, "O-1_R-1").functionText());
            assertEquals("Brightness", name(project, "O-2_R-2"));
            assertEquals("Dimming", object(project, "O-2_R-2").functionText());
        }
    }

    @Test
    void aTranslatedTextIsFilledLikeTheDefaultText() throws Exception {
        String device = parameterValues(parameterValue(APP + "_P-1_R-1", "Hal"));
        String content = usualOrder(parameter(1, ""), parameterRef(1, null), comObject(1, "Text=\"Output {{0:A}}\""),
                textReference(1));
        String languages = language("nl-BE", translation(APP + "_O-1", "Text", "Uitgang {{0:A}}"));

        KnxProject project = read(ProgramFixture.container(instance(1, "Links=\"GA-1\""), device, content, languages),
                "nl-BE");

        assertEquals("Uitgang Hal", name(project, "O-1_R-1"));
        assertEquals(3, opens(PROGRAM_ENTRY), "parameter before reference before the object reference");
    }

    @Test
    void translationsBeforeTheProgramAreFoundToo() throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), "",
                objectsThenReferences(comObject(1, "Text=\"Switch\""), reference(1, "")), "");
        String languages = "<Languages>" + language("de-DE", translation(APP + "_O-1", "Text", "Schalten"))
                + "</Languages>";
        container.put(PROGRAM_ENTRY,
                container.text(PROGRAM_ENTRY).replace("<ApplicationPrograms>", languages + "<ApplicationPrograms>"));

        assertEquals("Schalten", name(read(container, "de-DE"), "O-1_R-1"));
    }

    @Test
    void translationsAndParametersCountAgainstTheEntriesKept() throws Exception {
        String device = parameterValues(parameterValue(APP + "_P-1_R-1", "Hal"));
        String content = usualOrder(parameter(1, ""), parameterRef(1, null), comObject(1, "Text=\"Output {{0}}\""),
                textReference(1));
        TestContainer container = ProgramFixture.container(instance(1, "Links=\"GA-1\""), device, content,
                language("nl-BE", translation(APP + "_O-1", "Text", "Uitgang {{0}}")));
        // reference, object, parameter reference, parameter, translation
        int needed = 5;

        assertEquals(Reason.LIMIT_EXCEEDED,
                assertThrows(KnxProjectException.class,
                        () -> new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 100, 1 << 20, 1 << 20, needed - 1))
                                .read(container.write(), null, "nl-BE"))
                        .getReason());
        assertEquals("Uitgang Hal",
                name(new KnxProjectReader(new ReadLimits(100, LARGE, LARGE, 100, 1 << 20, 1 << 20, needed))
                        .read(container.write(), null, "nl-BE"), "O-1_R-1"));
    }

    @Test
    void theGenerationFixturesGetTheirNames() throws Exception {
        KnxProject ets4 = read(generation("ets4", "P-0A1B", "M-00FA", "M-00FA_A-0022-33-4455"));
        assertEquals("Schalten", name(ets4, "M-00FA_A-0022-33-4455_O-0_R-1"));
        KnxProject ets4Dutch = read(generation("ets4", "P-0A1B", "M-00FA", "M-00FA_A-0022-33-4455"), "nl-NL");
        assertEquals("Schakelen", name(ets4Dutch, "M-00FA_A-0022-33-4455_O-0_R-1"));

        KnxProject ets5 = read(generation("ets5", "P-1C2D", "M-0083", "M-0083_A-00AA-11-B2C3"));
        assertEquals("Temperature Hal", name(ets5, "MD-1_M-1_MI-1_O-1-1_R-1"));
        assertEquals("Brightness", name(ets5, "O-2_R-3"), "the computed name wins over the instance text");

        KnxProject ets6 = read(generation("ets6", "P-3E4F", "M-00C8", "M-00C8_A-0101-20-0F0F"));
        assertEquals("Switch Hall", name(ets6, "O-1_R-1"));
        assertEquals("Colour 3", name(ets6, "MD-1_M-1_MI-2_O-3-1_R-4"));
        KnxProject ets6German = read(generation("ets6", "P-3E4F", "M-00C8", "M-00C8_A-0101-20-0F0F"), "de-DE");
        assertEquals("Schalten Hall", name(ets6German, "O-1_R-1"));

        KnxProject modular = read(generation("modular", "P-5A6B", "M-00C8", "M-00C8_A-0110-10-AAAA"));
        assertEquals("Output Hal", name(modular, "MD-1_M-1_MI-1_O-2-1_R-1"));
        assertEquals("Output Keuken", name(modular, "MD-1_M-1_MI-2_O-2-1_R-1"));
        assertEquals("Logic input", name(modular, "MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-1_SM-1_O-3-1_R-1"));
    }

    @Test
    void withoutManufacturerDataNamesComeFromTheInstanceText() throws Exception {
        KnxProject project = read(TestContainer.of("ets5", "P-1C2D"), "de-DE");

        assertEquals("Uitgang A", name(project, "O-2_R-3"));
        assertEquals("", name(project, "O-0_R-1"));
    }

    private long opens(String entry) {
        return opened.stream().filter(entry::equals).count();
    }
}
