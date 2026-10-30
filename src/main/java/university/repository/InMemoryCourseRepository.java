package university.repository;

import university.model.Course;
import university.model.Enrollment;
import university.model.Student;
import java.util.ArrayList;
import java.util.List;

public class InMemoryCourseRepository implements CourseRepository {
    private final List<Enrollment> enrollments = new ArrayList<>();

    @Override
    public void saveEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    @Override
    public boolean existsEnrollment(Student student, Course course) {
        return enrollments.stream()
                .anyMatch(e -> e.getStudent().equals(student) && e.getCourse().equals(course));
    }

    @Override
    public List<Enrollment> findAllEnrollments() {
        return new ArrayList<>(enrollments);
    }
}