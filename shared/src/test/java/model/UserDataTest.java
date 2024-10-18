package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserDataTest {

    @Test
    public void testConstructorAndAccessors() {
        String username = "testUser";
        String password = "password123";
        String email = "test@example.com";

        UserData userData = new UserData(username, password, email);

        assertEquals(username, userData.username());
        assertEquals(password, userData.password());
        assertEquals(email, userData.email());
    }

    @Test
    public void testEquality() {
        UserData user1 = new UserData("user1", "pass1", "user1@example.com");
        UserData user2 = new UserData("user1", "pass1", "user1@example.com");
        UserData user3 = new UserData("user2", "pass1", "user1@example.com");
        UserData user4 = new UserData("user1", "pass2", "user1@example.com");
        UserData user5 = new UserData("user1", "pass1", "user2@example.com");

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertNotEquals(user1, user4);
        assertNotEquals(user1, user5);
    }

    @Test
    public void testHashCode() {
        UserData user1 = new UserData("user1", "pass1", "user1@example.com");
        UserData user2 = new UserData("user1", "pass1", "user1@example.com");

        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testToString() {
        UserData userData = new UserData("user1", "pass1", "user1@example.com");
        String expected = "UserData[username=user1, password=pass1, email=user1@example.com]";
        assertEquals(expected, userData.toString());
    }

    @Test
    public void testNullValues() {
        assertDoesNotThrow(() -> new UserData(null, null, null));
        UserData userData = new UserData(null, null, null);
        assertNull(userData.username());
        assertNull(userData.password());
        assertNull(userData.email());
    }

    @Test
    public void testEmptyValues() {
        UserData userData = new UserData("", "", "");
        assertEquals("", userData.username());
        assertEquals("", userData.password());
        assertEquals("", userData.email());
    }
}