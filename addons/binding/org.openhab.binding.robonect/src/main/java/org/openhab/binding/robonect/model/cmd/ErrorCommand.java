package org.openhab.binding.robonect.model.cmd;

public class ErrorCommand implements Command {
    
    private boolean reset = false;
    
    public ErrorCommand withReset(boolean reset){
        this.reset = reset;
        return this;
    }
    
    @Override
    public String toCommandURL(String baseURL) {
        if(reset){
            return baseURL + "?cmd=error&reset";
        }else {
            return baseURL + "?cmd=error";
        }
    }
}
