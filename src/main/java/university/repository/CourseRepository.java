package university.repository;

import university.model.Course;
import university.model.Enrollment;
import university.model.Student;
import java.util.List;

public interface CourseRepository {
    void saveEnrollment(Enrollment enrollment);
    boolean existsEnrollment(Student student, Course course);
    List<Enrollment> findAllEnrollments();
}