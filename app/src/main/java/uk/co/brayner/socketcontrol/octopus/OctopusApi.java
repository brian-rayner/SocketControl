package uk.co.brayner.socketcontrol.octopus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OctopusApi
{
  @GET("v1/products/{product_code}/electricity-tariffs/{tariff_code}/standard-unit-rates")
  Call<Rates> getRates(@Path("product_code") String product_code, @Path("tariff_code") String tariffCode, @Query("period_from") String from, @Query("period_to") String to);
}
