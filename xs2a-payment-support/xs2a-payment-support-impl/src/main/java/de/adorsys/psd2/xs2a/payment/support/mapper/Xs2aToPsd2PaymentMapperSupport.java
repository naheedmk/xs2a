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

package de.adorsys.psd2.xs2a.payment.support.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Xs2aToPsd2PaymentMapperSupport {
    public PaymentInitiationJson mapToPaymentInitiationJson(SinglePayment singlePayment) {
        if (singlePayment == null) {
            return null;
        }

        PaymentInitiationJson payment = new PaymentInitiationJson();
        payment.setCreditorAddress(mapToAddress(singlePayment.getCreditorAddress()));
        payment.setRemittanceInformationStructured(mapToRemittanceInformationStructured(singlePayment.getRemittanceInformationStructured()));
        payment.setCreditorAgent(singlePayment.getCreditorAgent());
        payment.setCreditorName(singlePayment.getCreditorName());
        payment.setCreditorAccount(mapToAccountReference(singlePayment.getCreditorAccount()));
        payment.setDebtorAccount(mapToAccountReference(singlePayment.getDebtorAccount()));
        payment.setEndToEndIdentification(singlePayment.getEndToEndIdentification());
        Amount amount = new Amount();
        amount.setAmount(singlePayment.getInstructedAmount().getAmount());
        amount.setCurrency(singlePayment.getInstructedAmount().getCurrency().getCurrencyCode());
        payment.setInstructedAmount(amount);
        payment.setPurposeCode(mapToPurposeCode(singlePayment.getPurposeCode()));
        payment.setRemittanceInformationUnstructured(singlePayment.getRemittanceInformationUnstructured());
        payment.setRequestedExecutionDate(singlePayment.getRequestedExecutionDate());
        payment.setUltimateCreditor(singlePayment.getUltimateCreditor());
        payment.setUltimateDebtor(singlePayment.getUltimateDebtor());

        return payment;
    }

    private PurposeCode mapToPurposeCode(de.adorsys.psd2.xs2a.core.pis.PurposeCode xs2aPurposeCode) {
        if (xs2aPurposeCode == null) {
            return null;
        }
        return PurposeCode.fromValue(xs2aPurposeCode.toString());
    }

    public BulkPaymentInitiationJson mapToBulkPaymentInitiationJson(BulkPayment xs2aBulkPayment) {
        if (xs2aBulkPayment == null) {
            return null;
        }

        BulkPaymentInitiationJson payment = new BulkPaymentInitiationJson();
        List<SinglePayment> payments = xs2aBulkPayment.getPayments();
        payment.setPayments(payments.stream().map(this::mapToPaymentInitiationBulkElementJson).collect(Collectors.toList()));

        //Bulk
        payment.setBatchBookingPreferred(xs2aBulkPayment.getBatchBookingPreferred());
        payment.setDebtorAccount(mapToAccountReference(xs2aBulkPayment.getDebtorAccount()));
        payment.setRequestedExecutionDate(xs2aBulkPayment.getRequestedExecutionDate());
        payment.setRequestedExecutionTime(xs2aBulkPayment.getRequestedExecutionTime());

        return payment;
    }

    public PeriodicPaymentInitiationJson mapToPeriodicPaymentInitiationJson(PeriodicPayment xs2aPeriodicPayment) {
        if (xs2aPeriodicPayment == null) {
            return null;
        }

        PeriodicPaymentInitiationJson payment = new PeriodicPaymentInitiationJson();

        payment.setDebtorAccount(mapToAccountReference(xs2aPeriodicPayment.getDebtorAccount()));
        payment.setCreditorAddress(mapToAddress(xs2aPeriodicPayment.getCreditorAddress()));
        payment.setRemittanceInformationStructured(mapToRemittanceInformationStructured(xs2aPeriodicPayment.getRemittanceInformationStructured()));
        payment.setCreditorAgent(xs2aPeriodicPayment.getCreditorAgent());
        payment.setCreditorName(xs2aPeriodicPayment.getCreditorName());
        payment.setCreditorAccount(mapToAccountReference(xs2aPeriodicPayment.getCreditorAccount()));
        payment.setEndToEndIdentification(xs2aPeriodicPayment.getEndToEndIdentification());
        payment.setInstructedAmount(mapToAmount(xs2aPeriodicPayment.getInstructedAmount()));
        payment.setPurposeCode(mapToPurposeCode(xs2aPeriodicPayment.getPurposeCode()));
        payment.setRemittanceInformationUnstructured(xs2aPeriodicPayment.getRemittanceInformationUnstructured());
        payment.setUltimateCreditor(xs2aPeriodicPayment.getUltimateCreditor());
        payment.setUltimateDebtor(xs2aPeriodicPayment.getUltimateDebtor());

        //Periodic
        payment.setStartDate(xs2aPeriodicPayment.getStartDate());
        payment.setEndDate(xs2aPeriodicPayment.getEndDate());
        payment.setExecutionRule(ExecutionRule.fromValue(xs2aPeriodicPayment.getExecutionRule().getValue()));
        payment.setFrequency(mapToFrequencyCode(xs2aPeriodicPayment.getFrequency()));
        payment.setDayOfExecution(DayOfExecution.fromValue(xs2aPeriodicPayment.getDayOfExecution().getValue()));

        return payment;
    }

    private PaymentInitiationBulkElementJson mapToPaymentInitiationBulkElementJson(SinglePayment singlePayment) {
        PaymentInitiationBulkElementJson payment = new PaymentInitiationBulkElementJson();
        payment.setCreditorAddress(mapToAddress(singlePayment.getCreditorAddress()));
        payment.setRemittanceInformationStructured(mapToRemittanceInformationStructured(singlePayment.getRemittanceInformationStructured()));
        payment.setCreditorAgent(singlePayment.getCreditorAgent());
        payment.setCreditorName(singlePayment.getCreditorName());
        payment.setCreditorAccount(mapToAccountReference(singlePayment.getCreditorAccount()));
        payment.setEndToEndIdentification(singlePayment.getEndToEndIdentification());
        payment.setInstructedAmount(mapToAmount(singlePayment.getInstructedAmount()));
        payment.setPurposeCode(mapToPurposeCode(singlePayment.getPurposeCode()));
        payment.setRemittanceInformationUnstructured(singlePayment.getRemittanceInformationUnstructured());
        payment.setUltimateCreditor(singlePayment.getUltimateCreditor());
        payment.setUltimateDebtor(singlePayment.getUltimateDebtor());

        return payment;
    }

    private Address mapToAddress(Xs2aAddress xs2aAddress) {
        if (xs2aAddress == null) {
            return null;
        }

        Address address = new Address();
        address.setBuildingNumber(xs2aAddress.getBuildingNumber());
        address.setCountry(xs2aAddress.getCountry().getCode());
        address.setPostCode(xs2aAddress.getPostCode());
        address.setStreetName(xs2aAddress.getStreetName());
        address.setTownName(xs2aAddress.getTownName());

        return address;
    }

    private RemittanceInformationStructured mapToRemittanceInformationStructured(Remittance remittance) {
        if (remittance == null) {
            return null;
        }
        RemittanceInformationStructured informationStructured = new RemittanceInformationStructured();
        informationStructured.setReference(remittance.getReference());
        informationStructured.setReferenceIssuer(remittance.getReferenceIssuer());
        informationStructured.setReferenceType(remittance.getReferenceType());

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

    private Amount mapToAmount(Xs2aAmount xs2aAmount) {
        if (xs2aAmount == null) {
            return null;
        }

        Amount amount = new Amount();
        amount.setAmount(xs2aAmount.getAmount());
        amount.setCurrency(xs2aAmount.getCurrency().getCurrencyCode());
        return amount;
    }

    private FrequencyCode mapToFrequencyCode(de.adorsys.psd2.xs2a.core.pis.FrequencyCode xs2aFrequency) {
        if (xs2aFrequency == null) {
            return null;
        }

        return FrequencyCode.fromValue(xs2aFrequency.toString());
    }
}
