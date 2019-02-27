package rs.etf.teststudent.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rs.etf.teststudent.dto.CertRequestDTO;
import rs.etf.teststudent.dto.CertResponseDTO;

public interface NetworkClientPublic {

    @GET("/public/version")
    Call<ResponseBody> getVersion();

    @POST("/public/get-certificate")
    Call<CertResponseDTO> register(@Body CertRequestDTO request);
}
