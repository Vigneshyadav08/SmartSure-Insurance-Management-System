package in.cg.main.auth.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void testAuthRequest() {
        AuthRequest req = new AuthRequest();
        req.setUsername("u"); req.setPassword("p");
        assertEquals("u", req.getUsername());
        assertEquals("p", req.getPassword());
    }

    @Test
    void testAuthResponse() {
        AuthResponse res = new AuthResponse("t", "CUSTOMER");
        assertEquals("t", res.getToken());
        assertEquals("CUSTOMER", res.getRole());
        res.setToken("t2");
        res.setRole("ADMIN");
        assertEquals("t2", res.getToken());
        assertEquals("ADMIN", res.getRole());
    }

    @Test
    void testAdminRequest() {
        AdminRequest req = new AdminRequest();
        req.setUsername("u"); req.setPassword("p"); req.setEmail("e"); 
        req.setName("n"); req.setPhone("ph"); req.setAddress("a"); req.setRole("r");
        assertEquals("u", req.getUsername());
        assertEquals("p", req.getPassword());
        assertEquals("e", req.getEmail());
        assertEquals("n", req.getName());
        assertEquals("ph", req.getPhone());
        assertEquals("a", req.getAddress());
        assertEquals("r", req.getRole());
    }

    @Test
    void testUserRegistrationRequest() {
        UserRegistrationRequest req = new UserRegistrationRequest();
        req.setUsername("u"); req.setPassword("p"); req.setEmail("e");
        req.setName("n"); req.setPhone("ph"); req.setAddress("a");
        assertEquals("u", req.getUsername());
        assertEquals("p", req.getPassword());
        assertEquals("e", req.getEmail());
        assertEquals("n", req.getName());
        assertEquals("ph", req.getPhone());
        assertEquals("a", req.getAddress());
    }
}
