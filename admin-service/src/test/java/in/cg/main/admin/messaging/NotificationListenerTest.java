package in.cg.main.admin.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @InjectMocks
    private NotificationListener notificationListener;

    @Test
    void receivePolicyEvent_shouldLogMessage() {
        assertDoesNotThrow(() -> notificationListener.receivePolicyEvent("Test Policy Message"));
    }

    @Test
    void receiveClaimEvent_shouldLogMessage() {
        assertDoesNotThrow(() -> notificationListener.receiveClaimEvent("Test Claim Message"));
    }
}
