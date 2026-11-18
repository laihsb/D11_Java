# D11_Java

A simple Java console program for submitting and routing service requests by category.

## Run the program

Requires Java 8 or newer.

```bash
javac src/main/java/ServiceRequestRouter.java
java -cp src/main/java ServiceRequestRouter
```

Enter a short request description, select a category, and the program routes the request to its assigned actioner. You can submit multiple requests in one session and review the routed requests before exiting.

## Default routing

- IT Support → IT Support Team
- Facilities → Facilities Team
- Human Resources → HR Team
- Finance → Finance Team
- Other → Service Desk
