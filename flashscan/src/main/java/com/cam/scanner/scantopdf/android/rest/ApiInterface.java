package com.cam.scanner.scantopdf.android.rest;

import com.cam.scanner.scantopdf.android.rest.request.RequestOrder;
import com.cam.scanner.scantopdf.android.rest.request.RequestRegister;
import com.cam.scanner.scantopdf.android.rest.request.RequestUpdateCredits;
import com.cam.scanner.scantopdf.android.rest.request.RequestUpdateEmail;
import com.cam.scanner.scantopdf.android.rest.response.GetPlans;
import com.cam.scanner.scantopdf.android.rest.response.Order;
import com.cam.scanner.scantopdf.android.rest.response.Register;
import com.cam.scanner.scantopdf.android.rest.response.UpdateCredits;
import com.cam.scanner.scantopdf.android.rest.response.UpdateEmail;
import com.cam.scanner.scantopdf.android.util.Constants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {
    @GET(Constants.URL_GET_PLANS)
    Call<GetPlans> getPlans();

    @POST(Constants.URL_REGISTER)
    Call<Register> register(@Body RequestRegister requestRegister);

    @POST(Constants.URL_UPDATE_CREDIT)
    Call<UpdateCredits> updateCredits(@Body RequestUpdateCredits requestUpdateCredits);

    @POST(Constants.URL_UPDATE_Email)
    Call<UpdateEmail> updateEmail(@Body RequestUpdateEmail requestUpdateEmail);

    @POST(Constants.URL_ORDER)
    Call<Order> order(@Body RequestOrder requestOrder);
}
