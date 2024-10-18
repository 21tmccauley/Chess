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
}