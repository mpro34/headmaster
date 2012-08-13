package edu.lmu.cs.headmaster.ws.service;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import edu.lmu.cs.headmaster.ws.dao.StudentDao;
import edu.lmu.cs.headmaster.ws.dao.UserDao;
import edu.lmu.cs.headmaster.ws.domain.Event;
import edu.lmu.cs.headmaster.ws.domain.Student;
import edu.lmu.cs.headmaster.ws.types.ClassYear;

/**
 * The sole implementation of the student service.
 */
@Path("/students")
public class StudentServiceImpl extends AbstractService implements StudentService {

    private StudentDao studentDao;
    
    /**
     * Creates a student service with the injected dao.
     */
    public StudentServiceImpl(UserDao userDao, StudentDao studentDao) {
        super(userDao);
        this.studentDao = studentDao;
    }

    @Override
    public List<Student> getStudents(String query, Boolean active, ClassYear classYear,
            Integer expectedGraduationYearFrom, Integer expectedGraduationYearTo,
            int skip, int max) {
        logServiceCall();

        // classYear is mutually exclusive with (expectedGraduationYearFrom,
        // expectedGraduationYearTo).
        validate(
            (classYear == null && expectedGraduationYearFrom == null &&
                            expectedGraduationYearTo == null) ||
                    (classYear != null && expectedGraduationYearFrom == null &&
                            expectedGraduationYearTo == null) ||
                    (classYear == null && (expectedGraduationYearFrom != null ||
                                expectedGraduationYearTo != null)),
            Response.Status.BAD_REQUEST, ARGUMENT_CONFLICT
        );

        // At least one of query, classYear, expectedGraduationYearFrom, or
        // expectedGraduationYearTo must be set.
        validate(query != null || classYear != null || expectedGraduationYearFrom != null ||
                expectedGraduationYearTo != null, Response.Status.BAD_REQUEST, QUERY_REQUIRED);

        return studentDao.getStudents(
            query != null ? preprocessQuery(query, skip, max, 0, 50) : null,
            active, expectedGraduationYearFrom, expectedGraduationYearTo, skip, max
        );
    }

    @Override
    public Response createStudent(Student student) {
        logServiceCall();
        
        validate(student.getId() == null, Response.Status.BAD_REQUEST, STUDENT_OVERSPECIFIED);

        // Dao problems will filter up as exceptions.
        studentDao.createStudent(student);
        return Response.created(URI.create(Long.toString(student.getId()))).build();
    }

    @Override
    public Response createOrUpdateStudent(Long id, Student student) {
        logServiceCall();
        
        // The student IDs should match.
        validate(id.equals(student.getId()), Response.Status.BAD_REQUEST, STUDENT_INCONSISTENT);
        
        // Dao problems will filter up as exceptions.
        studentDao.createOrUpdateStudent(student);
        return Response.noContent().build();
    }

    @Override
    public Student getStudentById(Long id) {
        logServiceCall();

        Student student = studentDao.getStudentById(id);
        validate(student != null, Response.Status.NOT_FOUND, STUDENT_NOT_FOUND);
        return student;
    }

    @Override
    public List<Event> getStudentAttendanceById(Long id) {
        logServiceCall();

        List<Event> events = studentDao.getStudentAttendanceById(id);
        validate(events != null, Response.Status.NOT_FOUND, STUDENT_NOT_FOUND);
        return events;
    }

}
