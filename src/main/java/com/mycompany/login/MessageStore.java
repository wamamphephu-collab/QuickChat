package com.mycompany.login;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 3: holds the arrays used by the "Stored Messages" feature and
 * implements the search, delete and report operations on them.
 *
 * Nothing is hard-coded: every array is filled at run time by addMessage()
 * (as the user sends / disregards / stores messages) or by loadStoredMessages()
 * (which reads messages.json).
 */
public class MessageStore {

    private final List<Message> sentMessages = new ArrayList<>();
    private final List<Message> disregardedMessages = new ArrayList<>();
    private final List<Message> storedMessages = new ArrayList<>();
    private final List<String> messageHashes = new ArrayList<>();
    private final List<String> messageIDs = new ArrayList<>();

    // ---------- Populating the arrays ----------

    /**
     * Adds a message to the array that matches its flag ("Sent", "Disregarded"
     * or "Stored") and records its hash and ID in the hash / ID arrays.
     *
     * @return true if the message was added, false if the flag is not recognised
     */
    public boolean addMessage(Message message, String flag) {
        switch (flag.toLowerCase()) {
            case "sent":
                message.setStatus("Sent");
                sentMessages.add(message);
                break;
            case "disregard":
            case "disregarded":
                message.setStatus("Disregarded");
                disregardedMessages.add(message);
                break;
            case "stored":
                message.setStatus("Stored");
                storedMessages.add(message);
                break;
            default:
                return false;
        }
        messageHashes.add(message.getMessageHash());
        messageIDs.add(message.getMessageID());
        return true;
    }

