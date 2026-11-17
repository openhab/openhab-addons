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

import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests for the object number of a device: inside a module the {@code BaseNumber} argument plus the number of the
 * object. All data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectObjectNumberTest {

    private static final String FULL_BASE = APP + "_MD-1_A-1";
    private static final String SHORT_BASE = "MD-1_A-1";

    private final KnxProjectReader reader = new KnxProjectReader();

    private static KnxGroupObject object(KnxProject project, String refId) {
        return project.groupObjects().stream().filter(o -> refId.equals(o.instance().refId())).findFirst()
                .orElseThrow();
    }

    private static @Nullable Integer number(KnxProject project, String refId) {
        return object(project, refId).number();
    }

    private static String moduleInstance(String id, String arguments) {
        int end = id.lastIndexOf("_MI-");
        return "<ModuleInstance Id=\"" + id + "\" RefId=\"" + id.substring(0, end) + "\"><Arguments>" + arguments
                + "</Arguments></ModuleInstance>";
    }

    private static String argument(String refId, String value) {
        return "<Argument RefId=\"" + refId + "\" Value=\"" + value + "\" />";
    }

    private static String argumentDef(String id, String name, @Nullable String allocates) {
        return "<Argument Id=\"" + APP + "_" + id + "\" Name=\"" + name + "\""
                + (allocates == null ? "" : " Allocates=\"" + allocates + "\"") + " />";
    }

    private static String moduleObject(String instance, String object) {
        return "<ComObjectInstanceRef RefId=\"" + instance + "_" + object + "_R-1\" Links=\"GA-1\" />\n";
    }

    private static String moduleComObject(String id, int number, @Nullable String baseNumber) {
        return "<ComObject Id=\"" + APP + "_MD-1_" + id + "\" Number=\"" + number + "\""
                + (baseNumber == null ? "" : " BaseNumber=\"" + baseNumber + "\"") + " />";
    }

    private static String moduleReference(String id) {
        return "<ComObjectRef Id=\"" + APP + "_MD-1_" + id + "_R-1\" RefId=\"" + APP + "_MD-1_" + id + "\" />";
    }

    /** A module definition {@code MD-1} with these arguments and objects, each object with a reference. */
    private static String moduleDef(String arguments, String objects, String references) {
        return "<ModuleDefs><ModuleDef Id=\"" + APP + "_MD-1\" Name=\"Kanaal\"><Arguments>" + arguments
                + "</Arguments><Static><ComObjects>" + objects + "</ComObjects><ComObjectRefs>" + references
                + "</ComObjectRefs></Static></ModuleDef></ModuleDefs>";
    }

    private static String allocator() {
        return "<Static><Allocators><Allocator Id=\"" + APP + "_L-1\" Start=\"200\" /></Allocators></Static>";
    }

    private KnxProject read(TestContainer container) throws Exception {
        return reader.read(container.write(), null);
    }

    @Test
    void theNumberIsTheBaseArgumentPlusTheNumberOfTheObject() throws Exception {
        for (String base : new String[] { FULL_BASE, SHORT_BASE }) {
            String content = allocator() + moduleDef(argumentDef("MD-1_A-1", "base", "10"),
                    moduleComObject("O-1-1", 1, base), moduleReference("O-1-1"));
            String modules = "<ModuleInstances>" + moduleInstance("MD-1_M-1_MI-1", argument("MD-1_A-1", "100"))
                    + moduleInstance("MD-1_M-1_MI-2", argument("MD-1_A-1", "L-1")) + "</ModuleInstances>";

            KnxProject project = read(ProgramFixture.container(
                    moduleObject("MD-1_M-1_MI-1", "O-1-1") + moduleObject("MD-1_M-1_MI-2", "O-1-1"), modules, content,
                    ""));

            assertEquals(101, number(project, "MD-1_M-1_MI-1_O-1-1_R-1"), base + ": a literal value");
            assertEquals(211, number(project, "MD-1_M-1_MI-2_O-1-1_R-1"), base + ": 200 + (2 - 1) * 10, plus 1");
        }
    }

    @Test
    void anObjectWithoutABaseNumberKeepsItsNumber() throws Exception {
        String content = moduleDef(argumentDef("MD-1_A-1", "base", null), moduleComObject("O-1-1", 7, null),
                moduleReference("O-1-1")) + objectsThenReferences(comObject(3, ""), reference(3, ""));
        String modules = "<ModuleInstances>" + moduleInstance("MD-1_M-1_MI-1", argument("MD-1_A-1", "100"))
                + "</ModuleInstances>";

        KnxProject project = read(ProgramFixture.container(moduleObject("MD-1_M-1_MI-1", "O-1-1") + instance(3, ""),
                modules, content, ""));

        assertEquals(7, number(project, "MD-1_M-1_MI-1_O-1-1_R-1"));
        assertEquals(3, number(project, "O-3_R-3"));
    }

    /** Several objects whose base cannot be resolved, and one that can. */
    private TestContainer unresolvable() {
        String objects = moduleComObject("O-1-1", 1, FULL_BASE) + moduleComObject("O-1-2", 1, APP + "_MD-1_A-9")
                + moduleComObject("O-1-3", 1, APP + "_MD-1_A-2") + moduleComObject("O-1-4", 1, APP + "_MD-1_A-3")
                + moduleComObject("O-1-5", 1, APP + "_MD-1_A-4") + moduleComObject("O-1-6", 1, APP + "_MD-1_A-5");
        String references = moduleReference("O-1-1") + moduleReference("O-1-2") + moduleReference("O-1-3")
                + moduleReference("O-1-4") + moduleReference("O-1-5") + moduleReference("O-1-6");
        String arguments = argumentDef("MD-1_A-1", "base", null) + argumentDef("MD-1_A-2", "text", null)
                + argumentDef("MD-1_A-3", "high", null) + argumentDef("MD-1_A-4", "empty", null)
                + argumentDef("MD-1_A-5", "negative", null);
        String content = moduleDef(arguments, objects, references)
                + objectsThenReferences(comObject(3, "BaseNumber=\"" + FULL_BASE + "\""), reference(3, ""));
        String modules = "<ModuleInstances>"
                + moduleInstance("MD-1_M-1_MI-1",
                        argument("MD-1_A-1", "100") + argument("MD-1_A-2", "Hal") + argument("MD-1_A-3", "65535")
                                + argument("MD-1_A-5", "-20"))
                + moduleInstance("MD-1_M-1_MI-2", "") + "</ModuleInstances>";
        String instances = moduleObject("MD-1_M-1_MI-1", "O-1-1") + moduleObject("MD-1_M-1_MI-1", "O-1-2")
                + moduleObject("MD-1_M-1_MI-1", "O-1-3") + moduleObject("MD-1_M-1_MI-1", "O-1-4")
                + moduleObject("MD-1_M-1_MI-1", "O-1-5") + moduleObject("MD-1_M-1_MI-1", "O-1-6")
                + moduleObject("MD-1_M-1_MI-2", "O-1-1") + instance(3, "");
        return ProgramFixture.container(instances, modules, content, "");
    }

    @Test
    void aBaseThatCannotBeResolvedGivesNoNumberNeverTheBareModuleNumber() throws Exception {
        KnxProject project = read(unresolvable());

        assertEquals(101, number(project, "MD-1_M-1_MI-1_O-1-1_R-1"));
        assertNull(number(project, "MD-1_M-1_MI-1_O-1-2_R-1"), "no such argument");
        assertNull(number(project, "MD-1_M-1_MI-1_O-1-3_R-1"), "the value is not a number");
        assertNull(number(project, "MD-1_M-1_MI-1_O-1-4_R-1"), "65535 + 1 is out of range");
        assertNull(number(project, "MD-1_M-1_MI-1_O-1-5_R-1"), "the instance gives the argument no value");
        assertNull(number(project, "MD-1_M-1_MI-1_O-1-6_R-1"), "-20 + 1 is out of range");
        assertNull(number(project, "MD-1_M-1_MI-2_O-1-1_R-1"), "the instance has no such argument");
        assertNull(number(project, "O-3_R-3"), "an object outside a module");
    }

    private int unresolvedBases(TestContainer container) throws Exception {
        Path file = container.write();
        KnxProject project = reader.read(file, null);
        ReadBudget budget = new ReadBudget(ReadLimits.DEFAULT);
        try (OuterContainer opened = OuterContainer.open(file, budget, name -> {
        })) {
            ManufacturerReader manufacturer = new ManufacturerReader(opened, budget, null);
            manufacturer.read(project);
            return manufacturer.unresolvedBases();
        }
    }

    @Test
    void theUnresolvableBasesAreCounted() throws Exception {
        assertEquals(7, unresolvedBases(unresolvable()));
    }

    @Test
    void aResolvedBaseIsNotCounted() throws Exception {
        String content = moduleDef(argumentDef("MD-1_A-1", "base", null), moduleComObject("O-1-1", 1, SHORT_BASE),
                moduleReference("O-1-1"));
        String modules = "<ModuleInstances>" + moduleInstance("MD-1_M-1_MI-1", argument("MD-1_A-1", "5"))
                + "</ModuleInstances>";

        assertEquals(0, unresolvedBases(
                ProgramFixture.container(moduleObject("MD-1_M-1_MI-1", "O-1-1"), modules, content, "")));
    }

    private static final String SUB_ONE = "MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-1";
    private static final String SUB_TWO = "MD-1_M-1_MI-1_SM-1_M-1_MI-1-1-2";

    /** A submodule definition {@code MD-1_SM-1} with one object, 3 plus the base named. */
    private static String withSubmodule(String base) {
        return "<ModuleDefs><ModuleDef Id=\"" + APP + "_MD-1\" Name=\"Kanaal\"><Arguments>"
                + argumentDef("MD-1_A-1", "outer", null) + "</Arguments><SubModuleDefs><ModuleDef Id=\"" + APP
                + "_MD-1_SM-1\" Name=\"Logica\"><Arguments>" + argumentDef("MD-1_SM-1_A-1", "inner", "1")
                + "</Arguments><Static><ComObjects><ComObject Id=\"" + APP + "_MD-1_SM-1_O-3-1\" Number=\"3\""
                + " BaseNumber=\"" + base + "\" /></ComObjects><ComObjectRefs><ComObjectRef Id=\"" + APP
                + "_MD-1_SM-1_O-3-1_R-1\" RefId=\"" + APP + "_MD-1_SM-1_O-3-1\" /></ComObjectRefs></Static></ModuleDef>"
                + "</SubModuleDefs></ModuleDef></ModuleDefs>";
    }

    private static String subObject(String instance) {
        return "<ComObjectInstanceRef RefId=\"" + instance + "_SM-1_O-3-1_R-1\" Links=\"GA-1\" />\n";
    }

    @Test
    void aSubmoduleObjectTakesTheBaseOfItsOwnInstanceOrOfAParent() throws Exception {
        String modules = "<ModuleInstances>" + moduleInstance("MD-1_M-1_MI-1", argument("MD-1_A-1", "100"))
                + moduleInstance(SUB_ONE, argument("SM-1_A-1", "50")) + moduleInstance(SUB_TWO, "")
                + "</ModuleInstances>";
        String instances = subObject(SUB_ONE) + subObject(SUB_TWO);

        for (String base : new String[] { APP + "_MD-1_SM-1_A-1", "MD-1_SM-1_A-1" }) {
            KnxProject own = read(ProgramFixture.container(instances, modules, withSubmodule(base), ""));
            assertEquals(53, number(own, SUB_ONE + "_SM-1_O-3-1_R-1"), base + ": the submodule's argument, short id");
            assertNull(number(own, SUB_TWO + "_SM-1_O-3-1_R-1"), "the second instance gives it no value");
        }
        KnxProject parent = read(ProgramFixture.container(instances, modules, withSubmodule(FULL_BASE), ""));
        assertEquals(103, number(parent, SUB_ONE + "_SM-1_O-3-1_R-1"), "found at the parent instance");
        assertEquals(103, number(parent, SUB_TWO + "_SM-1_O-3-1_R-1"));
    }

    @Test
    void anAllocatorOfANestedSubmoduleInstanceGivesNoNumber() throws Exception {
        String modules = "<ModuleInstances>" + moduleInstance("MD-1_M-1_MI-1", "")
                + moduleInstance(SUB_ONE, argument("SM-1_A-1", "L-1")) + "</ModuleInstances>";
        String content = allocator() + withSubmodule(APP + "_MD-1_SM-1_A-1");

        KnxProject project = read(ProgramFixture.container(subObject(SUB_ONE), modules, content, ""));

        assertNull(number(project, SUB_ONE + "_SM-1_O-3-1_R-1"));
    }
}
