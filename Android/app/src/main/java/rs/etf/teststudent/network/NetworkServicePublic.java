/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.teststudent.network;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rs.etf.teststudent.Configuration;
import rs.etf.teststudent.dto.CertRequestDTO;
import rs.etf.teststudent.dto.CertResponseDTO;

public class NetworkServicePublic {

    private static NetworkServicePublic networkServicePublic;

    public static NetworkServicePublic getInstance() {
        if (networkServicePublic == null) {
            networkServicePublic = new NetworkServicePublic();
        }
        return networkServicePublic;
    }

    private NetworkClientPublic publicClient = null;

    private NetworkServicePublic() {
        Retrofit baseRetrofit = new Retrofit.Builder()
                .baseUrl(Configuration.SERVER_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        publicClient = baseRetrofit.create(NetworkClientPublic.class);
    }

    public String getVersion() throws Exception {
        Response<ResponseBody> response = publicClient.getVersion().execute();
        NetworkUtils.handleError(response);
        return response.body().string();
    }

    public CertResponseDTO register(CertRequestDTO request) throws Exception {
        Response<CertResponseDTO> response = publicClient.register(request).execute();
        NetworkUtils.handleError(response);
        return response.body();
    }
}
