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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CourseDefinition}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class CourseDefinition {
    private String courseName = "";
    // Name of the course this is based on. It's only used for SmartCourses
    private String baseCourseName = "";
    private CourseType courseType = CourseType.UNDEF;
    private List<CourseFunction> functions = new ArrayList<>();

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getBaseCourseName() {
        return baseCourseName;
    }

    public void setBaseCourseName(String baseCourseName) {
        this.baseCourseName = baseCourseName;
    }

    public CourseType getCourseType() {
        return courseType;
    }

    public void setCourseType(CourseType courseType) {
        this.courseType = courseType;
    }

    public List<CourseFunction> getFunctions() {
        return functions;
    }

    public void setFunctions(List<CourseFunction> functions) {
        this.functions = functions;
    }
}
