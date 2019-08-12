package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Equals true if sufficient funds are available at the time of the request, false otherwise.  This datalemenet is allways contained in a confirmation of funds response.  This data element is contained in a payment status response, if supported by the ASPSP, if a funds check has been performed and if the transactionStatus is \&quot;ATCT\&quot;, \&quot;ACWC\&quot; or \&quot;ACCP\&quot;. 
 */
@ApiModel(description = "Equals true if sufficient funds are available at the time of the request, false otherwise.  This datalemenet is allways contained in a confirmation of funds response.  This data element is contained in a payment status response, if supported by the ASPSP, if a funds check has been performed and if the transactionStatus is \"ATCT\", \"ACWC\" or \"ACCP\". ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-08-07T16:04:49.625002+03:00[Europe/Kiev]")

public class FundsAvailable   {

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FundsAvailable {\n");
    
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

