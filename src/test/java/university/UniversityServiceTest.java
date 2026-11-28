package university;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import university.model.Course;
import university.model.Enrollment;
import university.model.Student;
import university.repository.CourseRepository;
import university.repository.InMemoryCourseRepository;
import university.service.UniversityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UniversityServiceTest {

    private CourseRepository repository;
    private UniversityService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCourseRepository();
        service = new UniversityService(repository);
    }

    @Test
    void testEnrollStudentSuccess() {
        Student student = new Student("S1", "Иван Иванов");
        Course course = new Course("C1", "Java Разработка");

        Enrollment enrollment = service.enrollStudent(student, course);

        assertNotNull(enrollment);
        assertEquals(student, enrollment.getStudent());
        assertEquals(course, enrollment.getCourse());
        assertTrue(repository.existsEnrollment(student, course));
    }

    @Test
    void testEnrollStudentDuplicateThrowsException() {
        Student student = new Student("S1", "Иван Иванов");
        Course course = new Course("C1", "Java Разработка");

        service.enrollStudent(student, course);



        assertThrows(
            IllegalArgumentException.class,
            () -> service.enrollStudent(student, course)
        );
    }

    @Test
    void testMultipleStudentsCanEnrollInSameCourse() {
        Student student1 = new Student("S3", "Вася ВАсильев");
        Student student2 = new Student("S2", "Петр Петров");
        Course course = new Course("C1", "Java Разработка");

        service.enrollStudent(student1, course);

        assertDoesNotThrow(() -> service.enrollStudent(student2, course));
        assertEquals(2, repository.findAllEnrollments().size());
    }

    @Test
    void testGetCoursesForStudent() {
        Student student = new Student("S1", "Иван Иванов");
        Course course1 = new Course("C1", "Java Разработка");
        Course course2 = new Course("C2", "Математическое программирование");

        service.enrollStudent(student, course1);
        service.enrollStudent(student, course2);

        List<Course> studentCourses = service.getCoursesForStudent(student);

        assertEquals(2, studentCourses.size());
        assertTrue(studentCourses.contains(course1));
        assertTrue(studentCourses.contains(course2));
    }

    @Test
    void testMetricsUpdateAcrossMultipleOperations() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        UniversityService meteredService = new UniversityService(repository, registry);

        Student student = new Student("S7", "Иван Иванов");
        Course course1 = new Course("C1", "Java Разработка");
        Course course2 = new Course("C2", "Математическое программирование");

        assertEquals(0.0, registry.get("university_enrollments_total").counter().count());
        assertEquals(0.0, registry.get("university_enrollments_success_total").counter().count());
        assertEquals(0.0, registry.get("university_enrollments_duplicate_total").counter().count());

        // Операция 1: успешная запись
        meteredService.enrollStudent(student, course1);
        // Операция 2: еще одна успешная запись
        meteredService.enrollStudent(student, course2);
        // Операция 3: повторная запись (ошибочный/дублирующий сценарий)
        assertThrows(
            IllegalArgumentException.class,
            () -> meteredService.enrollStudent(student, course1)
        );

        assertEquals(3.0, registry.get("university_enrollments_total").counter().count());
        assertEquals(2.0, registry.get("university_enrollments_success_total").counter().count());
        assertEquals(1.0, registry.get("university_enrollments_duplicate_total").counter().count());
        assertEquals(1.0, registry.get("university_enrollments_errors_total").counter().count());
    }
}