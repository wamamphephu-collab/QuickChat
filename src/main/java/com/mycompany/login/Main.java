package com.mycompany.login;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("===== Registration =====");
            System.out.print("Enter your first name: ");
            String firstName = input.nextLine();
            System.out.print("Enter your last name: ");
            String lastName = input.nextLine();
            System.out.print("Enter username: ");
            String username = input.nextLine();
            System.out.print("Enter password: ");
            String password = input.nextLine();
            System.out.print("Enter South African cell phone number (+27 followed by number): ");
            String cellPhoneNumber = input.nextLine();

            Login user = new Login(username, password, cellPhoneNumber, firstName, lastName);
            String registration = user.registerUser();
            System.out.println(registration);
            if (!"User has been registered successfully.".equals(registration)) {
                System.out.println("Registration failed. Please correct the details and run the application again.");
                return;
            }

            System.out.println("\n===== Login =====");
            System.out.print("Enter username: ");
            String loginUsername = input.nextLine();
            System.out.print("Enter password: ");
            String loginPassword = input.nextLine();
            boolean loggedIn = user.loginUser(loginUsername, loginPassword);
            System.out.println(user.returnLoginStatus(loggedIn));
            if (!loggedIn) return;

            System.out.println("Welcome to QuickChat.");
            int messageLimit = readNonNegativeInt(input, "How many messages would you like to enter? ");
            int createdMessages = 0;
            boolean running = true;
            while (running) {
                System.out.println("\n===== QuickChat Menu =====");
                System.out.println("1) Send Messages");
                System.out.println("2) Show recently sent messages");
                System.out.println("3) Quit");
                System.out.print("Choose an option: ");
                String option = input.nextLine().trim();

                switch (option) {
                    case "1":
                        if (createdMessages >= messageLimit) {
                            System.out.println("You have reached the number of messages you selected.");
                            break;
                        }
                        System.out.print("Enter recipient number with international code (e.g. +27718693002): ");
                        String recipient = input.nextLine().trim();
                        System.out.print("Enter your message (maximum 250 characters): ");
                        String text = input.nextLine();
                        Message message = new Message(createdMessages, recipient, text);

                        System.out.println(message.recipientValidationMessage());
                        if (!message.checkRecipientCell()) break;
                        System.out.println(message.messageLengthMessage());
                        if (!message.checkMessageLength()) break;

                        System.out.println("Message ID generated: " + message.getMessageID());
                        System.out.println("Message Hash: " + message.getMessageHash());
                        createdMessages++;
                        System.out.println("Choose an action:");
                        System.out.println("1) Send Message");
                        System.out.println("2) Disregard Message");
                        System.out.println("3) Store Message to send later");
                        System.out.print("Choose an option: ");
                        int action = readAction(input);
                        System.out.println(message.SentMessage(action));
                        if (action == 1 || action == 3) {
                            System.out.println(message.printMessages());
                        }
                        break;
                    case "2":
                        System.out.println("Coming Soon.");
                        break;
                    case "3":
                        running = false;
                        System.out.println("Total messages sent: " + Message.returnTotalMessagess());
                        System.out.println("Goodbye.");
                        break;
                    default:
                        System.out.println("Invalid option. Please choose 1, 2, or 3.");
                }
            }
        }
    }

    private static int readNonNegativeInt(Scanner input, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(input.nextLine().trim());
                if (value >= 0) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a whole number that is zero or greater.");
        }
    }

    private static int readAction(Scanner input) {
        while (true) {
            try {
                int value = Integer.parseInt(input.nextLine().trim());
                if (value >= 1 && value <= 3) return value;
            } catch (NumberFormatException ignored) { }
            System.out.print("Enter 1, 2, or 3: ");
        }
    }
}
