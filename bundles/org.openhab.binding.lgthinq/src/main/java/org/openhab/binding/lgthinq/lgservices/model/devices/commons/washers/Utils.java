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
package org.openhab.binding.lgthinq.lgservices.model.devices.commons.washers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * The {@link Utils} class defines common methods to handle generic washer devices
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class Utils {

    public static Map<String, CourseDefinition> getGenericCourseDefinitions(JsonNode courseNode, CourseType type,
            String notSelectedCourseKey) {
        Map<String, CourseDefinition> coursesDef = new HashMap<>();
        courseNode.fields().forEachRemaining(e -> {
            CourseDefinition cd = new CourseDefinition();
            JsonNode thisCourseNode = e.getValue();
            cd.setCourseName(thisCourseNode.path("_comment").textValue());
            if (CourseType.SMART_COURSE.equals(type)) {
                cd.setBaseCourseName(thisCourseNode.path("Course").textValue());
            }
            cd.setCourseType(type);
            if (thisCourseNode.path("function").isArray()) {
                // just to be safe here
                ArrayNode functions = (ArrayNode) thisCourseNode.path("function");
                List<CourseFunction> functionList = cd.getFunctions();
                for (JsonNode fNode : functions) {
                    // map all course functions here
                    CourseFunction f = new CourseFunction();
                    f.setValue(fNode.path("value").textValue());
                    f.setDefaultValue(fNode.path("default").textValue());
                    JsonNode selectableNode = fNode.path("selectable");
                    // only Courses (not SmartCourses or DownloadedCourses) can have selectable functions
                    f.setSelectable(
                            !selectableNode.isMissingNode() && selectableNode.isArray() && (type == CourseType.COURSE));
                    if (f.isSelectable()) {
                        List<String> selectableValues = f.getSelectableValues();
                        // map values acceptable for this function
                        for (JsonNode v : selectableNode) {
                            if (v.isValueNode()) {
                                selectableValues.add(v.textValue());
                            }
                        }
                        f.setSelectableValues(selectableValues);
                    }
                    functionList.add(f);
                }
                cd.setFunctions(functionList);
            }
            coursesDef.put(e.getKey(), cd);
        });
        CourseDefinition cdNotSelected = new CourseDefinition();
        cdNotSelected.setCourseType(type);
        cdNotSelected.setCourseName("Not Selected");
        coursesDef.put(notSelectedCourseKey, cdNotSelected);
        return coursesDef;
    }
}
