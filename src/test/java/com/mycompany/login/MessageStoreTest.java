package com.mycompany.login;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Part 3 unit tests, using the test data from the assessment brief. */
class MessageStoreTest {

    private static final String LONG_TEXT = "Where are you? You are late! I have asked you to be on time.";

    private MessageStore store;
    private Message message2; // used by the delete test

    /** Populates the arrays with Test Data Messages 1-5 (flags: Sent / Stored / Disregard). */
    @BeforeEach
    void setUp() {
        store = new MessageStore();

        Message message1 = new Message("0123456781", 0, "+27834557896", "Did you get the cake?");
        message2 = new Message("0123456782", 1, "+27838884567", LONG_TEXT);
        Message message3 = new Message("0123456783", 2, "+27834484567", "Yohoooo, I am at your gate.");
        // Message 4 uses the number 0838884567 from the brief as both ID and recipient.
        Message message4 = new Message("0838884567", 3, "0838884567", "It is dinner time!");
        Message message5 = new Message("0123456785", 4, "+27838884567", "Ok, I am leaving without you.");

        store.addMessage(message1, "Sent");
        store.addMessage(message2, "Stored");
        store.addMessage(message3, "Disregard");
        store.addMessage(message4, "Sent");
        store.addMessage(message5, "Stored");
    }

    @Test
    void sentMessagesArrayIsCorrectlyPopulated() {
        assertEquals(List.of("Did you get the cake?", "It is dinner time!"), store.getSentMessageTexts());
    }

    @Test
    void disregardedAndStoredArraysArePopulated() {
        assertEquals(1, store.getDisregardedMessages().size());
        assertEquals("Yohoooo, I am at your gate.", store.getDisregardedMessages().get(0).getMessage());
        assertEquals(2, store.getStoredMessages().size());
    }

    @Test
    void hashAndIdArraysContainEveryMessage() {
        assertEquals(5, store.getMessageHashes().size());
        assertEquals(5, store.getMessageIDs().size());
        assertTrue(store.getMessageHashes().contains(message2.getMessageHash()));
        assertTrue(store.getMessageIDs().contains("0123456782"));
    }

    @Test
    void unknownFlagIsRejected() {
        assertFalse(store.addMessage(new Message(9, "+27834557896", "Hi"), "Banana"));
        assertEquals(5, store.getMessageIDs().size());
    }

    @Test
    void displayLongestMessage() {
        assertEquals(LONG_TEXT, store.findLongestStoredMessage().getMessage());
        assertEquals("Longest stored message: " + LONG_TEXT, store.displayLongestStoredMessage());
    }

    @Test
    void longestMessageWhenNothingStored() {
        assertNull(new MessageStore().findLongestStoredMessage());
        assertEquals("There are no stored messages.", new MessageStore().displayLongestStoredMessage());
    }

    @Test
    void searchForMessageId() {
        Message found = store.searchByMessageID("0838884567");
        assertNotNull(found);
        assertEquals("It is dinner time!", found.getMessage());
        assertEquals("0838884567", found.getRecipient());
        assertEquals("Recipient: 0838884567\nMessage: It is dinner time!",
                store.displayMessageSearch("0838884567"));
    }

    @Test
    void searchForUnknownMessageId() {
        assertNull(store.searchByMessageID("9999999999"));
        assertEquals("No message found with ID 9999999999.", store.displayMessageSearch("9999999999"));
    }

    @Test
    void searchMessagesForRecipient() {
        List<Message> matches = store.searchByRecipient("+27838884567");
        assertEquals(2, matches.size());
        assertEquals(LONG_TEXT, matches.get(0).getMessage());
        assertEquals("Ok, I am leaving without you.", matches.get(1).getMessage());
        assertEquals(LONG_TEXT + "\nOk, I am leaving without you.",
                store.displayMessagesForRecipient("+27838884567"));
    }

    @Test
    void searchForRecipientWithNoMessages() {
        assertTrue(store.searchByRecipient("+27000000000").isEmpty());
    }

