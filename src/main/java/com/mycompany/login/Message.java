package com.mycompany.login;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Represents one QuickChat message. */
public class Message {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern INTERNATIONAL_CELL = Pattern.compile("^\\+[1-9]\\d{7,11}$");
    private static final List<Message> MESSAGES = new ArrayList<>();

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String message;
    private final String messageHash;
    private String status;

    public Message(int messageNumber, String recipient, String message) {
        this(generateMessageID(), messageNumber, recipient, message);
    }

    /** Constructor with an explicit ID, useful for predictable unit tests. */
    public Message(String messageID, int messageNumber, String recipient, String message) {
        this.messageID = messageID == null ? "" : messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient == null ? "" : recipient.trim();
        this.message = message == null ? "" : message.trim();
        this.messageHash = createMessageHash();
        this.status = "Pending";
    }

    private static String generateMessageID() {
        return String.format("%010d", RANDOM.nextLong(1_000_000_000L, 10_000_000_000L));
    }

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public String getMessageHash() { return messageHash; }
    public String getStatus() { return status; }

    public boolean checkMessageID() {
        return messageID.length() <= 10;
    }

    public boolean checkRecipientCell() {
        return INTERNATIONAL_CELL.matcher(recipient).matches();
    }

    public String recipientValidationMessage() {
        return checkRecipientCell()
                ? "Cell phone number successfully captured."
                : "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public boolean checkMessageLength() {
        return message.length() <= 250;
    }

    public String messageLengthMessage() {
        if (checkMessageLength()) return "Message ready to send.";
        return "Message exceeds 250 characters by " + (message.length() - 250) + "; please reduce the size.";
    }

    public String createMessageHash() {
        String[] words = message.trim().split("\\s+");
        String first = words.length == 0 || words[0].isBlank() ? "" : words[0];
        String last = words.length == 0 || words[0].isBlank() ? "" : words[words.length - 1];
        return (messageID.substring(0, 2) + ":" + messageNumber + ":" + first + last)
                .replaceAll("[^A-Za-z0-9:]", "").toUpperCase(Locale.ROOT);
    }

    /** Applies one of the three actions and returns the required status text. */
    public String SentMessage(int choice) {
        switch (choice) {
            case 1:
                status = "Sent";
                MESSAGES.add(this);
                return "Message successfully sent.";
            case 2:
                status = "Disregarded";
                return "Press 0 to delete the message.";
            case 3:
                status = "Stored";
                try {
                    storeMessage();
                    MESSAGES.add(this);
                    return "Message successfully stored.";
                } catch (IOException ex) {
                    return "Could not store message: " + ex.getMessage();
                }
            default:
                return "Invalid option. Choose 1, 2, or 3.";
        }
    }

    public String printMessages() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + message;
    }

    public static int returnTotalMessagess() {
        return (int) MESSAGES.stream().filter(m -> "Sent".equals(m.status)).count();
    }

    public static List<Message> getMessages() {
        return new ArrayList<>(MESSAGES);
    }

    public void storeMessage() throws IOException {
        Path file = Path.of("messages.json");
        String object = "  {\"messageID\":\"" + escapeJson(messageID)
                + "\",\"messageNumber\":" + messageNumber
                + ",\"messageHash\":\"" + escapeJson(messageHash)
                + "\",\"recipient\":\"" + escapeJson(recipient)
                + "\",\"message\":\"" + escapeJson(message)
                + "\",\"status\":\"Stored\"}";
        String existing = Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8).trim() : "";
        String updated;
        if (existing.isEmpty() || existing.equals("[]")) {
            updated = "[\n" + object + "\n]\n";
        } else {
            // Insert before the closing array bracket.
            updated = existing.substring(0, existing.lastIndexOf(']')).stripTrailing()
                    + ",\n" + object + "\n]\n";
        }
        Files.writeString(file, updated, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
