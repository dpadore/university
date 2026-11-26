package university.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import university.model.Course;
import university.model.Enrollment;
import university.model.Student;
import university.repository.CourseRepository;

import java.util.List;

public class UniversityService {

    private static final Logger logger = LoggerFactory.getLogger(UniversityService.class);
    private final CourseRepository repository;
    private final Counter enrollmentsTotalCounter;
    private final Counter enrollmentsSuccessCounter;
    private final Counter enrollmentsDuplicateCounter;
    private final Counter enrollmentsErrorCounter;

    public UniversityService(CourseRepository repository) {
        this(repository, new SimpleMeterRegistry());
    }

    public UniversityService(CourseRepository repository, MeterRegistry registry) {
        this.repository = repository;
        this.enrollmentsTotalCounter = Counter.builder("university_enrollments_total")
                .description("Total number of student course enrollment attempts")
                .register(registry);
        this.enrollmentsSuccessCounter = Counter.builder("university_enrollments_success_total")
                .description("Number of successful student course enrollments")
                .register(registry);
        this.enrollmentsDuplicateCounter = Counter.builder("university_enrollments_duplicate_total")
                .description("Number of duplicate course enrollment attempts")
                .register(registry);
        this.enrollmentsErrorCounter = Counter.builder("university_enrollments_errors_total")
                .description("Number of failed or duplicate enrollment operations")
                .register(registry);
    }

    public Enrollment enrollStudent(Student student, Course course) {
        enrollmentsTotalCounter.increment();

        if (student == null || course == null) {
            enrollmentsErrorCounter.increment();
            logger.error("Ошибка операции записи: передан пустой объект студента или дисциплины");
            throw new IllegalArgumentException("Студент и дисциплина не могут быть null");
        }

        if (repository.existsEnrollment(student, course)) {
            enrollmentsDuplicateCounter.increment();
            enrollmentsErrorCounter.increment();
            logger.warn("Студент '{}' уже записан на дисциплину '{}'",
                    student.getName(), course.getTitle());
            throw new IllegalArgumentException(
                    "Студент " + student.getName() + " уже записан на курс " + course.getTitle()
            );
        }

        Enrollment enrollment = new Enrollment(student, course);

        repository.saveEnrollment(enrollment);
        enrollmentsSuccessCounter.increment();
        logger.info("Студент '{}' успешно записан на дисциплину '{}'",
                student.getName(), course.getTitle());

        return enrollment;
    }

    public List<Course> getCoursesForStudent(Student student) {
        return repository.findAllEnrollments().stream()
                .filter(e -> e.getStudent().getId().equals(student.getId()))
                .map(Enrollment::getCourse)
                .toList();
    }
}