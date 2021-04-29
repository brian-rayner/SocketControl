package uk.co.brayner.socketcontrol.octopus;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProductDetails
{
  @SerializedName("code")
  @Expose
  public String code;
  @SerializedName("full_name")
  @Expose
  public String fullName;
  @SerializedName("display_name")
  @Expose
  public String displayName;
  @SerializedName("description")
  @Expose
  public String description;
  @SerializedName("is_variable")
  @Expose
  public Boolean isVariable;
  @SerializedName("is_green")
  @Expose
  public Boolean isGreen;
  @SerializedName("is_tracker")
  @Expose
  public Boolean isTracker;
  @SerializedName("is_prepay")
  @Expose
  public Boolean isPrepay;
  @SerializedName("is_business")
  @Expose
  public Boolean isBusiness;
  @SerializedName("is_restricted")
  @Expose
  public Boolean isRestricted;
  @SerializedName("term")
  @Expose
  public Integer term;
  @SerializedName("available_from")
  @Expose
  public String availableFrom;
  @SerializedName("available_to")
  @Expose
  public Object availableTo;
  @SerializedName("tariffs_active_at")
  @Expose
  public String tariffsActiveAt;
  @SerializedName("single_register_electricity_tariffs")
  @Expose
  public SingleRegisterElectricityTariffs singleRegisterElectricityTariffs;
  @SerializedName("dual_register_electricity_tariffs")
  @Expose
  public DualRegisterElectricityTariffs dualRegisterElectricityTariffs;
  @SerializedName("single_register_gas_tariffs")
  @Expose
  public SingleRegisterGasTariffs singleRegisterGasTariffs;
  @SerializedName("sample_quotes")
  @Expose
  public SampleQuotes sampleQuotes;
  @SerializedName("sample_consumption")
  @Expose
  public SampleConsumption sampleConsumption;
  @SerializedName("links")
  @Expose
  public List<Link> links = null;
  @SerializedName("brand")
  @Expose
  public String brand;

  public static class Link
  {
    @SerializedName("href")
    @Expose
    public String href;
    @SerializedName("method")
    @Expose
    public String method;
    @SerializedName("rel")
    @Expose
    public String rel;
  }

  public static class SingleRegisterElectricityTariffs
  {
    @SerializedName("_A")
    @Expose
    public Tariff a;
    @SerializedName("_B")
    @Expose
    public Tariff b;
    @SerializedName("_C")
    @Expose
    public Tariff c;
    @SerializedName("_D")
    @Expose
    public Tariff d;
    @SerializedName("_E")
    @Expose
    public Tariff e;
    @SerializedName("_F")
    @Expose
    public Tariff f;
    @SerializedName("_G")
    @Expose
    public Tariff g;
    @SerializedName("_H")
    @Expose
    public Tariff h;
    @SerializedName("_J")
    @Expose
    public Tariff j;
    @SerializedName("_K")
    @Expose
    public Tariff k;
    @SerializedName("_L")
    @Expose
    public Tariff l;
    @SerializedName("_M")
    @Expose
    public Tariff m;
    @SerializedName("_N")
    @Expose
    public Tariff n;
    @SerializedName("_P")
    @Expose
    public Tariff p;
  }

  public static class Tariff
  {
    @SerializedName("direct_debit_monthly")
    @Expose
    public DirectDebitMonthly directDebitMonthly;
  }

  public static class DirectDebitMonthly
  {
    @SerializedName("code")
    @Expose
    public String code;
    @SerializedName("standing_charge_exc_vat")
    @Expose
    public Double standingChargeExcVat;
    @SerializedName("standing_charge_inc_vat")
    @Expose
    public Double standingChargeIncVat;
    @SerializedName("online_discount_exc_vat")
    @Expose
    public Integer onlineDiscountExcVat;
    @SerializedName("online_discount_inc_vat")
    @Expose
    public Integer onlineDiscountIncVat;
    @SerializedName("dual_fuel_discount_exc_vat")
    @Expose
    public Integer dualFuelDiscountExcVat;
    @SerializedName("dual_fuel_discount_inc_vat")
    @Expose
    public Integer dualFuelDiscountIncVat;
    @SerializedName("exit_fees_exc_vat")
    @Expose
    public Integer exitFeesExcVat;
    @SerializedName("exit_fees_inc_vat")
    @Expose
    public Integer exitFeesIncVat;
    @SerializedName("links")
    @Expose
    public List<Link> links = null;
    @SerializedName("standard_unit_rate_exc_vat")
    @Expose
    public Double standardUnitRateExcVat;
    @SerializedName("standard_unit_rate_inc_vat")
    @Expose
    public Double standardUnitRateIncVat;
  }

  public static class DualRegisterElectricityTariffs
  {
  }

  public static class SingleRegisterGasTariffs
  {
  }

  public static class SampleQuotes
  {
    @SerializedName("_A")
    @Expose
    public Tariff a;
    @SerializedName("_B")
    @Expose
    public Tariff b;
    @SerializedName("_C")
    @Expose
    public Tariff c;
    @SerializedName("_D")
    @Expose
    public Tariff d;
    @SerializedName("_E")
    @Expose
    public Tariff e;
    @SerializedName("_F")
    @Expose
    public Tariff f;
    @SerializedName("_G")
    @Expose
    public Tariff g;
    @SerializedName("_H")
    @Expose
    public Tariff h;
    @SerializedName("_J")
    @Expose
    public Tariff j;
    @SerializedName("_K")
    @Expose
    public Tariff k;
    @SerializedName("_L")
    @Expose
    public Tariff l;
    @SerializedName("_M")
    @Expose
    public Tariff m;
    @SerializedName("_N")
    @Expose
    public Tariff n;
    @SerializedName("_P")
    @Expose
    public Tariff p;
  }

  public static class SampleConsumption
  {
    @SerializedName("electricity_single_rate")
    @Expose
    public ElectricitySingleRate electricitySingleRate;
    @SerializedName("electricity_dual_rate")
    @Expose
    public ElectricityDualRate electricityDualRate;
    @SerializedName("dual_fuel_single_rate")
    @Expose
    public DualFuelSingleRate dualFuelSingleRate;
    @SerializedName("dual_fuel_dual_rate")
    @Expose
    public DualFuelDualRate dualFuelDualRate;
  }

  public static class ElectricitySingleRate
  {
    @SerializedName("electricity_standard")
    @Expose
    public Integer electricityStandard;
  }

  public static class ElectricityDualRate
  {
    @SerializedName("electricity_day")
    @Expose
    public Integer electricityDay;
    @SerializedName("electricity_night")
    @Expose
    public Integer electricityNight;
  }

  public static class DualFuelSingleRate
  {
    @SerializedName("electricity_standard")
    @Expose
    public Integer electricityStandard;
    @SerializedName("gas_standard")
    @Expose
    public Integer gasStandard;
  }

  public class DualFuelDualRate
  {
    @SerializedName("electricity_day")
    @Expose
    public Integer electricityDay;
    @SerializedName("electricity_night")
    @Expose
    public Integer electricityNight;
    @SerializedName("gas_standard")
    @Expose
    public Integer gasStandard;
  }
}

