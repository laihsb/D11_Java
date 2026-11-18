import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** A small console app that categorizes service requests and routes them to an actioner. */
public class ServiceRequestRouter {
    private static final Path DATA_FILE = Paths.get("requests.json");

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

    private enum Status {
        OPEN("Open"),
        IN_PROGRESS("In Progress"),
        WAITING("Waiting"),
        COMPLETED("Completed");

        private final String label;

        Status(String label) {
            this.label = label;
        }
    }

    private static class ServiceRequest {
        private final String description;
        private final Category category;
        private Status status;

        ServiceRequest(String description, Category category) {
            this(description, category, Status.OPEN);
        }

        ServiceRequest(String description, Category category, Status status) {
            this.description = description;
            this.category = category;
            this.status = status;
        }

        @Override
        public String toString() {
            return "[" + category.label + "] [" + status.label + "] " + description
                    + " -> " + category.actioner;
        }
    }

    public static void main(String[] args) {
        final List<ServiceRequest> requests;
        try {
            requests = loadRequests();
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Could not load " + DATA_FILE + ": " + e.getMessage());
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== Service Request Router ===");
            System.out.println("Loaded " + requests.size() + " saved request(s) from " + DATA_FILE + ".");
            boolean running = true;

            while (running) {
                System.out.println("\n1. Create a request");
                System.out.println("2. View requests");
                System.out.println("3. Update a request status");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        createRequest(scanner, requests);
                        break;
                    case "2":
                        printRequests(requests);
                        break;
                    case "3":
                        updateRequestStatus(scanner, requests);
                        break;
                    case "4":
                        running = false;
                        break;
                    default:
                        System.out.println("Please enter a number from 1 to 4.");
                        break;
                }
            }
        }

