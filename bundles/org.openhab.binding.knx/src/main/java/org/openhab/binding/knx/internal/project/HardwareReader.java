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

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.project.KnxProjectException.Reason;

/**
 * Reads the {@code Hardware.xml} files of the manufacturers: the products and the {@code Hardware2Program}
 * entries the devices of the project name, nothing else. The {@code Hardware} elements themselves, with their
 * serial numbers, are not read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class HardwareReader {

    private final Set<String> wantedHardware2Programs;
    private final Set<String> wantedProducts;
    private final Map<String, String> programByHardware2Program = new HashMap<>();
    private final Map<String, KnxProduct> products = new HashMap<>();

    /**
     * @param wantedHardware2Programs the {@code Hardware2Program@Id} values of the devices
     * @param wantedProducts the {@code Product@Id} values of the devices
     */
    HardwareReader(Set<String> wantedHardware2Programs, Set<String> wantedProducts) {
        this.wantedHardware2Programs = wantedHardware2Programs;
        this.wantedProducts = wantedProducts;
    }

    /** Reads one file; of several entries with the same id the first one read counts. */
    void read(InputStream in, String file, ReadBudget budget) throws KnxProjectException {
        XMLStreamReader xml = null;
        try {
            xml = XmlInput.open(in, file, budget);
            Deque<String> open = new ArrayDeque<>();
            String hardware2Program = null;
            while (xml.hasNext()) {
                int event = XmlInput.next(xml, file);
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String element = xml.getLocalName();
                    String parent = open.peek();
                    if (open.isEmpty() && !"KNX".equals(element)) {
                        throw new KnxProjectException(Reason.MALFORMED, file + " is not a KNX manufacturer file");
                    }
                    if ("Hardware2Program".equals(element) && "Hardware2Programs".equals(parent)) {
                        String id = XmlInput.text(xml, "Id");
                        hardware2Program = wantedHardware2Programs.contains(id)
                                && !programByHardware2Program.containsKey(id) ? id : null;
                        if (hardware2Program != null) {
                            budget.countRecord();
                            programByHardware2Program.put(hardware2Program, "");
                        }
                    } else if ("ApplicationProgramRef".equals(element) && "Hardware2Program".equals(parent)
                            && hardware2Program != null && "".equals(programByHardware2Program.get(hardware2Program))) {
                        programByHardware2Program.put(hardware2Program, XmlInput.text(xml, "RefId"));
                    } else if ("Product".equals(element) && "Products".equals(parent)) {
                        String id = XmlInput.text(xml, "Id");
                        if (wantedProducts.contains(id) && !products.containsKey(id)) {
                            budget.countRecord();
                            products.put(id, new KnxProduct(id, XmlInput.attribute(xml, "Text"),
                                    XmlInput.attribute(xml, "OrderNumber")));
                        }
                    }
                    open.push(element);
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("Hardware2Program".equals(open.pop())) {
                        hardware2Program = null;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw XmlInput.failure(e, file, budget);
        } finally {
            XmlInput.close(xml);
        }
    }

    /** Whether one of the files read holds this {@code Hardware2Program}. */
    boolean hasHardware2Program(String id) {
        return programByHardware2Program.containsKey(id);
    }

    /** The program the {@code Hardware2Program} names; {@code null} when it names none or is not known. */
    @Nullable
    String programOf(String hardware2ProgramId) {
        String program = programByHardware2Program.get(hardware2ProgramId);
        return program == null || program.isEmpty() ? null : program;
    }

    @Nullable
    KnxProduct product(String id) {
        return products.get(id);
    }
}
