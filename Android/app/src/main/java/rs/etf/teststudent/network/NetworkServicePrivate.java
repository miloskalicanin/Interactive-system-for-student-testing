/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.network;

import android.content.Context;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyStore;
import java.security.Security;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rs.etf.teststudent.Configuration;
import rs.etf.teststudent.dto.ClassCheckInDTO;
import rs.etf.teststudent.dto.GetSentQuestionsDTO;
import rs.etf.teststudent.dto.LoginDataDTO;
import rs.etf.teststudent.dto.UserAnswerDTO;
import rs.etf.teststudent.dto.UserInfoDTO;

public class NetworkServicePrivate {

    private static NetworkServicePrivate networkServicePrivate;

    public static NetworkServicePrivate getInstance(Context context) {
        if (networkServicePrivate == null) {
            try {
                networkServicePrivate = new NetworkServicePrivate(context);
            } catch (Exception e) {
                // failed to create secure client
            }
        }
        return networkServicePrivate;
    }

    private NetworkClientPrivate secureClient = null;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private NetworkServicePrivate(Context context) throws Exception {
        KeyStore keyStore = NetworkUtils.loadKeyStore(context);
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

    public UserInfoDTO userInfo(String email) throws Exception {
        UserInfoDTO request = new UserInfoDTO();
        request.setEmail(email);
        Response<UserInfoDTO> response = secureClient.userInfo(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }

    public void classCheckIn(ClassCheckInDTO request) throws Exception {
        Response<Void> response = secureClient.classCheckIn(request).execute();
        NetworkUtils.handleError(response);
    }

    public void addAnswer(UserAnswerDTO request) throws Exception {
        Response<Void> response = secureClient.addAnswer(request).execute();
        NetworkUtils.handleError(response);
    }

    public GetSentQuestionsDTO getSentQuestions(GetSentQuestionsDTO request) throws Exception {
        Response<GetSentQuestionsDTO> response = secureClient.getSentQuestions(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }
}
