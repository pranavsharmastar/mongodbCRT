package com.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class App {

    private static MongoCollection<Document> students;
    private static MongoCollection<Document> companies;
    private static MongoCollection<Document> jobDrives;
    private static MongoCollection<Document> applications;

    private static Scanner sc;

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .directory("D:/JAVA MONGODB CONN/mongodb-java-demo")
                .ignoreIfMissing()
                .load();

        String uri = dotenv.get("MONGODB_URI");

        if (uri == null || uri.isBlank()) {
            System.out.println("MONGODB_URI not found in .env file.");
            return;
        }

        try (MongoClient client = MongoClients.create(uri);
             Scanner scanner = new Scanner(System.in)) {

            sc = scanner;

            MongoDatabase database = client.getDatabase("collegeDB");

            students = database.getCollection("students");
            companies = database.getCollection("companies");
            jobDrives = database.getCollection("jobDrives");
            applications = database.getCollection("applications");

            database.runCommand(new Document("ping", 1));
            System.out.println("MongoDB connected successfully!");
            System.out.println("Recruitment Management System");

            while (true) {
                System.out.println("\n========== MAIN MENU ==========");
                System.out.println("1. Student Management");
                System.out.println("2. Company Management");
                System.out.println("3. Job Drive Management");
                System.out.println("4. Application Management");
                System.out.println("5. Interview Round Management");
                System.out.println("6. Placement Analytics");
                System.out.println("7. View All Collections");
                System.out.println("8. Exit");
                System.out.print("Enter choice: ");

                int choice = readInt();

                switch (choice) {
                    case 1 -> studentMenu();
                    case 2 -> companyMenu();
                    case 3 -> driveMenu();
                    case 4 -> applicationMenu();
                    case 5 -> roundMenu();
                    case 6 -> placementAnalytics();
                    case 7 -> viewAllCollections();
                    case 8 -> {
                        System.out.println("Exiting application...");
                        return;
                    }
                    default -> System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= COMMON INPUT HELPERS =================

    private static int readInt() {
        while (!sc.hasNextInt()) {
            System.out.print("Enter a valid number: ");
            sc.nextLine();
        }
        int value = sc.nextInt();
        sc.nextLine();
        return value;
    }

    private static double readDouble() {
        while (!sc.hasNextDouble()) {
            System.out.print("Enter a valid number: ");
            sc.nextLine();
        }
        double value = sc.nextDouble();
        sc.nextLine();
        return value;
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static Date readDate(String prompt) {
        while (true) {
            String value = readText(prompt + " (YYYY-MM-DD): ");
            try {
                return Date.from(
                        Instant.parse(value + "T00:00:00Z")
                );
            } catch (Exception e) {
                System.out.println("Invalid date. Example: 2026-10-27");
            }
        }
    }

    private static java.util.List<String> readList(String prompt) {
        String value = readText(prompt + " (comma-separated): ");

        if (value.isBlank()) {
            return new java.util.ArrayList<>();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    private static void printDocuments(
            MongoCollection<Document> collection,
            Bson filter) {

        boolean found = false;

        for (Document doc : collection.find(filter)) {
            System.out.println(doc.toJson());
            found = true;
        }

        if (!found) {
            System.out.println("No matching records found.");
        }
    }

    private static boolean exists(
            MongoCollection<Document> collection,
            String field,
            String value) {

        return collection.find(Filters.eq(field, value)).first() != null;
    }

    private static boolean validStudentAndDrive(
            String studentId, String driveId) {

        boolean studentExists =
                exists(students, "studentId", studentId);

        boolean driveExists =
                exists(jobDrives, "driveId", driveId);

        if (!studentExists) {
            System.out.println("Student ID does not exist.");
        }

        if (!driveExists) {
            System.out.println("Job drive ID does not exist.");
        }

        return studentExists && driveExists;
    }

    // ================= STUDENT MANAGEMENT =================

    private static void studentMenu() {
        while (true) {
            System.out.println("\n--- Student Management ---");
            System.out.println("1. Register Student");
            System.out.println("2. Search Student");
            System.out.println("3. View All Students");
            System.out.println("4. Update Placement Status");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> registerStudent();
                case 2 -> searchStudent();
                case 3 -> printDocuments(students, new Document());
                case 4 -> updatePlacementStatus();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void registerStudent() {
        String id = readText("Student ID (e.g. STU106): ");

        if (id.isBlank() || exists(students, "studentId", id)) {
            System.out.println("Invalid or duplicate student ID.");
            return;
        }

        String name = readText("Name: ");
        String email = readText("Email: ");
        String phone = readText("Phone: ");
        String department = readText("Department: ");
        int year = readIntPrompt("Graduation year: ");
        double cgpa = readDoublePrompt("CGPA: ");

        if (cgpa < 0 || cgpa > 10) {
            System.out.println("CGPA must be between 0 and 10.");
            return;
        }

        java.util.List<String> skills = readList("Skills");
        java.util.List<String> certifications =
                readList("Certifications");

        String city = readText("City: ");
        String state = readText("State: ");

        int tenth = readIntPrompt("10th percentage: ");
        int twelfth = readIntPrompt("12th percentage: ");

        Document student = new Document("studentId", id)
                .append("name", name)
                .append("email", email)
                .append("phone", phone)
                .append("department", department)
                .append("graduationYear", year)
                .append("cgpa", cgpa)
                .append("skills", skills)
                .append("certifications", certifications)
                .append("address", new Document("city", city)
                        .append("state", state))
                .append("education",
                        new Document("tenthPercentage", tenth)
                                .append("twelfthPercentage", twelfth))
                .append("placementStatus", "Not Placed");

        students.insertOne(student);
        System.out.println("Student registered successfully!");
    }

    private static void searchStudent() {
        String id = readText("Enter student ID: ");

        printDocuments(students, Filters.eq("studentId", id));
    }

    private static void updatePlacementStatus() {
        String id = readText("Enter student ID: ");
        String status = readText(
                "Enter status (Not Placed/Placed): ");

        if (!status.equalsIgnoreCase("Placed")
                && !status.equalsIgnoreCase("Not Placed")) {
            System.out.println("Invalid placement status.");
            return;
        }

        UpdateResult result = students.updateOne(
                Filters.eq("studentId", id),
                Updates.set("placementStatus",
                        status.equalsIgnoreCase("Placed")
                                ? "Placed" : "Not Placed")
        );

        System.out.println(result.getMatchedCount() > 0
                ? "Placement status updated."
                : "Student not found.");
    }

    // ================= COMPANY MANAGEMENT =================

    private static void companyMenu() {
        while (true) {
            System.out.println("\n--- Company Management ---");
            System.out.println("1. Add Company");
            System.out.println("2. Search Company");
            System.out.println("3. View All Companies");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> addCompany();
                case 2 -> {
                    String id = readText("Enter company ID: ");
                    printDocuments(companies,
                            Filters.eq("companyId", id));
                }
                case 3 -> printDocuments(companies, new Document());
                case 4 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void addCompany() {
        String id = readText("Company ID (e.g. COMP113): ");

        if (id.isBlank() || exists(companies, "companyId", id)) {
            System.out.println("Invalid or duplicate company ID.");
            return;
        }

        String name = readText("Company name: ");
        String industry = readText("Industry: ");
        String city = readText("City: ");
        String state = readText("State: ");

        java.util.List<String> roles =
                readList("Hiring roles");

        double minimumCGPA =
                readDoublePrompt("Minimum CGPA: ");

        java.util.List<String> departments =
                readList("Allowed departments");

        Document company = new Document("companyId", id)
                .append("companyName", name)
                .append("industry", industry)
                .append("location", new Document("city", city)
                        .append("state", state))
                .append("hiringRoles", roles)
                .append("eligibility",
                        new Document("minimumCGPA", minimumCGPA)
                                .append("allowedDepartments",
                                        departments));

        companies.insertOne(company);
        System.out.println("Company added successfully!");
    }

    // ================= JOB DRIVE MANAGEMENT =================

    private static void driveMenu() {
        while (true) {
            System.out.println("\n--- Job Drive Management ---");
            System.out.println("1. Post Job Drive");
            System.out.println("2. View All Job Drives");
            System.out.println("3. Search Job Drive");
            System.out.println("4. Update Drive Status");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> postJobDrive();
                case 2 -> printDocuments(jobDrives, new Document());
                case 3 -> {
                    String id = readText("Enter drive ID: ");
                    printDocuments(jobDrives,
                            Filters.eq("driveId", id));
                }
                case 4 -> updateDriveStatus();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void postJobDrive() {
        String id = readText("Drive ID (e.g. DRV109): ");

        if (id.isBlank() || exists(jobDrives, "driveId", id)) {
            System.out.println("Invalid or duplicate drive ID.");
            return;
        }

        String companyId = readText("Company ID: ");

        if (!exists(companies, "companyId", companyId)) {
            System.out.println("Company not found. Add it first.");
            return;
        }

        String role = readText("Job role: ");
        int ctc = readIntPrompt("CTC (LPA): ");
        double eligibilityCGPA =
                readDoublePrompt("Minimum eligibility CGPA: ");

        java.util.List<String> skills =
                readList("Required skills");

        Date driveDate = readDate("Drive date");
        String mode = readText("Mode (On Campus/Off Campus): ");

        Document drive = new Document("driveId", id)
                .append("companyId", companyId)
                .append("role", role)
                .append("package", new Document("ctc", ctc)
                        .append("currency", "LPA"))
                .append("eligibilityCGPA", eligibilityCGPA)
                .append("skillsRequired", skills)
                .append("driveDate", driveDate)
                .append("mode", mode)
                .append("status", "Open");

        jobDrives.insertOne(drive);
        System.out.println("Job drive posted successfully!");
    }

    private static void updateDriveStatus() {
        String id = readText("Enter drive ID: ");
        String status = readText(
                "Enter status (Open/Closed/Completed): ");

        if (!Arrays.asList("Open", "Closed", "Completed")
                .contains(status)) {
            System.out.println("Invalid status.");
            return;
        }

        UpdateResult result = jobDrives.updateOne(
                Filters.eq("driveId", id),
                Updates.set("status", status)
        );

        System.out.println(result.getMatchedCount() > 0
                ? "Drive status updated."
                : "Job drive not found.");
    }

    // ================= APPLICATION MANAGEMENT =================

    private static void applicationMenu() {
        while (true) {
            System.out.println("\n--- Application Management ---");
            System.out.println("1. Apply for Job Drive");
            System.out.println("2. Search Application");
            System.out.println("3. View All Applications");
            System.out.println("4. Update Application Status");
            System.out.println("5. View Applications by Student");
            System.out.println("6. Back");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> applyForDrive();
                case 2 -> {
                    String id = readText("Application ID: ");
                    printDocuments(applications,
                            Filters.eq("applicationId", id));
                }
                case 3 -> printDocuments(applications, new Document());
                case 4 -> updateApplicationStatus();
                case 5 -> {
                    String id = readText("Student ID: ");
                    printDocuments(applications,
                            Filters.eq("studentId", id));
                }
                case 6 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void applyForDrive() {
        String applicationId =
                readText("Application ID (e.g. APP102): ");

        if (applicationId.isBlank()
                || exists(applications, "applicationId", applicationId)) {
            System.out.println("Invalid or duplicate application ID.");
            return;
        }

        String studentId = readText("Student ID: ");
        String driveId = readText("Drive ID: ");

        if (!validStudentAndDrive(studentId, driveId)) {
            return;
        }

        Document drive = jobDrives.find(
                Filters.eq("driveId", driveId)).first();

        if (!"Open".equals(drive.getString("status"))) {
            System.out.println("This job drive is not open.");
            return;
        }

        Document student = students.find(
                Filters.eq("studentId", studentId)).first();

        if (applications.find(
                Filters.and(
                        Filters.eq("studentId", studentId),
                        Filters.eq("driveId", driveId)
                )).first() != null) {
            System.out.println(
                    "This student has already applied to this drive.");
            return;
        }

        double cgpa = student.get("cgpa", Number.class).doubleValue();
        double requiredCGPA =
                drive.get("eligibilityCGPA", Number.class).doubleValue();

        if (cgpa < requiredCGPA) {
            System.out.println("Student does not meet the CGPA requirement.");
            return;
        }

        Document company = companies.find(
                Filters.eq("companyId", drive.getString("companyId"))
        ).first();

        if (company != null) {
            Document eligibility = company.get("eligibility",
                    Document.class);

            if (eligibility != null) {
                Object minValue = eligibility.get("minimumCGPA");

                if (minValue instanceof Number
                        && cgpa < ((Number) minValue).doubleValue()) {
                    System.out.println(
                            "Student does not meet company CGPA criteria.");
                    return;
                }

                java.util.List<String> allowedDepartments =
                        eligibility.getList("allowedDepartments",
                                String.class);

                if (allowedDepartments != null
                        && !allowedDepartments.isEmpty()
                        && !allowedDepartments.contains(
                                student.getString("department"))) {
                    System.out.println(
                            "Student department is not eligible.");
                    return;
                }
            }
        }

        Date applicationDate = new Date();

        Document application = new Document("applicationId", applicationId)
                .append("studentId", studentId)
                .append("driveId", driveId)
                .append("applicationDate", applicationDate)
                .append("status", "Applied")
                .append("rounds", new java.util.ArrayList<Document>());

        applications.insertOne(application);
        System.out.println("Application submitted successfully!");
    }

    private static void updateApplicationStatus() {
        String id = readText("Enter application ID: ");
        String status = readText(
                "Status (Applied/Shortlisted/Rejected/Selected): ");

        if (!Arrays.asList("Applied", "Shortlisted",
                "Rejected", "Selected").contains(status)) {
            System.out.println("Invalid application status.");
            return;
        }

        UpdateResult result = applications.updateOne(
                Filters.eq("applicationId", id),
                Updates.set("status", status)
        );

        System.out.println(result.getMatchedCount() > 0
                ? "Application status updated."
                : "Application not found.");
    }

    // ================= INTERVIEW ROUND MANAGEMENT =================

    private static void roundMenu() {
        while (true) {
            System.out.println("\n--- Interview Round Management ---");
            System.out.println("1. Add Interview Round");
            System.out.println("2. View Application Rounds");
            System.out.println("3. Update Round Status");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> addInterviewRound();
                case 2 -> viewRounds();
                case 3 -> updateRoundStatus();
                case 4 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void addInterviewRound() {
        String applicationId = readText("Application ID: ");

        Document application = applications.find(
                Filters.eq("applicationId", applicationId)).first();

        if (application == null) {
            System.out.println("Application not found.");
            return;
        }

        String roundName = readText("Round name: ");
        Date date = readDate("Round date");

        Document round = new Document("roundName", roundName)
                .append("status", "Pending")
                .append("date", date);

        UpdateResult result = applications.updateOne(
                Filters.eq("applicationId", applicationId),
                Updates.push("rounds", round)
        );

        System.out.println(result.getModifiedCount() > 0
                ? "Interview round added."
                : "No changes made.");
    }

    private static void viewRounds() {
        String applicationId = readText("Application ID: ");

        Document application = applications.find(
                Filters.eq("applicationId", applicationId)).first();

        if (application == null) {
            System.out.println("Application not found.");
            return;
        }

        System.out.println("Application status: "
                + application.getString("status"));

        java.util.List<Document> rounds =
                application.getList("rounds", Document.class);

        if (rounds == null || rounds.isEmpty()) {
            System.out.println("No interview rounds recorded.");
            return;
        }

        for (Document round : rounds) {
            System.out.println(round.toJson());
        }
    }

    private static void updateRoundStatus() {
        String applicationId = readText("Application ID: ");
        String roundName = readText("Round name: ");
        String status = readText(
                "Status (Pending/Cleared/Failed): ");

        if (!Arrays.asList("Pending", "Cleared", "Failed")
                .contains(status)) {
            System.out.println("Invalid round status.");
            return;
        }

        Document application = applications.find(
                Filters.eq("applicationId", applicationId)).first();

        if (application == null) {
            System.out.println("Application not found.");
            return;
        }

        java.util.List<Document> rounds =
                application.getList("rounds", Document.class);

        if (rounds == null) {
            System.out.println("No interview rounds found.");
            return;
        }

        boolean found = false;

        for (int i = 0; i < rounds.size(); i++) {
            if (roundName.equals(rounds.get(i).getString("roundName"))) {
                rounds.get(i).put("status", status);
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Round not found.");
            return;
        }

        UpdateResult result = applications.updateOne(
                Filters.eq("applicationId", applicationId),
                Updates.set("rounds", rounds)
        );

        System.out.println(result.getMatchedCount() > 0
                ? "Interview round status updated."
                : "Could not update round.");
    }

    // ================= PLACEMENT ANALYTICS =================

    private static void placementAnalytics() {
        long totalStudents = students.countDocuments();
        long placedStudents = students.countDocuments(
                Filters.eq("placementStatus", "Placed"));

        long totalCompanies = companies.countDocuments();
        long totalDrives = jobDrives.countDocuments();
        long openDrives = jobDrives.countDocuments(
                Filters.eq("status", "Open"));

        long totalApplications = applications.countDocuments();
        long shortlisted = applications.countDocuments(
                Filters.eq("status", "Shortlisted"));
        long selected = applications.countDocuments(
                Filters.eq("status", "Selected"));
        long rejected = applications.countDocuments(
                Filters.eq("status", "Rejected"));

        double placementRate = totalStudents == 0
                ? 0.0
                : (placedStudents * 100.0 / totalStudents);

        System.out.println("\n========== PLACEMENT ANALYTICS ==========");
        System.out.println("Total Students       : " + totalStudents);
        System.out.println("Placed Students      : " + placedStudents);
        System.out.printf("Placement Rate       : %.2f%%%n", placementRate);
        System.out.println("Total Companies      : " + totalCompanies);
        System.out.println("Total Job Drives     : " + totalDrives);
        System.out.println("Open Job Drives      : " + openDrives);
        System.out.println("Total Applications   : " + totalApplications);
        System.out.println("Shortlisted          : " + shortlisted);
        System.out.println("Selected Applications: " + selected);
        System.out.println("Rejected Applications: " + rejected);

        System.out.println("\n--- Student Placement Status ---");
        for (Document student : students.find()) {
            System.out.println(
                    student.getString("studentId") + " | "
                    + student.getString("name") + " | "
                    + student.getString("department") + " | "
                    + student.getString("placementStatus"));
        }

        System.out.println("\n--- Applications by Drive ---");
        for (Document drive : jobDrives.find()) {
            String driveId = drive.getString("driveId");
            long count = applications.countDocuments(
                    Filters.eq("driveId", driveId));

            System.out.println(driveId + " | "
                    + drive.getString("role")
                    + " | Applications: " + count);
        }
    }

    // ================= VIEW ALL COLLECTIONS =================

    private static void viewAllCollections() {
        System.out.println("\n--- Students ---");
        printDocuments(students, new Document());

        System.out.println("\n--- Companies ---");
        printDocuments(companies, new Document());

        System.out.println("\n--- Job Drives ---");
        printDocuments(jobDrives, new Document());

        System.out.println("\n--- Applications ---");
        printDocuments(applications, new Document());
    }

    // ================= NUMERIC INPUT HELPERS =================

    private static int readIntPrompt(String prompt) {
        System.out.print(prompt);
        return readInt();
    }

    private static double readDoublePrompt(String prompt) {
        System.out.print(prompt);
        return readDouble();
    }
}
