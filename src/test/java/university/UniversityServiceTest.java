package university;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import university.model.Course;
import university.model.Enrollment;
import university.model.Student;
import university.repository.CourseRepository;
import university.repository.InMemoryCourseRepository;
import university.service.UniversityService;

import static org.junit.jupiter.api.Assertions.*;

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
        Student student1 = new Student("S1", "Иван Иванов");
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
    Course course2 = new Course("C1", "Математическое программирование");

    service.enrollStudent(student, course1);
    service.enrollStudent(student, course2);

    var studentCourses = service.getCoursesForStudent(student);
    
    assertEquals(2, studentCourses.size());
    assertTrue(studentCourses.contains(course1));
    assertTrue(studentCourses.contains(course2));

}
}