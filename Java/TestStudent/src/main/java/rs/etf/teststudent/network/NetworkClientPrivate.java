package rs.etf.teststudent.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rs.etf.teststudent.dto.*;

public interface NetworkClientPrivate {

    @POST("/private/login")
    Call<Void> login(@Body LoginDataDTO request);

    @POST("/private/create-test")
    Call<Void> createTest(@Body CreateTestDTO request);

    @POST("/private/edit-test")
    Call<Void> editTest(@Body TestDTO request);

    @POST("/private/get-unfinished-tests")
    Call<TestsDTO> getUnfinishedTests(@Body TestsDTO request);

    @POST("/private/get-finished-tests")
    Call<TestsDTO> getFinishedTests(@Body TestsDTO request);

    @POST("/private/delete-test")
    Call<Void> deleteTest(@Body TestDTO request);

    @POST("/private/finish-test")
    Call<Void> finishTest(@Body TestDTO request);

    @POST("/private/get-test-info")
    Call<TestInfoDTO> getTestInfo(@Body TestDTO request);

    @POST("/private/update-question")
    Call<QuestionDTO> updateQuestion(@Body QuestionDTO request);

    @POST("/private/delete-question")
    Call<Void> deleteQuestion(@Body QuestionDTO request);

    @POST("/private/send-guestion")
    Call<Void> sendQuestion(@Body QuestionDTO request);

    @POST("/private/finish-guestion")
    Call<Void> finishQuestion(@Body QuestionDTO request);

    @POST("/private/create-users")
    Call<Void> createUsers(@Body CreateUsersDTO request);

    @POST("/private/get-courses")
    Call<GetCoursesDTO> getCourses(@Body GetCoursesDTO request);

    @POST("/private/create-course")
    Call<Void> createCourse(@Body CreateCourseDTO request);

    @POST("/private/delete-course")
    Call<Void> deleteCourse(@Body DeleteCourseDTO request);

    @POST("/private/copy-tests")
    Call<Void> copyTests(@Body CopyTestsDTO request);

    @GET("/private/get-students-and-courses")
    Call<GetStudentsAndCoursesDTO> getStudentsAndCourses();

    @POST("/private/get-students-from-course")
    Call<GetStudentsFromCourseDTO> getStudentsFromCourse(@Body GetStudentsFromCourseDTO request);

    @POST("/private/add-student-to-course")
    Call<Void> addStudentToCourse(@Body UpdateCourseStudentDTO request);

    @POST("/private/delete-student-from-course")
    Call<Void> deleteStudentFromCourse(@Body UpdateCourseStudentDTO request);
}
