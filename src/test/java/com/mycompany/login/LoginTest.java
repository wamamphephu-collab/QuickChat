package com.mycompany.login;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    Login validUser = new Login(
            "kyl_1",
            "Ch&&sec@ke99!",
            "0838969876",
            "Kyle",
            "Smith"
    );

    @Test
    public void testUsernameCorrectlyFormatted() {
        assertTrue(validUser.checkUserName());
    }

    @Test
    public void testUsernameIncorrectlyFormatted() {
        Login user = new Login(
                "kyle!!!!!",
                "Ch&&sec@ke99!",
                "0838969876",
                "Kyle",
                "Smith"
        );

        assertFalse(user.checkUserName());
    }

    @Test
    public void testPasswordMeetsComplexity() {
        assertTrue(validUser.checkPasswordComplexity());
    }

    @Test
    public void testPasswordDoesNotMeetComplexity() {
        Login user = new Login(
                "kyl_1",
                "password",
                "0838969876",
                "Kyle",
                "Smith"
        );

        assertFalse(user.checkPasswordComplexity());
    }

    @Test
    public void testRegisterUserSuccessful() {
        assertEquals(
                "User has been registered successfully.",
                validUser.registerUser()
        );
    }

    @Test
    public void testLoginSuccessful() {
        boolean result = validUser.loginUser(
                "kyl_1",
                "Ch&&sec@ke99!"
        );

        assertTrue(result);

        assertEquals(
                "Welcome Kyle Smith, it is great to see you again.",
                validUser.returnLoginStatus(true)
        );
    }

    @Test
    public void testLoginFailed() {
        boolean result = validUser.loginUser(
                "kyl_1",
                "wrongpass"
        );

        assertFalse(result);

        assertEquals(
                "Username or password incorrect, please try again.",
                validUser.returnLoginStatus(false)
        );
    }

    @Test
    public void testLoginWithWrongUsername() {
        boolean result = validUser.loginUser(
                "wrong_user",
                "Ch&&sec@ke99!"
        );

        assertFalse(result);
    }
}

