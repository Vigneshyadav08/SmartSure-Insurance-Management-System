package in.cg.main.admin.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        Exception ex = new Exception("Test Exception");
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Object message = response.getBody().get("message");
        assertNotNull(message);
        assertTrue(message.toString().contains("Test Exception"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}
