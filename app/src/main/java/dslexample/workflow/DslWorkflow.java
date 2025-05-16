package dslexample.workflow;

import dslexample.model.PaymentData;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;


@WorkflowInterface
public interface DslWorkflow {
    
    @WorkflowMethod
    void execute(String flowDefinitionJson);
    
    @SignalMethod
    void updatePaymentInformation(String paymentId);
    
    @QueryMethod
    PaymentData getCurrentPaymentData();
    
    @QueryMethod
    String getCurrentStep();
}
