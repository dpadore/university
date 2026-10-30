package university;

import university.model.Course;
import university.model.Student;
import university.repository.CourseRepository;
import university.repository.InMemoryCourseRepository;
import university.service.UniversityService;

public class App {
    public static void main(String[] args) {
        CourseRepository repository = new InMemoryCourseRepository();
        UniversityService service = new UniversityService(repository);

        Student student = new Student("S1", "Иван Иванов");
        Course course = new Course("C1", "Java Разработка");

        System.out.println("Запуск системы Университета...");
        service.enrollStudent(student, course);
        System.out.println("Студент " + student.getName() + " успешно записан на курс " + course.getTitle() + "!");
    }
}