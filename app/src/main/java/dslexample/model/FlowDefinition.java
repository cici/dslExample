package dslexample.model;

import java.util.List;

public class FlowDefinition {
    private String id;
    private String name;
    private String description;
    private List<FlowAction> actions;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<FlowAction> getActions() { return actions; }
    public void setActions(List<FlowAction> actions) { this.actions = actions; }
}
