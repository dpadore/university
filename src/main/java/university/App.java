package university;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import university.model.Course;
import university.model.Student;
import university.repository.CourseRepository;
import university.repository.InMemoryCourseRepository;
import university.service.UniversityService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final int PORT = 8081;
    private static final int HTTP_OK = 200;

    public static void main(String[] args) throws IOException {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        CourseRepository repository = new InMemoryCourseRepository();
        UniversityService service = new UniversityService(repository, registry);

        Student defaultStudent = new Student("S1", "Иван Иванов");
        Course defaultCourse = new Course("C1", "Java Разработка");

        System.out.println("Запуск системы Университета...");
        service.enrollStudent(defaultStudent, defaultCourse);
        System.out.println("Студент " + defaultStudent.getName()
                + " успешно записан на курс " + defaultCourse.getTitle() + "!");

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/metrics", exchange -> {
            String body = registry.scrape();
            sendResponse(exchange, HTTP_OK, "text/plain; version=0.0.4; charset=utf-8", body);
        });

        server.createContext("/enroll", exchange -> {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
            String studentName = params.getOrDefault("student", "Иван Иванов");
            String courseTitle = params.getOrDefault("course", "Java Разработка");
            String studentId = "STU_" + Integer.toHexString(studentName.hashCode());
            String courseId = "CRS_" + Integer.toHexString(courseTitle.hashCode());

            Student student = new Student(studentId, studentName);
            Course course = new Course(courseId, courseTitle);

            String responseText;
            try {
                service.enrollStudent(student, course);
                responseText = "OK: студент '" + studentName + "' записан на '" + courseTitle + "'";
            } catch (IllegalArgumentException ex) {
                responseText = "ERROR/DUPLICATE: " + ex.getMessage();
            }

            sendResponse(exchange, HTTP_OK, "text/plain; charset=utf-8", responseText);
        });

        server.createContext("/error-test", exchange -> {
            String responseText;
            try {
                service.enrollStudent(defaultStudent, defaultCourse);
                responseText = "Unexpected success";
            } catch (IllegalArgumentException ex) {
                responseText = "Recorded duplicate/error operation: " + ex.getMessage();
            }
            sendResponse(exchange, HTTP_OK, "text/plain; charset=utf-8", responseText);
        });

        server.start();
        System.out.println("HTTP-сервер запущен на http://localhost:" + PORT);
        System.out.println("Метрики доступны по адресу: http://localhost:" + PORT + "/metrics");
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String contentType,
            String body
    ) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return map;
        }
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }
}