    /**
     * Reads the JSON file written by Message.storeMessage() into the stored
     * messages array. Messages already in the array (same hash and ID) are
     * skipped so the file can be re-read safely.
     *
     * JSON parsing uses Google Gson (JsonParser):
     * https://github.com/google/gson/blob/main/UserGuide.md
     *
     * @return the number of messages loaded
     */
    public int loadStoredMessages(Path file) throws IOException {
        if (!Files.exists(file)) {
            return 0;
        }
        String json = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (json.isEmpty()) {
            return 0;
        }

        int loaded = 0;
        try {
            JsonArray entries = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement element : entries) {
                JsonObject entry = element.getAsJsonObject();
                Message message = new Message(
                        entry.get("messageID").getAsString(),
                        entry.get("messageNumber").getAsInt(),
                        entry.get("recipient").getAsString(),
                        entry.get("message").getAsString());
                if (findInList(storedMessages, message.getMessageID()) == null) {
                    addMessage(message, "Stored");
                    loaded++;
                }
            }
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException ex) {
            throw new IOException("messages.json is not in the expected format.", ex);
        }
        return loaded;
    }

    /** Rewrites the JSON file so it matches the stored messages array. */
    public void saveStoredMessages(Path file) throws IOException {
        JsonArray entries = new JsonArray();
        for (Message m : storedMessages) {
            JsonObject entry = new JsonObject();
            entry.addProperty("messageID", m.getMessageID());
            entry.addProperty("messageNumber", m.getMessageNumber());
            entry.addProperty("messageHash", m.getMessageHash());
            entry.addProperty("recipient", m.getRecipient());
            entry.addProperty("message", m.getMessage());
            entry.addProperty("status", "Stored");
            entries.add(entry);
        }
        Files.writeString(file, entries.toString() + "\n", StandardCharsets.UTF_8);
    }

    // ---------- Getters (copies, so callers cannot change the arrays) ----------

    public List<Message> getSentMessages() { return new ArrayList<>(sentMessages); }
    public List<Message> getDisregardedMessages() { return new ArrayList<>(disregardedMessages); }
    public List<Message> getStoredMessages() { return new ArrayList<>(storedMessages); }
    public List<String> getMessageHashes() { return new ArrayList<>(messageHashes); }
    public List<String> getMessageIDs() { return new ArrayList<>(messageIDs); }

    /** The text of every sent message, in the order they were sent. */
    public List<String> getSentMessageTexts() {
        List<String> texts = new ArrayList<>();
        for (Message m : sentMessages) {
            texts.add(m.getMessage());
        }
        return texts;
    }

    // ---------- Menu feature 4a-4f ----------

    /** 4a. Sender and recipient of every stored message. */
    public String displaySenderAndRecipient(String sender) {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder out = new StringBuilder();
        for (Message m : storedMessages) {
            out.append("Sender: ").append(sender)
               .append(" | Recipient: ").append(m.getRecipient()).append('\n');
        }
        return out.toString().stripTrailing();
    }

    /** 4b. The longest stored message, or null if nothing is stored. */
    public Message findLongestStoredMessage() {
        Message longest = null;
        for (Message m : storedMessages) {
            if (longest == null || m.getMessage().length() > longest.getMessage().length()) {
                longest = m;
            }
        }
        return longest;
    }

    public String displayLongestStoredMessage() {
        Message longest = findLongestStoredMessage();
        return longest == null ? "There are no stored messages."
                : "Longest stored message: " + longest.getMessage();
    }

    /** 4c. Finds a message (sent, stored or disregarded) by its message ID. */
    public Message searchByMessageID(String messageID) {
        Message found = findInList(sentMessages, messageID);
        if (found == null) found = findInList(storedMessages, messageID);
        if (found == null) found = findInList(disregardedMessages, messageID);
        return found;
    }

    public String displayMessageSearch(String messageID) {
        Message found = searchByMessageID(messageID);
        return found == null ? "No message found with ID " + messageID + "."
                : "Recipient: " + found.getRecipient() + "\nMessage: " + found.getMessage();
    }

    /** 4d. All sent or stored messages for one recipient, sent messages first. */
    public List<Message> searchByRecipient(String recipient) {
        List<Message> matches = new ArrayList<>();
        for (Message m : sentMessages) {
            if (m.getRecipient().equals(recipient.trim())) matches.add(m);
        }
        for (Message m : storedMessages) {
            if (m.getRecipient().equals(recipient.trim())) matches.add(m);
        }
        return matches;
    }

    public String displayMessagesForRecipient(String recipient) {
        List<Message> matches = searchByRecipient(recipient);
        if (matches.isEmpty()) {
            return "No sent or stored messages found for " + recipient + ".";
        }
        StringBuilder out = new StringBuilder();
        for (Message m : matches) {
            out.append(m.getMessage()).append('\n');
        }
        return out.toString().stripTrailing();
    }

    /**
     * 4e. Deletes the message with the given hash from every array (and keeps
     * the hash / ID arrays in step).
     */
    public String deleteByMessageHash(String messageHash) {
        int index = messageHashes.indexOf(messageHash);
        if (index < 0) {
            return "No message found with hash " + messageHash + ".";
        }
        String deletedText = null;
        for (List<Message> list : List.of(sentMessages, disregardedMessages, storedMessages)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getMessageHash().equals(messageHash)) {
                    deletedText = list.get(i).getMessage();
                    list.remove(i);
                    break;
                }
            }
        }
        messageHashes.remove(index);
        messageIDs.remove(index);
        return "Message: \"" + deletedText + "\" successfully deleted.";
    }

    /** 4f / report. Full details of the messages in the given array. */
    public static String buildReport(String title, List<Message> messages) {
        StringBuilder out = new StringBuilder("===== " + title + " =====\n");
        if (messages.isEmpty()) {
            return out.append("No messages to report.").toString();
        }
        int count = 1;
        for (Message m : messages) {
            out.append(count++).append(")\n")
               .append("Message Hash: ").append(m.getMessageHash()).append('\n')
               .append("Message ID: ").append(m.getMessageID()).append('\n')
               .append("Recipient: ").append(m.getRecipient()).append('\n')
               .append("Message: ").append(m.getMessage()).append('\n');
        }
        return out.toString().stripTrailing();
    }

    public String displayStoredReport() { return buildReport("Stored Messages Report", storedMessages); }
    public String displaySentReport() { return buildReport("Sent Messages Report", sentMessages); }

    // ---------- Helpers ----------

    private static Message findInList(List<Message> list, String messageID) {
        for (Message m : list) {
            if (m.getMessageID().equals(messageID)) return m;
        }
        return null;
    }
}
