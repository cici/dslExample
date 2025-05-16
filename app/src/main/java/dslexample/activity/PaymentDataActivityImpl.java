package dslexample.activity;

import dslexample.model.PaymentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentDataActivityImpl implements PaymentDataActivity {
    private static final Logger logger = LoggerFactory.getLogger(PaymentDataActivityImpl.class);
    
    @Override
    public PaymentData fetchPaymentData(String paymentId) {
        logger.info("Fetching payment data for ID: {}", paymentId);
        
        // In a real implementation, this would call an external service
        // This is just a placeholder implementation
        
        // Simulate network delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Return mock payment data
        return new PaymentData(
            paymentId,
            1250.75,
            "USD",
            "PROCESSED"
        );
    }
}
