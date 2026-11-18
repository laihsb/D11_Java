# D11_Java

A simple Java console program for submitting, routing, and tracking service requests by category.

## Run the program

Requires Java 8 or newer.

```bash
javac src/main/java/ServiceRequestRouter.java
java -cp src/main/java ServiceRequestRouter
```

The menu lets you create requests, view all saved requests, update a request status, or exit. New requests start with the **Open** status and are routed to the team associated with their category.

Requests are loaded from and saved to `requests.json` in the directory where you run the program. Each saved request includes its description, category, status, and actioner.

## Default routing

- IT Support → IT Support Team
- Facilities → Facilities Team
- Human Resources → HR Team
- Finance → Finance Team
- Other → Service Desk

## Statuses

- Open
- In Progress
- Waiting
- Completed