        System.out.println("\n=== Saved Requests ===");
        printRequests(requests);
        System.out.println("Goodbye!");
    }

    private static void createRequest(Scanner scanner, List<ServiceRequest> requests) {
        System.out.print("Enter a short description of your service request: ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            System.out.println("A request description cannot be empty.");
            return;
        }

        Category category = readCategory(scanner);
        ServiceRequest request = new ServiceRequest(description, category);
        requests.add(request);
        try {
            saveRequests(requests);
            System.out.println("Request submitted and routed to: " + category.actioner);
            System.out.println("Status: " + request.status.label);
            System.out.println("Saved to " + DATA_FILE.toAbsolutePath());
        } catch (IOException e) {
            requests.remove(request);
            System.err.println("Could not save the request: " + e.getMessage());
        }
    }

    private static void updateRequestStatus(Scanner scanner, List<ServiceRequest> requests) {
        if (requests.isEmpty()) {
            System.out.println("There are no requests to update.");
            return;
        }

        printRequests(requests);
        Integer index = readRequestNumber(scanner, requests.size());
        if (index == null) return;

        System.out.println("Choose a status:");
        Status[] statuses = Status.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i].label);
        }
        System.out.print("Status number: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice < 1 || choice > statuses.length) {
                System.out.println("Please enter a valid status number.");
                return;
            }
            ServiceRequest request = requests.get(index);
            request.status = statuses[choice - 1];
            saveRequests(requests);
            System.out.println("Request " + (index + 1) + " status updated to: " + request.status.label);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid status number.");
        } catch (IOException e) {
            System.err.println("Could not save the status update: " + e.getMessage());
        }
    }

    private static Integer readRequestNumber(Scanner scanner, int size) {
        System.out.print("Enter the request number to update: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice >= 1 && choice <= size) return choice - 1;
            System.out.println("Please enter a request number from 1 to " + size + ".");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid request number.");
        }
        return null;
    }

    private static void printRequests(List<ServiceRequest> requests) {
        if (requests.isEmpty()) {
            System.out.println("No requests have been submitted.");
            return;
        }
        for (int i = 0; i < requests.size(); i++) {
            System.out.println((i + 1) + ". " + requests.get(i));
        }
    }

    private static List<ServiceRequest> loadRequests() throws IOException {
        List<ServiceRequest> requests = new ArrayList<>();
        if (!Files.exists(DATA_FILE)) return requests;

        Object parsed = new JsonParser(new String(Files.readAllBytes(DATA_FILE), StandardCharsets.UTF_8)).parse();
        if (!(parsed instanceof List<?>)) {
            throw new IllegalArgumentException("expected a JSON array of requests");
        }
        for (Object item : (List<?>) parsed) {
            if (!(item instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("each request must be a JSON object");
            }
            Map<?, ?> object = (Map<?, ?>) item;
            Object description = object.get("description");
            Object categoryName = object.get("category");
            Object statusName = object.get("status");
            if (!(description instanceof String) || !(categoryName instanceof String)) {
                throw new IllegalArgumentException("each request needs string description and category fields");
            }
            try {
                Category category = Category.valueOf((String) categoryName);
                Status status = Status.OPEN;
                if (statusName instanceof String) {
                    status = Status.valueOf((String) statusName);
                }
                requests.add(new ServiceRequest((String) description, category, status));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("unknown saved request category or status: " + e.getMessage());
            }
        }
        return requests;
    }

    private static void saveRequests(List<ServiceRequest> requests) throws IOException {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < requests.size(); i++) {
            ServiceRequest request = requests.get(i);
            json.append("  {\"description\": ").append(jsonString(request.description))
                    .append(", \"category\": ").append(jsonString(request.category.name()))
                    .append(", \"status\": ").append(jsonString(request.status.name()))
                    .append(", \"actioner\": ").append(jsonString(request.category.actioner)).append("}");
            if (i < requests.size() - 1) json.append(',');
            json.append('\n');
        }
        json.append("]\n");

        Path absoluteFile = DATA_FILE.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "requests-", ".json.tmp");
        try {
            Files.write(temporaryFile, json.toString().getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static String jsonString(String value) {
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': escaped.append("\\\""); break;
                case '\\': escaped.append("\\\\"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                default:
                    if (c < 0x20) escaped.append(String.format("\\u%04x", (int) c));
                    else escaped.append(c);
            }
        }
        return escaped.append('"').toString();
    }

    private static Category readCategory(Scanner scanner) {
        while (true) {
            System.out.println("Choose a service category:");
            Category[] categories = Category.values();
            for (int i = 0; i < categories.length; i++) {
                System.out.println((i + 1) + ". " + categories[i].label);
            }
            System.out.print("Category number: ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= categories.length) return categories[choice - 1];
            } catch (NumberFormatException ignored) {}
            System.out.println("Please enter a number from 1 to " + categories.length + ".");
        }
    }

    /** Minimal JSON reader for the array/object/string format written by this application. */
    private static class JsonParser {
        private final String input;
        private int position;

        JsonParser(String input) { this.input = input; }

        Object parse() {
            Object value = readValue();
            skipWhitespace();
            if (position != input.length()) fail("unexpected trailing content");
            return value;
        }

        private Object readValue() {
            skipWhitespace();
            if (position >= input.length()) return fail("unexpected end of file");
            char c = input.charAt(position);
            if (c == '[') return readArray();
            if (c == '{') return readObject();
            if (c == '"') return readString();
            return fail("expected an array, object, or string");
        }

        private List<Object> readArray() {
            List<Object> values = new ArrayList<>();
            position++;
            skipWhitespace();
            if (consume(']')) return values;
            while (true) {
                values.add(readValue());
                skipWhitespace();
                if (consume(']')) return values;
                expect(',');
            }
        }

        private Map<String, Object> readObject() {
            java.util.LinkedHashMap<String, Object> values = new java.util.LinkedHashMap<>();
            position++;
            skipWhitespace();
            if (consume('}')) return values;
            while (true) {
                skipWhitespace();
                if (position >= input.length() || input.charAt(position) != '"') fail("expected an object key");
                String key = readString();
                skipWhitespace();
                expect(':');
                Object value = readValue();
                if (values.put(key, value) != null) fail("duplicate object key: " + key);
                skipWhitespace();
                if (consume('}')) return values;
                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder value = new StringBuilder();
            while (position < input.length()) {
                char c = input.charAt(position++);
                if (c == '"') return value.toString();
                if (c < 0x20) fail("unescaped control character in string");
                if (c != '\\') {
                    value.append(c);
                    continue;
                }
                if (position >= input.length()) fail("incomplete string escape");
                char escape = input.charAt(position++);
                switch (escape) {
                    case '"': value.append('"'); break;
                    case '\\': value.append('\\'); break;
                    case '/': value.append('/'); break;
                    case 'b': value.append('\b'); break;
                    case 'f': value.append('\f'); break;
                    case 'n': value.append('\n'); break;
                    case 'r': value.append('\r'); break;
                    case 't': value.append('\t'); break;
                    case 'u': value.append(readUnicodeEscape()); break;
                    default: return fail("invalid string escape");
                }
            }
            return fail("unterminated string");
        }

        private char readUnicodeEscape() {
            if (position + 4 > input.length()) return fail("incomplete unicode escape");
            int code = 0;
            for (int i = 0; i < 4; i++) {
                int digit = Character.digit(input.charAt(position++), 16);
                if (digit < 0) return fail("invalid unicode escape");
                code = (code << 4) + digit;
            }
            return (char) code;
        }

        private boolean consume(char expected) {
            if (position < input.length() && input.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            skipWhitespace();
            if (!consume(expected)) fail("expected '" + expected + "'");
        }

        private void skipWhitespace() {
            while (position < input.length() && Character.isWhitespace(input.charAt(position))) position++;
        }

        private <T> T fail(String message) {
            throw new IllegalArgumentException(message + " at character " + position);
        }
    }
}
