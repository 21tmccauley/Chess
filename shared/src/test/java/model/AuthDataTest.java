package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthDataTest {

    @Test
    public void testConstructorAndAccessors() {
        String authToken = "TestToken123";
        String userName = "TestUserName123";

        AuthData authData = new AuthData(authToken, userName);

        assertEquals(authToken, authData.authToken());
        assertEquals(userName, authData.username());
    }
    @Test
    public void testEquality() {
        AuthData auth1 = new AuthData("token123", "user1");
        AuthData auth2 = new AuthData("token123", "user1");
        AuthData auth3 = new AuthData("differentToken", "user1");
        AuthData auth4 = new AuthData("token123", "differentUser");

        assertEquals(auth1, auth2);
        assertNotEquals(auth1, auth3);
        assertNotEquals(auth1, auth4);
    }

    @Test
    public void testHashCode() {
        AuthData auth1 = new AuthData("token123", "user1");
        AuthData auth2 = new AuthData("token123", "user1");

        assertEquals(auth1.hashCode(), auth2.hashCode());
    }

    @Test
    public void testToString() {
        AuthData authData = new AuthData("token123", "user1");
        String expected = "AuthData[authToken=token123, username=user1]";
        assertEquals(expected, authData.toString());
    }
}