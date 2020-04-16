/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.integration;

import org.springframework.web.util.UriComponentsBuilder;

public class UrlBuilder {

    public static String getConsentsByTppUrl(String tppId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/ais/consents/tpp/{tpp-id}")
                   .buildAndExpand(tppId)
                   .toUriString();
    }

    public static String getConsentsByPsuUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/ais/consents/psu")
                   .toUriString();
    }

    public static String getConsentsByAccountUrl(String accountId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/ais/consents/account/{account-id}")
                   .buildAndExpand(accountId)
                   .toUriString();
    }

    public static String getEventsForDatesUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/events/")
                   .toUriString();
    }

    public static String createPiisConsentUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents")
                   .toUriString();
    }

    public static String getPiisConsentsByPsuUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents")
                   .toUriString();
    }

    public static String getPiisTerminateConsentUrl(String consentId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents/{consent-id}")
                   .buildAndExpand(consentId)
                   .toUriString();
    }

    public static String getPiisConsentsByTppUrl(String tppId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents/tpp/{tpp-id}")
                   .buildAndExpand(tppId)
                   .toUriString();
    }

    public static String getPiisConsentsByPsuUrl2() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents/psu")
                   .toUriString();
    }

    public static String getPiisConsentsByAccountUrl(String accountId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/piis/consents/account/{account-id}")
                   .buildAndExpand(accountId)
                   .toUriString();
    }

    public static String getPaymentsByTppUrl(String tppId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/pis/payments/tpp/{tpp-id}")
                   .buildAndExpand(tppId)
                   .toUriString();
    }

    public static String getPaymentsByPsuUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/pis/payments/psu")
                   .toUriString();
    }

    public static String getPaymentsByAccountUrl(String accountId) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/pis/payments/account/{account-id}")
                   .buildAndExpand(accountId)
                   .toUriString();
    }

    public static String updatePaymentStatusUrl(String paymentId, String status) {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/pis/transaction-status/{payment-id}/status/{status}")
                   .buildAndExpand(paymentId, status)
                   .toUriString();
    }

    public static String closeAllConsentsUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/psu/consent/all")
                   .toUriString();
    }

    public static String getTppStopListRecordUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/tpp/stop-list")
                   .toUriString();
    }

    public static String blockTppUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/tpp/stop-list/block")
                   .toUriString();
    }

    public static String unblockTppUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/tpp/stop-list/unblock")
                   .toUriString();
    }

    public static String getTppInfoUrl() {
        return UriComponentsBuilder.fromPath("/aspsp-api/v1/tpp")
                   .toUriString();
    }
}
