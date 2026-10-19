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
