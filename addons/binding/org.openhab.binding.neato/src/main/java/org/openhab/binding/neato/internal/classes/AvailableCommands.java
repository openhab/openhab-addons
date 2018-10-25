/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

/**
 * The {@link AvailableCommands} is responsible for listing all available commands.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class AvailableCommands {

    private Boolean start;
    private Boolean stop;
    private Boolean pause;
    private Boolean resume;
    private Boolean goToBase;

    public Boolean getStart() {
        return start;
    }

    public void setStart(Boolean start) {
        this.start = start;
    }

    public Boolean getStop() {
        return stop;
    }

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    public Boolean getPause() {
        return pause;
    }

    public void setPause(Boolean pause) {
        this.pause = pause;
    }

    public Boolean getResume() {
        return resume;
    }

    public void setResume(Boolean resume) {
        this.resume = resume;
    }

    public Boolean getGoToBase() {
        return goToBase;
    }

    public void setGoToBase(Boolean goToBase) {
        this.goToBase = goToBase;
    }

}
