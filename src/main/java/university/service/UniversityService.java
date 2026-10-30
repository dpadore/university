package university.service;

import university.model.Course;
import university.model.Enrollment;
import university.model.Student;
import university.repository.CourseRepository;

public class UniversityService {
    
    private final CourseRepository repository;

    public UniversityService(CourseRepository repository) {
        this.repository = repository;
    }

    public Enrollment enrollStudent(Student student, Course course) {
        if (repository.existsEnrollment(student, course)) {
            throw new IllegalArgumentException(
                "Предупреждение: Студент " + student.getName() + " уже записан на курс " + course.getTitle()
            );
        }

        Enrollment enrollment = new Enrollment(student, course);
        repository.saveEnrollment(enrollment);
        return enrollment;
    }

    public java.util.List<Course> getCoursesForStudent(Student student) {
    return repository.findAllEnrollments().stream()
            .filter(e -> e.getStudent().getId().equals(student.getId()))
            .map(Enrollment::getCourse)
            .toList();
}
}