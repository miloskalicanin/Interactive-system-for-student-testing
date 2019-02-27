package rs.etf.teststudent.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rs.etf.teststudent.dto.ClassCheckInDTO;
import rs.etf.teststudent.dto.GetSentQuestionsDTO;
import rs.etf.teststudent.dto.LoginDataDTO;
import rs.etf.teststudent.dto.UserAnswerDTO;
import rs.etf.teststudent.dto.UserInfoDTO;

public interface NetworkClientPrivate {

    @POST("/private/login")
    Call<Void> login(@Body LoginDataDTO request);

    @POST("/private/user-info")
    Call<UserInfoDTO> userInfo(@Body UserInfoDTO request);

    @POST("/private/class-check-in")
    Call<Void> classCheckIn(@Body ClassCheckInDTO request);

    @POST("/private/add-answer")
    Call<Void> addAnswer(@Body UserAnswerDTO request);

    @POST("/private/get-sent-questions")
    Call<GetSentQuestionsDTO> getSentQuestions(@Body GetSentQuestionsDTO request);
}
