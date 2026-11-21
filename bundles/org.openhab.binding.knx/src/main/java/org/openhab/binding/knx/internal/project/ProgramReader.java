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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;
import org.openhab.binding.knx.internal.project.ProgramFile.AllocatorEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ArgumentEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ComObjectEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ComObjectRefEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ParameterEntry;
import org.openhab.binding.knx.internal.project.ProgramFile.ParameterRefEntry;

/**
 * Reads one application program file into a {@link ProgramFile}, in as many passes as its wanted entries need.
 * Only the {@code ApplicationProgram} with the wanted id is looked into, and outside it the translations;
 * the order of the sections does not matter. {@code Dynamic} and {@code BusInterfaces} are passed over as a
 * whole.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProgramReader {

    /**
     * Each link of a chain of references, for example object reference to object, can cost one pass when the
     * entry it names comes earlier in the file. The chains are short; this only bounds a file that keeps
     * naming entries the pass has already passed.
     */
    static final int MAX_PASSES = 4;

    /** Takes one start tag inside the program; {@code open} holds its ancestors, the parent first. */
    @FunctionalInterface
    interface Element {
        void start(XMLStreamReader xml, Deque<String> open, ProgramFile file) throws KnxProjectException;
    }

    private static final Set<String> SKIPPED = Set.of("Dynamic", "BusInterfaces");
    private static final Map<String, Element> ELEMENTS = Map.of("ComObject", ProgramReader::comObject, "ComObjectRef",
            ProgramReader::comObjectRef, "ParameterRef", ProgramReader::parameterRef, "Parameter",
            ProgramReader::parameter, "Allocator", ProgramReader::allocator, "Argument", ProgramReader::argument);
    /** Elements outside the {@code ApplicationProgram}: the translations. */
    private static final Map<String, Element> OUTSIDE = Map.of("Language", ProgramReader::language,
            "TranslationElement", ProgramReader::translationElement, "Translation", ProgramReader::translation);

    private ProgramReader() {
    }

    /** @return the number of passes */
    static int read(OuterContainer container, ZipEntry entry, ProgramFile file, ReadBudget budget)
            throws IOException, KnxProjectException {
        return read(container, entry, file, budget, MAX_PASSES);
    }

    /** The same with another bound; the longest chain of a valid file needs 3 passes, so tests lower it. */
    static int read(OuterContainer container, ZipEntry entry, ProgramFile file, ReadBudget budget, int maxPasses)
            throws IOException, KnxProjectException {
        int passes = 0;
        boolean again = true;
        while (again && passes < maxPasses) {
            file.startPass();
            try (InputStream in = container.open(entry)) {
                readOnce(in, entry.getName(), file, budget);
            }
            passes++;
            again = file.endPass();
        }
        if (again) {
            file.reachedPassBound();
        }
        return passes;
    }

    private static void readOnce(InputStream in, String name, ProgramFile file, ReadBudget budget)
            throws KnxProjectException {
        XMLStreamReader xml = null;
        try {
            xml = XmlInput.open(in, name, budget);
            Deque<String> open = new ArrayDeque<>();
            int programDepth = -1;
            while (xml.hasNext()) {
                int event = XmlInput.next(xml, name);
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String element = xml.getLocalName();
                    String parent = open.peek();
                    if (open.isEmpty() && !"KNX".equals(element)) {
                        throw new KnxProjectException(Reason.MALFORMED, name + " is not a KNX manufacturer file");
                    }
                    if (SKIPPED.contains(element)) {
                        XmlInput.skipElement(xml, name);
                        continue;
                    }
                    if ("ApplicationProgram".equals(element) && "ApplicationPrograms".equals(parent)) {
                        if (!file.id().equals(XmlInput.text(xml, "Id"))) {
                            XmlInput.skipElement(xml, name);
                            continue;
                        }
                        file.foundProgram(XmlInput.attribute(xml, "DefaultLanguage"));
                        programDepth = open.size();
                    } else {
                        Element handler = (programDepth >= 0 ? ELEMENTS : OUTSIDE).get(element);
                        if (handler != null) {
                            handler.start(xml, open, file);
                        }
                    }
                    open.push(element);
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    open.pop();
                    if (open.size() == programDepth) {
                        programDepth = -1;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw XmlInput.failure(e, name, budget);
        } finally {
            XmlInput.close(xml);
        }
    }

    private static void comObject(XMLStreamReader xml, Deque<String> open, ProgramFile file)
            throws KnxProjectException {
        if (!inStatic(open, "ComObjectTable") && !inStatic(open, "ComObjects")) {
            return;
        }
        file.comObjects.seen();
        String id = XmlInput.text(xml, "Id");
        if (file.comObjects.wants(id)) {
            file.comObjects.keep(id, new ComObjectEntry(number(xml), XmlInput.attribute(xml, "BaseNumber"),
                    XmlInput.attribute(xml, "Text"), XmlInput.attribute(xml, "FunctionText"),
                    XmlInput.attribute(xml, "ObjectSize"),
                    KnxDatapointType.parseList(XmlInput.attribute(xml, "DatapointType"), file.budget()), flags(xml)));
            file.wantTranslations(id);
        }
    }

    private static void comObjectRef(XMLStreamReader xml, Deque<String> open, ProgramFile file)
            throws KnxProjectException {
        if (!inStatic(open, "ComObjectRefs")) {
            return;
        }
        file.comObjectRefs.seen();
        String id = XmlInput.text(xml, "Id");
        if (file.comObjectRefs.wants(id)) {
            String refId = XmlInput.text(xml, "RefId");
            file.comObjectRefs.keep(id,
                    new ComObjectRefEntry(refId, XmlInput.attribute(xml, "Text"),
                            XmlInput.attribute(xml, "FunctionText"), XmlInput.attribute(xml, "ObjectSize"),
                            KnxDatapointType.parseList(XmlInput.attribute(xml, "DatapointType"), file.budget()),
                            XmlInput.attribute(xml, "TextParameterRefId"), flags(xml)));
            if (!refId.isEmpty()) {
                file.comObjects.want(refId);
            }
            String textParameter = XmlInput.attribute(xml, "TextParameterRefId");
            if (textParameter != null && !textParameter.isEmpty()) {
                file.parameterRefs.want(textParameter);
            }
            file.wantTranslations(id);
        }
    }

    private static void parameterRef(XMLStreamReader xml, Deque<String> open, ProgramFile file)
            throws KnxProjectException {
        if (!inStatic(open, "ParameterRefs")) {
            return;
        }
        file.parameterRefs.seen();
        String id = XmlInput.text(xml, "Id");
        if (file.parameterRefs.wants(id)) {
            String refId = XmlInput.text(xml, "RefId");
            file.parameterRefs.keep(id, new ParameterRefEntry(refId, XmlInput.attribute(xml, "Value")));
            if (!refId.isEmpty()) {
                file.parameters.want(refId);
            }
        }
    }

    /** A parameter of {@code Static/Parameters}, also inside a {@code Union} there. */
    private static void parameter(XMLStreamReader xml, Deque<String> open, ProgramFile file)
            throws KnxProjectException {
        if (!inStatic(open, "Parameters") && !inUnion(open)) {
            return;
        }
        file.parameters.seen();
        String id = XmlInput.text(xml, "Id");
        if (file.parameters.wants(id)) {
            file.parameters.keep(id, new ParameterEntry(XmlInput.attribute(xml, "Value")));
        }
    }

    private static boolean inUnion(Deque<String> open) {
        Iterator<String> ancestors = open.iterator();
        return ancestors.hasNext() && "Union".equals(ancestors.next()) && ancestors.hasNext()
                && "Parameters".equals(ancestors.next()) && ancestors.hasNext() && "Static".equals(ancestors.next());
    }

    private static void allocator(XMLStreamReader xml, Deque<String> open, ProgramFile file)
            throws KnxProjectException {
        if (!inStatic(open, "Allocators")) {
            return;
        }
        file.allocators.seen();
        String id = XmlInput.text(xml, "Id");
        if (file.allocators.wants(id)) {
            file.allocators.keep(id, new AllocatorEntry(wholeNumber(XmlInput.attribute(xml, "Start"))));
        }
    }

    /** An argument of a module definition, {@code ModuleDef/Arguments/Argument}. */
    private static void argument(XMLStreamReader xml, Deque<String> open, ProgramFile file) throws KnxProjectException {
        Iterator<String> ancestors = open.iterator();
        if (!ancestors.hasNext() || !"Arguments".equals(ancestors.next()) || !ancestors.hasNext()
                || !"ModuleDef".equals(ancestors.next())) {
            return;
        }
        String id = XmlInput.text(xml, "Id");
        if (file.wantsArgument(id)) {
            file.arguments.keep(id,
                    new ArgumentEntry(XmlInput.text(xml, "Name"), XmlInput.attribute(xml, "Allocates")));
        }
    }

    private static void language(XMLStreamReader xml, Deque<String> open, ProgramFile file) {
        if ("Languages".equals(open.peek())) {
            file.startLanguage(XmlInput.text(xml, "Identifier"), open.size());
        }
    }

    private static void translationElement(XMLStreamReader xml, Deque<String> open, ProgramFile file) {
        Iterator<String> ancestors = open.iterator();
        if (ancestors.hasNext() && "TranslationUnit".equals(ancestors.next()) && ancestors.hasNext()
                && "Language".equals(ancestors.next())) {
            file.startTranslationElement(XmlInput.text(xml, "RefId"), open.size());
        }
    }

    private static void translation(XMLStreamReader xml, Deque<String> open, ProgramFile file)
            throws KnxProjectException {
        if ("TranslationElement".equals(open.peek())) {
            file.translation(XmlInput.attribute(xml, "AttributeName"), XmlInput.attribute(xml, "Text"), open.size());
        }
    }

    /** {@code null} when missing or not a whole number. */
    static @Nullable Long wholeNumber(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Whether the parent is {@code container} and the grandparent {@code Static}. */
    private static boolean inStatic(Deque<String> open, String container) {
        Iterator<String> ancestors = open.iterator();
        return ancestors.hasNext() && container.equals(ancestors.next()) && ancestors.hasNext()
                && "Static".equals(ancestors.next());
    }

    /** {@code null} when missing or not a whole number of at least 0; a program's mistake is not the project's. */
    private static @Nullable Integer number(XMLStreamReader xml) {
        String value = XmlInput.attribute(xml, "Number");
        if (value == null) {
            return null;
        }
        try {
            int number = Integer.parseInt(value.trim());
            return number >= 0 ? number : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static KnxObjectFlags flags(XMLStreamReader xml) {
        return new KnxObjectFlags(XmlInput.attribute(xml, "ReadFlag"), XmlInput.attribute(xml, "WriteFlag"),
                XmlInput.attribute(xml, "CommunicationFlag"), XmlInput.attribute(xml, "TransmitFlag"),
                XmlInput.attribute(xml, "UpdateFlag"), XmlInput.attribute(xml, "ReadOnInitFlag"));
    }
}
