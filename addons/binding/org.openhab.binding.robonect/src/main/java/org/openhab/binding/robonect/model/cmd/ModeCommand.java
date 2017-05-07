package org.openhab.binding.robonect.model.cmd;

public class ModeCommand implements Command {

    public enum Mode {
        HOME(1),
        EOD(2),
        MAN(3),
        AUTO(4),
        JOB(5);

        int code;

        Mode(int code) {
            this.code = code;
        }
    }

    public enum RemoteStart {

        STANDARD(0),
        REMOTE_1(1),
        REMOTE_2(2);

        int code;

        RemoteStart(int code) {
            this.code = code;
        }

    }

    private Mode mode;

    private RemoteStart remoteStart;

    private Mode after;

    private String start;

    private String end;

    private Integer duration;

    public ModeCommand(Mode mode) {
        this.mode = mode;
    }

    public ModeCommand withRemoteStart(RemoteStart remoteStart) {
        this.remoteStart = remoteStart;
        return this;
    }

    public ModeCommand withAfter(Mode afterMode) {
        this.after = afterMode;
        return this;
    }

    public ModeCommand withStart(String startTime) {
        this.start = startTime;
        return this;
    }

    public ModeCommand withEnd(String endTime) {
        this.end = endTime;
        return this;
    }

    public ModeCommand withDuration(Integer durationInMinutes) {
        this.duration = durationInMinutes;
        return this;
    }

    @Override
    public String toCommandURL(String baseURL) {
        StringBuilder sb = new StringBuilder(baseURL);
        sb.append("?cmd=mode&&mode=");
        sb.append(mode.name().toLowerCase());
        switch (mode){
            case EOD:
            case MAN:
            case AUTO:
            case HOME:
                break;
            case JOB:
                if(remoteStart != null){
                    sb.append("&remotestart=");
                    sb.append(remoteStart.code);
                }
                if(after != null){
                    sb.append("&after=");
                    sb.append(after.code);
                }
                if(start != null){
                    sb.append("&start=");
                    sb.append(start);
                }
                if(end != null){
                    sb.append("&end=");
                    sb.append(end);
                }
                if(duration != null){
                    sb.append("&duration=");
                    sb.append(duration);
                }
                break;
        }
        return sb.toString();
    }

}
