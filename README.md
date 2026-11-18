# D11_Java

A simple Java console program for submitting, routing, filtering, and tracking service requests by category.

## Run the program

Requires Java 8 or newer.

```bash
javac src/main/java/ServiceRequestRouter.java
java -cp src/main/java ServiceRequestRouter
```

The menu lets you create requests, view and filter saved requests, update a request status, reroute it to another category, or exit. New requests start with the **Open** status and are routed to the team associated with their category.

When viewing requests, choose a status and category filter. Selecting **All statuses** or **All categories** disables that filter, so you can filter by either field independently or combine both. Matching requests retain their original request numbers.

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
