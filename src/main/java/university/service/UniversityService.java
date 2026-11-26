package university.service;

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

    public UniversityService(CourseRepository repository) {
        this.repository = repository;
    }

    public Enrollment enrollStudent(Student student, Course course) {
        if (student == null || course == null) {
            logger.error("Ошибка операции записи: передан пустой объект студента или дисциплины");
            throw new IllegalArgumentException("Студент и дисциплина не могут быть null");
        }

        if (repository.existsEnrollment(student, course)) {
            logger.warn("Студент '{}' уже записан на дисциплину '{}'",
                    student.getName(), course.getTitle());
            throw new IllegalArgumentException(
                    "Студент " + student.getName() + " уже записан на курс " + course.getTitle()
            );
        }

        Enrollment enrollment = new Enrollment(student, course);
        
        repository.saveEnrollment(enrollment);
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