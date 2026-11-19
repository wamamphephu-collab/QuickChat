# QuickChat Part 2

## Run in NetBeans
Open the `Login` folder as a Maven project and run `com.mycompany.login.Main`.

## Run tests
From the project folder, run:

```bash
mvn test
```

## Part 2 implementation
- Login is required before the QuickChat menu appears.
- Menu options: send messages, show recently sent messages (Coming Soon), quit.
- The user sets a message limit when the chat starts.
- `Message` generates a ten-digit ID, creates the message hash, validates recipient/message length, sends/disregards/stores messages, prints message details, and counts sent messages.
- Stored messages are appended to `messages.json` in the application's working directory.
- Unit tests cover ID generation, recipient validation, message length, hash generation, actions, and print order.

## Important specification note
The brief's example recipient `08575975889` has no international prefix and therefore fails the required international-code validation. The program rejects it; use a number such as `+27718693002` for a valid South African international-format number.

---

# QuickChat Part 3

## What was added
- `MessageStore` class holding the arrays: sent, disregarded, stored, message hashes and message IDs. All are filled at run time (nothing hard-coded).
- On start-up `messages.json` is read into the stored messages array (parsed with Google Gson: https://github.com/google/gson/blob/main/UserGuide.md).
- Main menu option 4, **Stored Messages**, with: sender/recipient of stored messages, longest stored message, search by message ID, search by recipient (sent + stored), delete by message hash (also updates `messages.json`), full stored-messages report, and a sent-messages report.
- `MessageStoreTest` covers every feature using the Part 3 test data from the brief.

## Notes on the brief's test data
- Message 4's number `0838884567` has no international code, so the live app would reject it as a recipient. The unit tests construct the message directly and use that number as both its ID and recipient.
- Message 4's text is tested as `"It is dinner time!"` (the expected output in the brief has no space before the `!`).
- The delete message prints the stored text exactly as saved, including its final full stop.

## CI
`.github/workflows/maven.yml` runs `mvn clean verify` (all tests) on pushes to `Part3-QuickChat` and `main`.
