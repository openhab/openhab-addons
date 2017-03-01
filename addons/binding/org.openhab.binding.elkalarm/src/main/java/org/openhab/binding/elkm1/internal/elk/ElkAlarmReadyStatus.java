package org.openhab.binding.elkm1.internal.elk;

public enum ElkAlarmReadyStatus {
    NotReadyToArm,
    ReadyToArm,
    ReadyToArmButZoneViolatedCanBeForced,
    ArmedWithExitTimerWorking,
    ArmedFully,
    ForceArmedWithZoneViolated,
    ArmedWithABypass
}
