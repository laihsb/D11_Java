# D11_Java

A simple Java console program for submitting and routing service requests by category. Requests are saved in a local JSON file so they are available when the program is started again.

## Run the program

Requires Java 8 or newer.

```bash
javac src/main/java/ServiceRequestRouter.java
java -cp src/main/java ServiceRequestRouter
```

Enter a short request description, select a category, and the program routes the request to its assigned actioner. You can submit multiple requests in one session and review all saved requests before exiting. New requests are saved to `requests.json` in the directory where you run the program. Existing saved requests are loaded on startup.

The JSON file is updated after each request using a temporary file and atomic replacement where the file system supports it. Back up `requests.json` before editing it manually.

## Default routing

- IT Support → IT Support Team
- Facilities → Facilities Team
- Human Resources → HR Team
- Finance → Finance Team
- Other → Service Desk
