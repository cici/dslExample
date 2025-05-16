package dslexample.activity;

import dslexample.model.PaymentData;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface PaymentDataActivity {
    
    @ActivityMethod
    PaymentData fetchPaymentData(String paymentId);
}
