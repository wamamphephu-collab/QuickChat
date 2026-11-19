package com.mycompany.login;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    @Test
    void messageIdIsTenDigitsOrFewer() {
        Message message = new Message(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageID().length());
        assertTrue(message.getMessageID().matches("\\d{10}"));
    }

    @Test
    void recipientAcceptsInternationalFormat() {
        Message message = new Message(0, "+27718693002", "Hi Mike");
        assertTrue(message.checkRecipientCell());
        assertEquals("Cell phone number successfully captured.", message.recipientValidationMessage());
    }

    @Test
    void recipientRejectsLocalNumberWithoutInternationalCode() {
        Message message = new Message(0, "08575975889", "Hi Keegan");
        assertFalse(message.checkRecipientCell());
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                message.recipientValidationMessage());
    }

    @Test
    void messageLengthSuccessAndFailure() {
        Message valid = new Message(0, "+27718693002", "Hello");
        assertTrue(valid.checkMessageLength());
        assertEquals("Message ready to send.", valid.messageLengthMessage());

        Message invalid = new Message(0, "+27718693002", "x".repeat(253));
        assertFalse(invalid.checkMessageLength());
        assertEquals("Message exceeds 250 characters by 3; please reduce the size.", invalid.messageLengthMessage());
    }

    @Test
    void messageHashUsesIdPrefixNumberAndFirstAndLastWords() {
        Message message = new Message("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("00:0:HITONIGHT", message.getMessageHash());
    }

    @Test
    void sendDisregardAndStoreReturnExpectedMessages() throws Exception {
        Message sent = new Message("0012345678", 0, "+27718693002", "Hi tonight");
        assertEquals("Message successfully sent.", sent.SentMessage(1));

        Message disregarded = new Message("0012345679", 1, "+27718693002", "Hi tonight");
        assertEquals("Press 0 to delete the message.", disregarded.SentMessage(2));

        // Store test is run in a temporary working directory by CI if filesystem isolation is needed.
        Message stored = new Message("0012345680", 2, "+27718693002", "Hi tonight");
        assertEquals("Message successfully stored.", stored.SentMessage(3));
    }

    @Test
    void printMessagesIncludesRequestedFieldsInOrder() {
        Message message = new Message("1234567890", 0, "+27718693002", "Hi Mike tonight");
        String output = message.printMessages();
        assertTrue(output.indexOf("Message ID:") < output.indexOf("Message Hash:"));
        assertTrue(output.indexOf("Message Hash:") < output.indexOf("Recipient:"));
        assertTrue(output.indexOf("Recipient:") < output.indexOf("Message:"));
    }
}
