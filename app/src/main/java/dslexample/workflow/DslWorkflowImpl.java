package dslexample.workflow;

import dslexample.activity.PaymentDataActivity;
import dslexample.model.FlowAction;
import dslexample.model.FlowDefinition;
import dslexample.model.PaymentData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DslWorkflowImpl implements DslWorkflow {
    private static final Logger logger = Workflow.getLogger(DslWorkflowImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Workflow state
    private PaymentData currentPaymentData;
    private String currentStep = "INITIALIZING";
    private final List<String> completedSteps = new ArrayList<>();
    private final AtomicBoolean paymentUpdateReceived = new AtomicBoolean(false);
    
    // Activity client
    private final PaymentDataActivity paymentDataActivity;
    
    public DslWorkflowImpl() {
        ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(RetryOptions.newBuilder()
                .setMaximumAttempts(3)
                .build())
            .build();
        
        this.paymentDataActivity = Workflow.newActivityStub(PaymentDataActivity.class, options);
    }
    
    @Override
    public void execute(String flowDefinitionJson) {
        logger.info("Starting workflow execution with DSL");
        
        try {
            // Parse the flow definition
            FlowDefinition flowDefinition = objectMapper.readValue(flowDefinitionJson, FlowDefinition.class);
            logger.info("Executing flow: {}", flowDefinition.getName());
            
            // Process each action in sequence
            for (FlowAction action : flowDefinition.getActions()) {
                currentStep = action.getAction();
                logger.info("Starting action: {}", currentStep);
                
                // Configure activity options from DSL parameters
                ActivityOptions actionOptions = ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(action.getStartToCloseSec()))
                    .setRetryOptions(RetryOptions.newBuilder()
                        .setMaximumAttempts(action.getRetries())
                        .build())
                    .build();
                
                // Create a typed activity stub with our options
                PaymentDataActivity actionActivity = 
                    Workflow.newActivityStub(PaymentDataActivity.class, actionOptions);
                
                // Check if we need to wait for a payment update signal before proceeding
                // Only check after the first action is complete
                if (!completedSteps.isEmpty() && !paymentUpdateReceived.get()) {
                    logger.info("Waiting for payment update signal before proceeding to step: {}", 
                        currentStep);
                    
                    // This will block the workflow until a signal is received
                    Workflow.await(() -> paymentUpdateReceived.get());
                    
                    logger.info("Payment update signal received, proceeding with step: {}", 
                        currentStep);
                }
                
                // Simulate workflow step execution
                Workflow.sleep(Duration.ofSeconds(1));
                logger.info("Action completed: {}", currentStep);
                completedSteps.add(currentStep);
            }
            
            logger.info("Workflow execution completed successfully");
            
        } catch (IOException e) {
            logger.error("Failed to parse flow definition: {}", e.getMessage());
            throw Workflow.wrap(new RuntimeException("Invalid flow definition JSON", e));
        }
    }
    
    @Override
    public void updatePaymentInformation(String paymentId) {
        logger.info("Signal received: updatePaymentInformation for ID: {}", paymentId);
        
        // Asynchronously execute the activity to fetch payment data
        Promise<PaymentData> paymentDataPromise = Async.function(
            paymentDataActivity::fetchPaymentData, paymentId);
        
        // When the activity completes, update the workflow state
        paymentDataPromise.thenApply(paymentData -> {
            this.currentPaymentData = paymentData;
            this.paymentUpdateReceived.set(true);
            logger.info("Payment data updated: {}", paymentData);
            return paymentData;
        });
    }
    
    @Override
    public PaymentData getCurrentPaymentData() {
        return currentPaymentData;
    }
    
    @Override
    public String getCurrentStep() {
        return currentStep;
    }
}