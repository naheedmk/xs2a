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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for a successful read card account transaction list request. This card account report contains transactions resulting from the query parameters.
 */
@ApiModel(description = "Body of the JSON response for a successful read card account transaction list request. This card account report contains transactions resulting from the query parameters. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-12-26T15:06:21.086+02:00[Europe/Kiev]")

public class CardAccountsTransactionsResponse200   {
  @JsonProperty("cardAccount")
  private AccountReference cardAccount = null;

  @JsonProperty("cardTransactions")
  private CardAccountReport cardTransactions = null;

  @JsonProperty("balances")
  private BalanceList balances = null;

  @JsonProperty("_links")
  private Map _links = null;

  public CardAccountsTransactionsResponse200 cardAccount(AccountReference cardAccount) {
    this.cardAccount = cardAccount;
    return this;
  }

  /**
   * Get cardAccount
   * @return cardAccount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("cardAccount")
  public AccountReference getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(AccountReference cardAccount) {
    this.cardAccount = cardAccount;
  }

  public CardAccountsTransactionsResponse200 cardTransactions(CardAccountReport cardTransactions) {
    this.cardTransactions = cardTransactions;
    return this;
  }

  /**
   * Get cardTransactions
   * @return cardTransactions
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("cardTransactions")
  public CardAccountReport getCardTransactions() {
    return cardTransactions;
  }

  public void setCardTransactions(CardAccountReport cardTransactions) {
    this.cardTransactions = cardTransactions;
  }

  public CardAccountsTransactionsResponse200 balances(BalanceList balances) {
    this.balances = balances;
    return this;
  }

  /**
   * Get balances
   * @return balances
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("balances")
  public BalanceList getBalances() {
    return balances;
  }

  public void setBalances(BalanceList balances) {
    this.balances = balances;
  }

  public CardAccountsTransactionsResponse200 _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    CardAccountsTransactionsResponse200 cardAccountsTransactionsResponse200 = (CardAccountsTransactionsResponse200) o;
    return Objects.equals(this.cardAccount, cardAccountsTransactionsResponse200.cardAccount) &&
    Objects.equals(this.cardTransactions, cardAccountsTransactionsResponse200.cardTransactions) &&
    Objects.equals(this.balances, cardAccountsTransactionsResponse200.balances) &&
    Objects.equals(this._links, cardAccountsTransactionsResponse200._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccount, cardTransactions, balances, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardAccountsTransactionsResponse200 {\n");

    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
    sb.append("    cardTransactions: ").append(toIndentedString(cardTransactions)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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