    @Test
    void deleteMessageUsingMessageHash() {
        String hash = message2.getMessageHash();
        assertEquals("Message: \"" + LONG_TEXT + "\" successfully deleted.",
                store.deleteByMessageHash(hash));
        assertEquals(1, store.getStoredMessages().size());
        assertFalse(store.getMessageHashes().contains(hash));
        assertFalse(store.getMessageIDs().contains("0123456782"));
        assertNull(store.searchByMessageID("0123456782"));
    }

    @Test
    void deleteWithUnknownHashChangesNothing() {
        assertEquals("No message found with hash NOPE.", store.deleteByMessageHash("NOPE"));
        assertEquals(5, store.getMessageHashes().size());
    }

    @Test
    void sentMessagesReportShowsHashRecipientAndMessage() {
        String report = store.displaySentReport();
        for (Message m : store.getSentMessages()) {
            assertTrue(report.contains("Message Hash: " + m.getMessageHash()));
            assertTrue(report.contains("Recipient: " + m.getRecipient()));
            assertTrue(report.contains("Message: " + m.getMessage()));
        }
        assertFalse(report.contains("Ok, I am leaving without you."));
    }

    @Test
    void storedReportListsFullDetailsOfStoredMessages() {
        String report = store.displayStoredReport();
        assertTrue(report.contains("Message ID: 0123456782"));
        assertTrue(report.contains("Message Hash: " + message2.getMessageHash()));
        assertTrue(report.contains("Recipient: +27838884567"));
        assertTrue(report.contains("Message: " + LONG_TEXT));
        assertTrue(report.contains("Message: Ok, I am leaving without you."));
        assertFalse(report.contains("Did you get the cake?"));
    }

    @Test
    void senderAndRecipientOfStoredMessages() {
        assertEquals("Sender: Kyle Smith | Recipient: +27838884567\n"
                + "Sender: Kyle Smith | Recipient: +27838884567",
                store.displaySenderAndRecipient("Kyle Smith"));
    }

    @Test
    void storedMessagesRoundTripThroughJsonFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("messages.json");
        store.saveStoredMessages(file);

        MessageStore reloaded = new MessageStore();
        assertEquals(2, reloaded.loadStoredMessages(file));
        assertEquals(LONG_TEXT, reloaded.getStoredMessages().get(0).getMessage());
        assertEquals(message2.getMessageHash(), reloaded.getStoredMessages().get(0).getMessageHash());

        // Re-reading the same file must not create duplicates.
        assertEquals(0, reloaded.loadStoredMessages(file));
        assertEquals(2, reloaded.getStoredMessages().size());
    }

    @Test
    void readsJsonInTheFormatWrittenByMessageStoreMessage(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("messages.json");
        Files.writeString(file, "[\n  {\"messageID\":\"1234567890\",\"messageNumber\":0,"
                + "\"messageHash\":\"12:0:HITONIGHT\",\"recipient\":\"+27718693002\","
                + "\"message\":\"Hi Mike, can you join us for dinner tonight?\",\"status\":\"Stored\"}\n]\n",
                StandardCharsets.UTF_8);

        MessageStore loaded = new MessageStore();
        assertEquals(1, loaded.loadStoredMessages(file));
        assertEquals("12:0:HITONIGHT", loaded.getStoredMessages().get(0).getMessageHash());
    }

    @Test
    void missingOrEmptyJsonFileLoadsNothing(@TempDir Path dir) throws Exception {
        assertEquals(0, new MessageStore().loadStoredMessages(dir.resolve("missing.json")));
        Path empty = dir.resolve("empty.json");
        Files.writeString(empty, "[]");
        assertEquals(0, new MessageStore().loadStoredMessages(empty));
    }

    @Test
    void malformedJsonIsReported(@TempDir Path dir) throws Exception {
        Path bad = dir.resolve("bad.json");
        Files.writeString(bad, "{not json");
        assertThrows(java.io.IOException.class, () -> new MessageStore().loadStoredMessages(bad));
    }
}
