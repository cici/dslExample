package dslexample;

import dslexample.activity.PaymentDataActivityImpl;
import dslexample.model.PaymentData;
import dslexample.workflow.DslWorkflow;
import dslexample.workflow.DslWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class DslWorkflowApplication {
    private static final Logger logger = LoggerFactory.getLogger(DslWorkflowApplication.class);
    private static final String TASK_QUEUE = "DSL_WORKFLOW_TASK_QUEUE";
    
    public static void main(String[] args) {
        // Set up Temporal service stubs
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        
        // Create worker factory and worker
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);
        
        // Register workflow and activity implementations
        worker.registerWorkflowImplementationTypes(DslWorkflowImpl.class);
        worker.registerActivitiesImplementations(new PaymentDataActivityImpl());
        
        // Start the worker
        factory.start();
        logger.info("Worker started");
        
        try {
            // Read the flow definition from file
            String flowJson = Files.readString(Paths.get(
                DslWorkflowApplication.class.getClassLoader().getResource("sampleflow.json").toURI()));
            
            // Generate a unique workflow ID
            String workflowId = "dsl-workflow-" + UUID.randomUUID();
            
            // Configure workflow options
            WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(TASK_QUEUE)
                .setWorkflowId(workflowId)
                .build();
            
            // Create the workflow stub and start execution
            DslWorkflow workflow = client.newWorkflowStub(DslWorkflow.class, options);
            WorkflowClient.start(workflow::execute, flowJson);
            logger.info("Workflow started with ID: {}", workflowId);
            
            // Create a workflow stub for querying and signaling
            DslWorkflow workflowStub = client.newWorkflowStub(DslWorkflow.class, workflowId);
            
            // Allow the workflow to complete the first action 
            Thread.sleep(2000);
            logger.info("Current step: {}", workflowStub.getCurrentStep());
            
            // The workflow will now be blocked waiting for our signal
            logger.info("Sending payment update signal to workflow...");
            String paymentId = "PMT-" + UUID.randomUUID().toString().substring(0, 8);
            workflowStub.updatePaymentInformation(paymentId);
            
            // Wait for the signal to be processed
            Thread.sleep(2000);
            
            // Query the current payment data
            PaymentData paymentData = workflowStub.getCurrentPaymentData();
            logger.info("Payment data after signal: {}", paymentData);
            
            // Wait for the workflow to complete
            logger.info("Waiting for workflow to complete...");
            Thread.sleep(5000);
            
            // Final status check
            try {
                String finalStep = workflowStub.getCurrentStep();
                logger.info("Final workflow step: {}", finalStep);
            } catch (Exception e) {
                logger.info("Workflow completed");
            }
            
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage(), e);
        } finally {
            System.exit(0);
        }
    }
}