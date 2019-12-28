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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.core.payment.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CmsCorePaymentMapper {

    public PaymentInitiationJson mapToPaymentInitiationJson(List<PisPayment> payments) {
        if (payments.isEmpty()) {
            return null;
        }

        PisPayment pisPayment = payments.get(0);
        PaymentInitiationJson payment = new PaymentInitiationJson();
        payment.setCreditorAddress(mapToAddress(pisPayment.getCreditorAddress()));
        payment.setRemittanceInformationStructured(mapToRemittanceInformationStructured(pisPayment.getRemittanceInformationStructured()));
        payment.setCreditorAgent(pisPayment.getCreditorAgent());
        payment.setCreditorName(pisPayment.getCreditorName());
        payment.setCreditorAccount(mapToAccountReference(pisPayment.getCreditorAccount()));
        payment.setDebtorAccount(mapToAccountReference(pisPayment.getDebtorAccount()));
        payment.setEndToEndIdentification(pisPayment.getEndToEndIdentification());
        Amount amount = new Amount();
        amount.setAmount(pisPayment.getAmount().toPlainString());
        amount.setCurrency(pisPayment.getCurrency().getCurrencyCode());
        payment.setInstructedAmount(amount);
        payment.setPurposeCode(PurposeCode.fromValue(pisPayment.getPurposeCode()));
        payment.setRemittanceInformationUnstructured(pisPayment.getRemittanceInformationUnstructured());
        payment.setRequestedExecutionDate(pisPayment.getRequestedExecutionDate());
        payment.setUltimateCreditor(pisPayment.getUltimateCreditor());
        payment.setUltimateDebtor(pisPayment.getUltimateDebtor());

        return payment;
    }

    public BulkPaymentInitiationJson mapToBulkPaymentInitiationJson(List<PisPayment> payments) {
        if (payments.isEmpty()) {
            return null;
        }

        PisPayment pisPayment = payments.get(0);
        BulkPaymentInitiationJson payment = new BulkPaymentInitiationJson();
        payment.setPayments(payments.stream().map(this::mapToPaymentInitiationBulkElementJson).collect(Collectors.toList()));

        //Bulk
        payment.setBatchBookingPreferred(pisPayment.getBatchBookingPreferred());
        payment.setDebtorAccount(mapToAccountReference(pisPayment.getDebtorAccount()));
        payment.setRequestedExecutionDate(pisPayment.getRequestedExecutionDate());
        payment.setRequestedExecutionTime(pisPayment.getRequestedExecutionTime());

        return payment;
    }

    public PeriodicPaymentInitiationJson mapToPeriodicPaymentInitiationJson(List<PisPayment> payments) {
        if (payments.isEmpty()) {
            return null;
        }

        PisPayment pisPayment = payments.get(0);
        PeriodicPaymentInitiationJson payment = new PeriodicPaymentInitiationJson();

        payment.setDebtorAccount(mapToAccountReference(pisPayment.getDebtorAccount()));
        payment.setCreditorAddress(mapToAddress(pisPayment.getCreditorAddress()));
        payment.setRemittanceInformationStructured(mapToRemittanceInformationStructured(pisPayment.getRemittanceInformationStructured()));
        payment.setCreditorAgent(pisPayment.getCreditorAgent());
        payment.setCreditorName(pisPayment.getCreditorName());
        payment.setCreditorAccount(mapToAccountReference(pisPayment.getCreditorAccount()));
        payment.setEndToEndIdentification(pisPayment.getEndToEndIdentification());
        Amount amount = new Amount();
        amount.setAmount(pisPayment.getAmount().toPlainString());
        amount.setCurrency(pisPayment.getCurrency().getCurrencyCode());
        payment.setInstructedAmount(amount);
        payment.setPurposeCode(PurposeCode.fromValue(pisPayment.getPurposeCode()));
        payment.setRemittanceInformationUnstructured(pisPayment.getRemittanceInformationUnstructured());
        payment.setUltimateCreditor(pisPayment.getUltimateCreditor());
        payment.setUltimateDebtor(pisPayment.getUltimateDebtor());

        //Periodic
        payment.setStartDate(pisPayment.getStartDate());
        payment.setEndDate(pisPayment.getEndDate());
        payment.setExecutionRule(ExecutionRule.fromValue(pisPayment.getExecutionRule().getValue()));
        payment.setFrequency(FrequencyCode.valueOf(pisPayment.getFrequency()));
        payment.setDayOfExecution(DayOfExecution.fromValue(pisPayment.getDayOfExecution().getValue()));

        return payment;
    }

    private PaymentInitiationBulkElementJson mapToPaymentInitiationBulkElementJson(PisPayment pisPayment) {
        PaymentInitiationBulkElementJson payment = new PaymentInitiationBulkElementJson();
        payment.setCreditorAddress(mapToAddress(pisPayment.getCreditorAddress()));
        payment.setRemittanceInformationStructured(mapToRemittanceInformationStructured(pisPayment.getRemittanceInformationStructured()));
        payment.setCreditorAgent(pisPayment.getCreditorAgent());
        payment.setCreditorName(pisPayment.getCreditorName());
        payment.setCreditorAccount(mapToAccountReference(pisPayment.getCreditorAccount()));
        payment.setEndToEndIdentification(pisPayment.getEndToEndIdentification());
        Amount amount = new Amount();
        amount.setAmount(pisPayment.getAmount().toPlainString());
        amount.setCurrency(pisPayment.getCurrency().getCurrencyCode());
        payment.setInstructedAmount(amount);
        payment.setPurposeCode(PurposeCode.fromValue(pisPayment.getPurposeCode()));
        payment.setRemittanceInformationUnstructured(pisPayment.getRemittanceInformationUnstructured());
        payment.setUltimateCreditor(pisPayment.getUltimateCreditor());
        payment.setUltimateDebtor(pisPayment.getUltimateDebtor());

        return payment;
    }

    private Address mapToAddress(CmsAddress creditorAddress) {
        if (creditorAddress == null) {
            return null;
        }

        Address address = new Address();
        address.setBuildingNumber(creditorAddress.getBuildingNumber());
        address.setCountry(creditorAddress.getCountry());
        address.setPostCode(creditorAddress.getPostalCode());
        address.setStreetName(creditorAddress.getStreet());
        address.setTownName(creditorAddress.getCity());

        return address;
    }

    private RemittanceInformationStructured mapToRemittanceInformationStructured(CmsRemittance remittanceInformationStructured) {
        if (remittanceInformationStructured == null) {
            return null;
        }
        RemittanceInformationStructured informationStructured = new RemittanceInformationStructured();
        informationStructured.setReference(remittanceInformationStructured.getReference());
        informationStructured.setReferenceIssuer(remittanceInformationStructured.getReferenceIssuer());
        informationStructured.setReferenceType(remittanceInformationStructured.getReferenceType());

        return informationStructured;
    }

    private AccountReference mapToAccountReference(de.adorsys.psd2.xs2a.core.profile.AccountReference reference) {
        if (reference == null) {
            return null;
        }

        AccountReference accountReference = new AccountReference();
        accountReference.setIban(reference.getIban());
        accountReference.setBban(reference.getBban());
        accountReference.setMaskedPan(reference.getMaskedPan());
        accountReference.setMsisdn(reference.getMsisdn());
        accountReference.setPan(reference.getPan());
        accountReference.setCurrency(reference.getCurrency().getCurrencyCode());

        return accountReference;
    }
}

