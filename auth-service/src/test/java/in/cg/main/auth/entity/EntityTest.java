package in.cg.main.auth.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testUserApp() {
        UserApp user = new UserApp();
        user.setId(1L);
        user.setUsername("u");
        user.setPassword("p");
        user.setEmail("e");
        user.setName("n");
        user.setPhone("ph");
        user.setAddress("a");
        user.setRole("r");
        
        assertEquals(1L, user.getId());
        assertEquals("u", user.getUsername());
        assertEquals("p", user.getPassword());
        assertEquals("e", user.getEmail());
        assertEquals("n", user.getName());
        assertEquals("ph", user.getPhone());
        assertEquals("a", user.getAddress());
        assertEquals("r", user.getRole());
    }
}
