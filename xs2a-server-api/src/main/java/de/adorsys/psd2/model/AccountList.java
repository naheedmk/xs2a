/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.AccountDetails;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * List of accounts with details. 
 */
@ApiModel(description = "List of accounts with details. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-18T12:38:01.509+03:00[Europe/Kiev]")

public class AccountList   {
  @JsonProperty("accounts")
  @Valid
  private List<AccountDetails> accounts = new ArrayList<>();

  public AccountList accounts(List<AccountDetails> accounts) {
    this.accounts = accounts;
    return this;
  }

  public AccountList addAccountsItem(AccountDetails accountsItem) {
    this.accounts.add(accountsItem);
    return this;
  }

  /**
   * Get accounts
   * @return accounts
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("accounts")
  public List<AccountDetails> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<AccountDetails> accounts) {
    this.accounts = accounts;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountList accountList = (AccountList) o;
    return Objects.equals(this.accounts, accountList.accounts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accounts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountList {\n");
    
    sb.append("    accounts: ").append(toIndentedString(accounts)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

