/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vera2.controller.json;

import java.util.List;

/**
 * @author Dmitriy Ponomarev
 */
public class Sdata {
    public String full;
    public String version;
    public String model;
    public String zwave_heal;
    public String temperature;
    public String serial_number;
    public String fwd1;
    public String fwd2;
    public String ir;
    public String irtx;
    public String loadtime;
    public String dataversion;
    public String state;
    public String comment;
    public List<Section> sections;
    public List<Room> rooms;
    public List<Scene> scenes;
    public List<Device> devices;
    public List<Categorie> categories;
}
