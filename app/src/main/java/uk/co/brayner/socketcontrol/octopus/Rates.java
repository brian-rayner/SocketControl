package uk.co.brayner.socketcontrol.octopus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Rates
{
  @SerializedName("count")
  @Expose
  public Integer count;
  @SerializedName("next")
  @Expose
  public String next;
  @SerializedName("previous")
  @Expose
  public Object previous;
  @SerializedName("results")
  @Expose
  public List<Result> results = null;
  
  public static class Result
  {
    @SerializedName("value_exc_vat")
    @Expose
    public Double valueExcVat;
    @SerializedName("value_inc_vat")
    @Expose
    public Double valueIncVat;
    @SerializedName("valid_from")
    @Expose
    public Date validFrom;
    @SerializedName("valid_to")
    @Expose
    public Date validTo;
  }
}
