/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.network;

import java.security.KeyStore;
import java.security.Security;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rs.etf.teststudent.dto.*;
import rs.etf.teststudent.utils.Configuration;

public class NetworkServicePrivate {

    private static NetworkServicePrivate networkServicePrivate;

    public static NetworkServicePrivate getInstance() {
        if (networkServicePrivate == null) {
            try {
                networkServicePrivate = new NetworkServicePrivate();
            } catch (Exception e) {
                // failed to create secure client
            }
        }
        return networkServicePrivate;
    }

    private NetworkClientPrivate secureClient = null;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private NetworkServicePrivate() throws Exception {
        KeyStore keyStore = NetworkUtils.loadKeyStore();
        Retrofit secureRetrofit = new Retrofit.Builder()
                .baseUrl(Configuration.SERVER_URL)
                .client(NetworkUtils.create(keyStore, Configuration.ENFORCE_TLS))
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        secureClient = secureRetrofit.create(NetworkClientPrivate.class);
    }

    public void login(LoginDataDTO request) throws Exception {
        Response<Void> response = secureClient.login(request).execute();
        NetworkUtils.handleError(response);
    }

    public void createTest(CreateTestDTO request) throws Exception {
        Response<Void> response = secureClient.createTest(request).execute();
        NetworkUtils.handleError(response);
    }

    public void editTest(TestDTO request) throws Exception {
        Response<Void> response = secureClient.editTest(request).execute();
        NetworkUtils.handleError(response);
    }

    public TestsDTO getUnfinishedTests(TestsDTO request) throws Exception {
        Response<TestsDTO> response = secureClient.getUnfinishedTests(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }

    public TestsDTO getFinishedTests(TestsDTO request) throws Exception {
        Response<TestsDTO> response = secureClient.getFinishedTests(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }

    public void deleteTest(TestDTO request) throws Exception {
        Response<Void> response = secureClient.deleteTest(request).execute();
        NetworkUtils.handleError(response);
    }

    public void finishTest(TestDTO request) throws Exception {
        Response<Void> response = secureClient.finishTest(request).execute();
        NetworkUtils.handleError(response);
    }

    public TestInfoDTO getTestInfo(TestDTO request) throws Exception {
        Response<TestInfoDTO> response = secureClient.getTestInfo(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }

    public QuestionDTO updateQuestion(QuestionDTO request) throws Exception {
        Response<QuestionDTO> response = secureClient.updateQuestion(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }

    public void deleteQuestion(QuestionDTO request) throws Exception {
        Response<Void> response = secureClient.deleteQuestion(request).execute();
        NetworkUtils.handleError(response);
    }

    public void sendQuestion(QuestionDTO request) throws Exception {
        Response<Void> response = secureClient.sendQuestion(request).execute();
        NetworkUtils.handleError(response);
    }

    public void finishQuestion(QuestionDTO request) throws Exception {
        Response<Void> response = secureClient.finishQuestion(request).execute();
        NetworkUtils.handleError(response);
    }

    public void createUsers(CreateUsersDTO request) throws Exception {
        Response<Void> response = secureClient.createUsers(request).execute();
        NetworkUtils.handleError(response);
    }

    public GetCoursesDTO getCourses(GetCoursesDTO request) throws Exception {
        Response<GetCoursesDTO> response = secureClient.getCourses(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }

    public void createCourse(CreateCourseDTO request) throws Exception {
        Response<Void> response = secureClient.createCourse(request).execute();
        NetworkUtils.handleError(response);
    }

    public void deleteCourse(DeleteCourseDTO request) throws Exception {
        Response<Void> response = secureClient.deleteCourse(request).execute();
        NetworkUtils.handleError(response);
    }

    public void copyTests(CopyTestsDTO request) throws Exception {
        Response<Void> response = secureClient.copyTests(request).execute();
        NetworkUtils.handleError(response);
    }

    public GetStudentsAndCoursesDTO getStudentsAndCourses() throws Exception {
        Response<GetStudentsAndCoursesDTO> response = secureClient.getStudentsAndCourses().execute();
        NetworkUtils.handleError(response);
        return response.body();
    }

    public GetStudentsFromCourseDTO getStudentsFromCourse(GetStudentsFromCourseDTO request) throws Exception {
        Response<GetStudentsFromCourseDTO> response = secureClient.getStudentsFromCourse(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }

    public void addStudentToCourse(UpdateCourseStudentDTO request) throws Exception {
        Response<Void> response = secureClient.addStudentToCourse(request).execute();
        NetworkUtils.handleError(response);
    }

    public void deleteStudentFromCourse(UpdateCourseStudentDTO request) throws Exception {
        Response<Void> response = secureClient.deleteStudentFromCourse(request).execute();
        NetworkUtils.handleError(response);
    }
}
