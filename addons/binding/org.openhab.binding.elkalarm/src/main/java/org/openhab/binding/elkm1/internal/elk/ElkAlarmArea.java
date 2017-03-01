package org.openhab.binding.elkm1.internal.elk;

/**
 * Details about an elk alarm area.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class ElkAlarmArea {
    public ElkAlarmStatus getStatus() {
        return status;
    }

    public void setStatus(ElkAlarmStatus status) {
        this.status = status;
    }

    public ElkAlarmReadyStatus getReadyStatus() {
        return readyStatus;
    }

    public void setReadyStatus(ElkAlarmReadyStatus readyStatus) {
        this.readyStatus = readyStatus;
    }

    public boolean isAlarmActive() {
        return active != ElkAlarmActive.NoAlarmActive && !isEntranceDelayActive() && !isAlarmAbortDelayActive();
    }

    public boolean isEntranceDelayActive() {
        return active == ElkAlarmActive.EntranceDelayActive;
    }

    public boolean isAlarmAbortDelayActive() {
        return active == ElkAlarmActive.AlarmAbortDelayActive;
    }

    public ElkAlarmActive getActive() {
        return active;
    }

    public void setActive(ElkAlarmActive active) {
        this.active = active;
    }

    private ElkAlarmStatus status;
    private ElkAlarmReadyStatus readyStatus;
    private ElkAlarmActive active;
}
