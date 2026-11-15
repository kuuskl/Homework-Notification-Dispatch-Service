package com.example.homework.notification;

import com.example.homework.notification.controller.NotificationController;
import com.example.homework.notification.controller.NotificationResponseV1;
import com.example.homework.notification.controller.RequestV1;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@SpringBootTest(classes = NotificationDispatch.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class NotificationControllerIntegrationTest {

    @Autowired
    private NotificationController notificationController;



    @Test
    public void emailSuccessIsStoredAsSent() throws Exception {
        RequestV1 requestV1 = new RequestV1();
        requestV1.type = "EMAIL";
        requestV1.target = "buyer@example.com";
        requestV1.payload = "hello";
        NotificationResponseV1 response = notificationController.send(requestV1);
        assertThat(response.status).isEqualTo("S");
        assertThat(response.dispatchCount).isEqualTo(1);
        assertThat(response.failureCount).isZero();
        assertThat(notificationController.history()).hasSize(1);
    }

    @Test
    public void blockedTargetFailsBeforeDispatchIsInserted() {
        RequestV1 requestV1 = new RequestV1();
        requestV1.type = "SMS";
        requestV1.target = "blocked-user@example.com";
        requestV1.payload = "hello";
        try {
            notificationController.send(requestV1);
            fail("Expected blocked target to fail");
        } catch (ResponseStatusException ex) {
            assertThat(ex.getMessage()).isEqualTo("400 BAD_REQUEST \"Business Rule Violation: User is blacklisted or rate-limited by DB policy.\"");
        } catch (Exception ex) {
            fail("Expected ResponseStatusException but got: " + ex.getClass().getName());
        }

        assertThat(notificationController.history()).isEmpty();
    }

    @Test
    public void unsupportedTypeIsStoredAsUnknown() {
        RequestV1 requestV1 = new RequestV1();
        requestV1.type = "FAX";
        requestV1.target = "ops@example.com";
        requestV1.payload = "hello";
        try {
            notificationController.send(requestV1);
            fail("Expected unsupported type to fail");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("400 BAD_REQUEST \"Unsupported type: FAX\"");
        }

        NotificationResponseV1 latest = notificationController.latest();
        assertThat(latest.status).isEqualTo("U");
        assertThat(latest.message).isEqualTo("Unsupported type: FAX");
    }

    @Test
    public void threeFailuresOpenCircuitBreakerForNextAttempt() {
        RequestV1 requestV1 = new RequestV1();
        requestV1.type = "EMAIL";
        requestV1.target = "error-user@example.com";
        requestV1.payload = "hello";
        for (int i = 0; i < 3; i++) {
            try {
                notificationController.send(requestV1);
                fail("Expected email failure");
            } catch (RuntimeException ex) {
                assertThat(ex.getMessage()).isEqualTo("400 BAD_REQUEST \"Inserted failure record for error-user@example.com: SMTP Timeout\"");
            } catch (Exception ex) {
                fail("Expected RuntimeException but got: " + ex.getClass().getName());
            }
        }

        try {
            requestV1.target = "ok-user@example.com";
            notificationController.send(requestV1);
            fail("Expected circuit breaker to reject the request");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo( "400 BAD_REQUEST \"Circuit breaker OPEN via database state.\"");
        } catch (Exception ex) {
            fail("Expected RuntimeException but got: " + ex.getClass().getName());
        }

        NotificationResponseV1 latest = notificationController.latest();
        assertThat(latest.status).isEqualTo("R");
        assertThat(latest.failureCount).isEqualTo(3);
        assertThat(latest.dispatchCount).isEqualTo(4);
    }
}