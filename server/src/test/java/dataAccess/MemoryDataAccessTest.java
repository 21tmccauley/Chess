package dataAccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoryDataAccessTest {
    private DataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
    }

    @Test
    void createUserAndGetUser() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        dataAccess.createUser(user);

        UserData retrievedUser = dataAccess.getUser("testUser");
        assertEquals(user, retrievedUser);
    }

    // Other test methods...
}