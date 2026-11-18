import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** A small console app that categorizes service requests and routes them to an actioner. */
public class ServiceRequestRouter {
    private enum Category {
        IT_SUPPORT("IT Support", "IT Support Team"),
        FACILITIES("Facilities", "Facilities Team"),
        HUMAN_RESOURCES("Human Resources", "HR Team"),
        FINANCE("Finance", "Finance Team"),
        OTHER("Other", "Service Desk");

        private final String label;
        private final String actioner;

        Category(String label, String actioner) {
            this.label = label;
            this.actioner = actioner;
        }
    }

    private static class ServiceRequest {
        private final String description;
        private final Category category;

        ServiceRequest(String description, Category category) {
            this.description = description;
            this.category = category;
        }

        @Override
        public String toString() {
            return "[" + category.label + "] " + description + " -> " + category.actioner;
        }
    }

    public static void main(String[] args) {
        List<ServiceRequest> requests = new ArrayList<>();

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== Service Request Router ===");
            boolean running = true;

            while (running) {
                System.out.print("\nEnter a short description of your service request: ");
                String description = scanner.nextLine().trim();
                if (description.isEmpty()) {
                    System.out.println("A request description cannot be empty.");
                    continue;
                }

                Category category = readCategory(scanner);
                ServiceRequest request = new ServiceRequest(description, category);
                requests.add(request);
                System.out.println("Request submitted and routed to: " + category.actioner);

                System.out.print("Submit another request? (y/n): ");
                running = scanner.nextLine().trim().equalsIgnoreCase("y");
            }
        }

        System.out.println("\n=== Submitted Requests ===");
        if (requests.isEmpty()) {
            System.out.println("No requests were submitted.");
        } else {
            for (int i = 0; i < requests.size(); i++) {
                System.out.println((i + 1) + ". " + requests.get(i));
            }
        }
        System.out.println("Goodbye!");
    }

    private static Category readCategory(Scanner scanner) {
        while (true) {
            System.out.println("Choose a service category:");
            Category[] categories = Category.values();
            for (int i = 0; i < categories.length; i++) {
                System.out.println((i + 1) + ". " + categories[i].label);
            }
            System.out.print("Category number: ");
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= categories.length) {
                    return categories[choice - 1];
                }
            } catch (NumberFormatException ignored) {
                // Show the menu again for non-numeric input.
            }
            System.out.println("Please enter a number from 1 to " + categories.length + ".");
        }
    }
}
