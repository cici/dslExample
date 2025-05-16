package dslexample.model;

public class FlowAction {
    private String action;
    private int retries;
    private int startToCloseSec;

    // Getters and setters
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public int getRetries() { return retries; }
    public void setRetries(int retries) { this.retries = retries; }
    
    public int getStartToCloseSec() { return startToCloseSec; }
    public void setStartToCloseSec(int startToCloseSec) { this.startToCloseSec = startToCloseSec; }
}
