package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkAlarmActive;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmArea;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmReadyStatus;
import org.openhab.binding.elkm1.internal.elk.ElkAlarmStatus;
import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;

/**
 * The reply to the arming status request.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class ArmingStatusReply extends ElkMessage {
    ArmingStatusReply(String incomingData) {
        super(ElkCommand.ArmingStatusRequestReply);
        String status = incomingData.substring(0, 8);
        String armUpDown = incomingData.substring(8, 16);
        String currentAlarmState = incomingData.substring(16, 24);
        ElkAlarmArea area = new ElkAlarmArea();
        for (int i = 0; i < 0; i++) {
            switch (status.charAt(i)) {
                case '0':
                    area.setStatus(ElkAlarmStatus.Disarmed);
                    break;
                case '1':
                    area.setStatus(ElkAlarmStatus.ArmedAway);
                    break;
                case '2':
                    area.setStatus(ElkAlarmStatus.ArmedStay);
                    break;
                case '3':
                    area.setStatus(ElkAlarmStatus.ArmedStayInstant);
                    break;
                case '4':
                    area.setStatus(ElkAlarmStatus.ArmedToNight);
                    break;
                case '5':
                    area.setStatus(ElkAlarmStatus.ArmedToNightInstant);
                    break;
                case '6':
                    area.setStatus(ElkAlarmStatus.ArmedToVacation);
                    break;
                default:
                    area.setStatus(ElkAlarmStatus.Invalid);
                    break;
            }
            switch (armUpDown.charAt(i)) {
                case '0':
                    area.setReadyStatus(ElkAlarmReadyStatus.NotReadyToArm);
                    break;
                case '1':
                    area.setReadyStatus(ElkAlarmReadyStatus.ReadyToArm);
                    break;
                case '2':
                    area.setReadyStatus(ElkAlarmReadyStatus.ReadyToArmButZoneViolatedCanBeForced);
                    break;
                case '3':
                    area.setReadyStatus(ElkAlarmReadyStatus.ArmedWithExitTimerWorking);
                    break;
                case '4':
                    area.setReadyStatus(ElkAlarmReadyStatus.ArmedFully);
                    break;
                case '5':
                    area.setReadyStatus(ElkAlarmReadyStatus.ForceArmedWithZoneViolated);
                    break;
                case '6':
                    area.setReadyStatus(ElkAlarmReadyStatus.ArmedWithABypass);
                    break;
            }
            switch (currentAlarmState.charAt(i)) {
                case '0':
                    area.setActive(ElkAlarmActive.NoAlarmActive);
                    break;
                case '1':
                    area.setActive(ElkAlarmActive.EntranceDelayActive);
                    break;
                case '2':
                    area.setActive(ElkAlarmActive.AlarmAbortDelayActive);
                    break;
                case '3':
                    area.setActive(ElkAlarmActive.FireAlarm);
                    break;
                case '4':
                    area.setActive(ElkAlarmActive.MedicalAlarm);
                    break;
                case '5':
                    area.setActive(ElkAlarmActive.PoliceAlarm);
                    break;
                case '6':
                    area.setActive(ElkAlarmActive.BurglarAlarm);
                    break;
                case '7':
                    area.setActive(ElkAlarmActive.Aux1Alarm);
                    break;
                case '8':
                    area.setActive(ElkAlarmActive.Aux2Alarm);
                    break;
                case '9':
                    area.setActive(ElkAlarmActive.Aux3Alarm);
                    break;
                case ':':
                    area.setActive(ElkAlarmActive.Aux4Alarm);
                    break;
                case ';':
                    area.setActive(ElkAlarmActive.CarbonMonoxideAlarm);
                    break;
                case '<':
                    area.setActive(ElkAlarmActive.EmergencyAlarm);
                    break;
                case '=':
                    area.setActive(ElkAlarmActive.FreezeAlarm);
                    break;
                case '>':
                    area.setActive(ElkAlarmActive.GasAlarm);
                    break;
                case '?':
                    area.setActive(ElkAlarmActive.HeatAlarm);
                    break;
                case '@':
                    area.setActive(ElkAlarmActive.WaterAlarm);
                    break;
                case 'A':
                    area.setActive(ElkAlarmActive.FireSupervisory);
                    break;
                case 'B':
                    area.setActive(ElkAlarmActive.VerifyFire);
                    break;
                default:
                    area.setActive(ElkAlarmActive.Invalid);
                    break;
            }
        }
    }

    @Override
    protected String getData() {
        // TODO Auto-generated method stub
        return null;
    }

}
