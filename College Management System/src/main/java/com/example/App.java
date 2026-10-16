package com.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class App {

    private static MongoCollection<Document> students;
    private static MongoCollection<Document> faculty;
    private static MongoCollection<Document> courses;
    private static MongoCollection<Document> registrations;
    private static MongoCollection<Document> attendance;
    private static MongoCollection<Document> examinations;
    private static MongoCollection<Document> results;

    private static Scanner sc;

    public static void main(String[] args) {

        // Load MongoDB URI from .env
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

            MongoDatabase db = client.getDatabase("collegeDB");

            students = db.getCollection("students");
            faculty = db.getCollection("faculty");
            courses = db.getCollection("courses");
            registrations = db.getCollection("registrations");
            attendance = db.getCollection("attendance");
            examinations = db.getCollection("examinations");
            results = db.getCollection("results");

            db.runCommand(new Document("ping", 1));

            System.out.println("MongoDB connected successfully!");
            System.out.println("College Management System");

            while (true) {
                System.out.println("\n========== MAIN MENU ==========");
                System.out.println("1. Student Management");
                System.out.println("2. Faculty Management");
                System.out.println("3. Course Management");
                System.out.println("4. Course Registration");
                System.out.println("5. Attendance Tracking");
                System.out.println("6. Examination Management");
                System.out.println("7. Result Analytics");
                System.out.println("8. View All Collections");
                System.out.println("9. Exit");
                System.out.print("Enter choice: ");

                int choice = readInt();

                switch (choice) {
                    case 1 -> studentMenu();
                    case 2 -> facultyMenu();
                    case 3 -> courseMenu();
                    case 4 -> registrationMenu();
                    case 5 -> attendanceMenu();
                    case 6 -> examinationMenu();
                    case 7 -> resultMenu();
                    case 8 -> viewAllCollections();
                    case 9 -> {
                        System.out.println("Exiting program...");
                        return;
                    }
                    default -> System.out.println("Invalid choice!");
                }
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= COMMON METHODS =================

    private static int readInt() {
        while (!sc.hasNextInt()) {
            System.out.print("Enter a valid integer: ");
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
                        LocalDate.parse(value)
                                .atStartOfDay()
                                .toInstant(ZoneOffset.UTC)
                );
            } catch (Exception e) {
                System.out.println("Invalid date. Example: 2026-10-20");
            }
        }
    }

    private static List<String> readList(String prompt) {
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

    private static int readIntPrompt(String prompt) {
        System.out.print(prompt);
        return readInt();
    }

    private static double readDoublePrompt(String prompt) {
        System.out.print(prompt);
        return readDouble();
    }

    private static boolean exists(
            MongoCollection<Document> collection,
            String field,
            String value) {

        return collection.find(Filters.eq(field, value)).first() != null;
    }

    private static void showDocuments(
            MongoCollection<Document> collection,
            Bson filter) {

        boolean found = false;

        for (Document doc : collection.find(filter)) {
            System.out.println(doc.toJson());
            found = true;
        }

        if (!found) {
            System.out.println("No records found.");
        }
    }

    private static void showAll(MongoCollection<Document> collection) {
        showDocuments(collection, new Document());
    }

    // ================= STUDENT MANAGEMENT =================

    private static void studentMenu() {
        while (true) {
            System.out.println("\n--- Student Management ---");
            System.out.println("1. Register Student");
            System.out.println("2. Search Student");
            System.out.println("3. View All Students");
            System.out.println("4. Update Student Email");
            System.out.println("5. Back");

            int choice = readIntPrompt("Enter choice: ");

            switch (choice) {
                case 1 -> registerStudent();
                case 2 -> {
                    String id = readText("Student ID: ");
                    showDocuments(students,
                            Filters.eq("studentId", id));
                }
                case 3 -> showAll(students);
                case 4 -> updateStudentEmail();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void registerStudent() {
        String id = readText("Student ID: ");

        if (id.isBlank() || exists(students, "studentId", id)) {
            System.out.println("Student ID is empty or already exists.");
            return;
        }

        String name = readText("Name: ");
        String email = readText("Email: ");
        String department = readText("Department: ");
        int semester = readIntPrompt("Semester: ");
        String phone = readText("Phone: ");
        int year = readIntPrompt("Enrollment year: ");

        if (semester < 1 || semester > 12) {
            System.out.println("Invalid semester.");
            return;
        }

        Document student = new Document("studentId", id)
                .append("name", name)
                .append("email", email)
                .append("department", department)
                .append("semester", semester)
                .append("phone", phone)
                .append("enrollmentYear", year);

        students.insertOne(student);
        System.out.println("Student registered successfully!");
    }

    private static void updateStudentEmail() {
        String id = readText("Student ID: ");
        String email = readText("New email: ");

        var result = students.updateOne(
                Filters.eq("studentId", id),
                Updates.set("email", email)
        );

        System.out.println(result.getMatchedCount() > 0
                ? "Student email updated."
                : "Student not found.");
    }

    // ================= FACULTY MANAGEMENT =================

    private static void facultyMenu() {
        while (true) {
            System.out.println("\n--- Faculty Management ---");
            System.out.println("1. Add Faculty");
            System.out.println("2. Search Faculty");
            System.out.println("3. View All Faculty");
            System.out.println("4. Back");

            int choice = readIntPrompt("Enter choice: ");

            switch (choice) {
                case 1 -> addFaculty();
                case 2 -> {
                    String id = readText("Faculty ID: ");
                    showDocuments(faculty,
                            Filters.eq("facultyId", id));
                }
                case 3 -> showAll(faculty);
                case 4 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void addFaculty() {
        String id = readText("Faculty ID: ");

        if (id.isBlank() || exists(faculty, "facultyId", id)) {
            System.out.println("Faculty ID is empty or already exists.");
            return;
        }

        String name = readText("Name: ");
        String email = readText("Email: ");
        String department = readText("Department: ");
        String designation = readText("Designation: ");
        List<String> courseIds = readList("Course IDs");

        Document doc = new Document("facultyId", id)
                .append("name", name)
                .append("email", email)
                .append("department", department)
                .append("designation", designation)
                .append("courses", courseIds);

        faculty.insertOne(doc);
        System.out.println("Faculty added successfully!");
    }

    // ================= COURSE MANAGEMENT =================

    private static void courseMenu() {
        while (true) {
            System.out.println("\n--- Course Management ---");
            System.out.println("1. Add Course");
            System.out.println("2. Search Course");
            System.out.println("3. View All Courses");
            System.out.println("4. Back");

            int choice = readIntPrompt("Enter choice: ");

            switch (choice) {
                case 1 -> addCourse();
                case 2 -> {
                    String id = readText("Course ID: ");
                    showDocuments(courses,
                            Filters.eq("courseId", id));
                }
                case 3 -> showAll(courses);
                case 4 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void addCourse() {
        String id = readText("Course ID: ");

        if (id.isBlank() || exists(courses, "courseId", id)) {
            System.out.println("Course ID is empty or already exists.");
            return;
        }

        String name = readText("Course name: ");
        String department = readText("Department: ");
        int semester = readIntPrompt("Semester: ");
        int credits = readIntPrompt("Credits: ");
        String facultyId = readText("Faculty ID: ");

        if (!exists(faculty, "facultyId", facultyId)) {
            System.out.println("Faculty does not exist.");
            return;
        }

        Document doc = new Document("courseId", id)
                .append("courseName", name)
                .append("department", department)
                .append("semester", semester)
                .append("credits", credits)
                .append("facultyId", facultyId);

        courses.insertOne(doc);

        faculty.updateOne(
                Filters.eq("facultyId", facultyId),
                Updates.addToSet("courses", id)
        );

        System.out.println("Course added successfully!");
    }

    // ================= COURSE REGISTRATION =================

    private static void registrationMenu() {
        while (true) {
            System.out.println("\n--- Course Registration ---");
            System.out.println("1. Register Student for Course");
            System.out.println("2. View Registrations");
            System.out.println("3. Search by Student");
            System.out.println("4. Back");

            int choice = readIntPrompt("Enter choice: ");

            switch (choice) {
                case 1 -> registerCourse();
                case 2 -> showAll(registrations);
                case 3 -> {
                    String id = readText("Student ID: ");
                    showDocuments(registrations,
                            Filters.eq("studentId", id));
                }
                case 4 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void registerCourse() {
        String id = readText("Registration ID: ");

        if (id.isBlank()
                || exists(registrations, "registrationId", id)) {
            System.out.println("Registration ID is empty or already exists.");
            return;
        }

        String studentId = readText("Student ID: ");
        String courseId = readText("Course ID: ");

        if (!exists(students, "studentId", studentId)) {
            System.out.println("Student not found.");
            return;
        }

        Document course = courses.find(
                Filters.eq("courseId", courseId)).first();

        if (course == null) {
            System.out.println("Course not found.");
            return;
        }

        if (registrations.find(
                Filters.and(
                        Filters.eq("studentId", studentId),
                        Filters.eq("courseId", courseId)
                )).first() != null) {
            System.out.println("Student is already registered for this course.");
            return;
        }

        Document student = students.find(
                Filters.eq("studentId", studentId)).first();

        if (!course.getString("department")
                .equals(student.getString("department"))) {
            System.out.println(
                    "Warning: Student and course departments differ.");
            String confirm = readText("Continue registration? (yes/no): ");

            if (!confirm.equalsIgnoreCase("yes")) {
                return;
            }
        }

        Document doc = new Document("registrationId", id)
                .append("studentId", studentId)
                .append("courseId", courseId)
                .append("registrationDate", new Date())
                .append("status", "Registered");

        registrations.insertOne(doc);
        System.out.println("Course registration successful!");
    }

    // ================= ATTENDANCE TRACKING =================

    private static void attendanceMenu() {
        while (true) {
            System.out.println("\n--- Attendance Tracking ---");
            System.out.println("1. Record Attendance");
            System.out.println("2. View All Attendance");
            System.out.println("3. View Student Attendance");
            System.out.println("4. Attendance Percentage");
            System.out.println("5. Back");

            int choice = readIntPrompt("Enter choice: ");

            switch (choice) {
                case 1 -> recordAttendance();
                case 2 -> showAll(attendance);
                case 3 -> {
                    String id = readText("Student ID: ");
                    showDocuments(attendance,
                            Filters.eq("studentId", id));
                }
                case 4 -> attendancePercentage();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void recordAttendance() {
        String id = readText("Attendance ID: ");

        if (id.isBlank() || exists(attendance, "attendanceId", id)) {
            System.out.println("Attendance ID is empty or already exists.");
            return;
        }

        String studentId = readText("Student ID: ");
        String courseId = readText("Course ID: ");

        if (!exists(students, "studentId", studentId)
                || !exists(courses, "courseId", courseId)) {
            System.out.println("Student or course not found.");
            return;
        }

        Date date = readDate("Attendance date");

        if (attendance.find(
                Filters.and(
                        Filters.eq("studentId", studentId),
                        Filters.eq("courseId", courseId),
                        Filters.eq("date", date)
                )).first() != null) {
            System.out.println(
                    "Attendance already exists for this student, course and date.");
            return;
        }

        String status = readText(
                "Status (Present/Absent/Late): ");

        if (!Arrays.asList("Present", "Absent", "Late")
                .contains(status)) {
            System.out.println("Invalid attendance status.");
            return;
        }

        Document doc = new Document("attendanceId", id)
                .append("studentId", studentId)
                .append("courseId", courseId)
                .append("date", date)
                .append("status", status);

        attendance.insertOne(doc);
        System.out.println("Attendance recorded successfully!");
    }

    private static void attendancePercentage() {
        String studentId = readText("Student ID: ");
        String courseId = readText("Course ID: ");

        long total = attendance.countDocuments(
                Filters.and(
                        Filters.eq("studentId", studentId),
                        Filters.eq("courseId", courseId)
                ));

        long present = attendance.countDocuments(
                Filters.and(
                        Filters.eq("studentId", studentId),
                        Filters.eq("courseId", courseId),
                        Filters.in("status", "Present", "Late")
                ));

        if (total == 0) {
            System.out.println("No attendance records found.");
            return;
        }

        double percentage = present * 100.0 / total;

        System.out.println("Total classes recorded: " + total);
        System.out.println("Present or late: " + present);
        System.out.printf("Attendance percentage: %.2f%%%n", percentage);
    }

    // ================= EXAMINATION MANAGEMENT =================

    private static void examinationMenu() {
        while (true) {
            System.out.println("\n--- Examination Management ---");
            System.out.println("1. Schedule Examination");
            System.out.println("2. View All Examinations");
            System.out.println("3. Search by Course");
            System.out.println("4. Back");

            int choice = readIntPrompt("Enter choice: ");

            switch (choice) {
                case 1 -> scheduleExamination();
                case 2 -> showAll(examinations);
                case 3 -> {
                    String id = readText("Course ID: ");
                    showDocuments(examinations,
                            Filters.eq("courseId", id));
                }
                case 4 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void scheduleExamination() {
        String id = readText("Exam ID: ");

        if (id.isBlank() || exists(examinations, "examId", id)) {
            System.out.println("Exam ID is empty or already exists.");
            return;
        }

        String courseId = readText("Course ID: ");

        if (!exists(courses, "courseId", courseId)) {
            System.out.println("Course not found.");
            return;
        }

        String name = readText("Exam name: ");
        Date date = readDate("Exam date");
        int maxMarks = readIntPrompt("Maximum marks: ");
        int semester = readIntPrompt("Semester: ");

        if (maxMarks <= 0 || semester <= 0) {
            System.out.println("Marks and semester must be positive.");
            return;
        }

        Document doc = new Document("examId", id)
                .append("courseId", courseId)
                .append("examName", name)
                .append("examDate", date)
                .append("maxMarks", maxMarks)
                .append("semester", semester);

        examinations.insertOne(doc);
        System.out.println("Examination scheduled successfully!");
    }

    // ================= RESULT MANAGEMENT AND ANALYTICS =================

    private static void resultMenu() {
        while (true) {
            System.out.println("\n--- Result Management & Analytics ---");
            System.out.println("1. Enter Student Result");
            System.out.println("2. View All Results");
            System.out.println("3. View Results by Student");
            System.out.println("4. View Result Analytics");
            System.out.println("5. Back");

            int choice = readIntPrompt("Enter choice: ");

            switch (choice) {
                case 1 -> enterResult();
                case 2 -> showAll(results);
                case 3 -> {
                    String id = readText("Student ID: ");
                    showDocuments(results,
                            Filters.eq("studentId", id));
                }
                case 4 -> resultAnalytics();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private static void enterResult() {
        String id = readText("Result ID: ");

        if (id.isBlank() || exists(results, "resultId", id)) {
            System.out.println("Result ID is empty or already exists.");
            return;
        }

        String studentId = readText("Student ID: ");
        String examId = readText("Exam ID: ");

        if (!exists(students, "studentId", studentId)) {
            System.out.println("Student not found.");
            return;
        }

        Document exam = examinations.find(
                Filters.eq("examId", examId)).first();

        if (exam == null) {
            System.out.println("Examination not found.");
            return;
        }

        String courseId = exam.getString("courseId");

        if (registrations.find(
                Filters.and(
                        Filters.eq("studentId", studentId),
                        Filters.eq("courseId", courseId),
                        Filters.eq("status", "Registered")
                )).first() == null) {
            System.out.println(
                    "Student is not registered for this course.");
            return;
        }

        if (results.find(
                Filters.and(
                        Filters.eq("studentId", studentId),
                        Filters.eq("examId", examId)
                )).first() != null) {
            System.out.println(
                    "A result already exists for this student and exam.");
            return;
        }

        int maxMarks = ((Number) exam.get("maxMarks")).intValue();
        int marks = readIntPrompt("Marks obtained: ");

        if (marks < 0 || marks > maxMarks) {
            System.out.println(
                    "Marks must be between 0 and " + maxMarks + ".");
            return;
        }

        double percentage = marks * 100.0 / maxMarks;

        String grade;
        if (percentage >= 90) {
            grade = "A+";
        } else if (percentage >= 80) {
            grade = "A";
        } else if (percentage >= 70) {
            grade = "B";
        } else if (percentage >= 60) {
            grade = "C";
        } else if (percentage >= 40) {
            grade = "D";
        } else {
            grade = "F";
        }

        String resultStatus = percentage >= 40 ? "Pass" : "Fail";

        Document doc = new Document("resultId", id)
                .append("studentId", studentId)
                .append("examId", examId)
                .append("courseId", courseId)
                .append("marksObtained", marks)
                .append("maxMarks", maxMarks)
                .append("grade", grade)
                .append("resultStatus", resultStatus);

        results.insertOne(doc);

        System.out.println("Result saved successfully!");
        System.out.println("Percentage: " + String.format("%.2f", percentage)
                + "%");
        System.out.println("Grade: " + grade);
        System.out.println("Result: " + resultStatus);
    }

    private static void resultAnalytics() {
        long totalResults = results.countDocuments();

        long passed = results.countDocuments(
                Filters.eq("resultStatus", "Pass"));

        long failed = results.countDocuments(
                Filters.eq("resultStatus", "Fail"));

        System.out.println("\n========== RESULT ANALYTICS ==========");
        System.out.println("Total students registered: "
                + students.countDocuments());
        System.out.println("Total faculty: "
                + faculty.countDocuments());
        System.out.println("Total courses: "
                + courses.countDocuments());
        System.out.println("Total course registrations: "
                + registrations.countDocuments());
        System.out.println("Total attendance records: "
                + attendance.countDocuments());
        System.out.println("Total examinations: "
                + examinations.countDocuments());
        System.out.println("Total results entered: " + totalResults);
        System.out.println("Passed results: " + passed);
        System.out.println("Failed results: " + failed);

        if (totalResults > 0) {
            double passRate = passed * 100.0 / totalResults;
            System.out.printf("Pass rate: %.2f%%%n", passRate);
        }

        System.out.println("\n--- Results by Course ---");

        for (Document course : courses.find()) {
            String courseId = course.getString("courseId");

            long courseResults = results.countDocuments(
                    Filters.eq("courseId", courseId));

            long coursePassed = results.countDocuments(
                    Filters.and(
                            Filters.eq("courseId", courseId),
                            Filters.eq("resultStatus", "Pass")
                    ));

            System.out.println(
                    courseId + " - " + course.getString("courseName"));
            System.out.println("Results: " + courseResults
                    + ", Passed: " + coursePassed);
        }
    }

    // ================= VIEW ALL COLLECTIONS =================

    private static void viewAllCollections() {
        System.out.println("\n--- STUDENTS ---");
        showAll(students);

        System.out.println("\n--- FACULTY ---");
        showAll(faculty);

        System.out.println("\n--- COURSES ---");
        showAll(courses);

        System.out.println("\n--- REGISTRATIONS ---");
        showAll(registrations);

        System.out.println("\n--- ATTENDANCE ---");
        showAll(attendance);

        System.out.println("\n--- EXAMINATIONS ---");
        showAll(examinations);

        System.out.println("\n--- RESULTS ---");
        showAll(results);
    }
}
