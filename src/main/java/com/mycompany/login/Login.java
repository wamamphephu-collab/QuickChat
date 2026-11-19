package com.mycompany.login;

public class Login {

    private String username;
    private String password;
    private String cellPhoneNumber;
    private String firstName;
    private String lastName;

    public Login(String username, String password, String cellPhoneNumber,
                 String firstName, String lastName) {

        this.username = username;
        this.password = password;
        this.cellPhoneNumber = cellPhoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public boolean checkUserName() {

        return username.contains("_") && username.length() <= 5;
    }

    public boolean checkPasswordComplexity() {

        if (password.length() < 8) {
            return false;
        }

        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {

            char character = password.charAt(i);

            if (Character.isUpperCase(character)) {
                hasCapital = true;
            }

            if (Character.isDigit(character)) {
                hasNumber = true;
            }

            if (!Character.isLetterOrDigit(character)) {
                hasSpecial = true;
            }
        }

        return hasCapital && hasNumber && hasSpecial;
    }

    public  String checkCellPhoneNumber() {

         return cellPhoneNumber;
    }

    public String registerUser() {

        if (!checkUserName()) {
            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        }

        if (!checkPasswordComplexity()) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }

        if (checkCellPhoneNumber() == null || checkCellPhoneNumber().isBlank()) {
            return "Cell phone number is incorrectly formatted or does not contain an international code.";
        }

        return "User has been registered successfully.";
    }

    public boolean loginUser(String enteredUsername, String enteredPassword) {

        return enteredUsername.equals(username)
                && enteredPassword.equals(password);
    }

    public String returnLoginStatus(boolean loginStatus) {

        if (loginStatus) {
            return "Welcome " + firstName + " " + lastName
                    + ", it is great to see you again.";
        } else {
            return "Username or password incorrect, please try again.";
        }
    }
}