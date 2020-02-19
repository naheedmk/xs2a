// TODO: 19.02.2020 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
///*
// * Copyright 2018-2020 adorsys GmbH & Co KG
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package de.adorsys.psd2.xs2a.service.validator.ais.account.common;
//
//import de.adorsys.psd2.core.data.AccountAccess;
//import de.adorsys.psd2.core.data.ais.AisConsent;
//import de.adorsys.psd2.core.data.ais.AisConsentData;
//import de.adorsys.psd2.xs2a.core.authorisation.AccountConsentAuthorization;
//import de.adorsys.psd2.xs2a.core.error.ErrorType;
//import de.adorsys.psd2.xs2a.core.profile.AccountReference;
//import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
//import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
//import de.adorsys.xs2a.reader.JsonReader;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.Collections;
//import java.util.Currency;
//import java.util.List;
//
//import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
//import static org.junit.jupiter.api.Assertions.*;
//
//class PermittedAccountReferenceValidatorTest {
//    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
//
//    private static final String IBAN = "DE89370400440532013000";
//    private static final String BBAN = "89370400440532013000";
//    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
//    private final static String AUTHORISATION_ID = "authorisation ID";
//    private static final String ACCOUNT_ID = "11111-999999999";
//    private static final String WRONG_ACCOUNT_ID = "wrong_account_id";
//    private PermittedAccountReferenceValidator validator;
//    private JsonReader jsonReader = new JsonReader();
//    private AisConsent aisConsent = buildAccountConsent();
//
//    @BeforeEach
//    void setUp() {
//        validator = new PermittedAccountReferenceValidator(new AccountReferenceAccessValidator());
//    }
//
//    @Test
//    void validate_withBalanceIsTrue_success() {
//        ValidationResult validationResult = validator.validate(aisConsent, ACCOUNT_ID, true);
//        assertTrue(validationResult.isValid());
//    }
//
//    @Test
//    void validate_withBalanceIsFalse_success() {
//        ValidationResult validationResult = validator.validate(aisConsent, ACCOUNT_ID, false);
//        assertTrue(validationResult.isValid());
//    }
//
//    @Test
//    void validate_withBalanceIsTrue_notValidByAccountId() {
//        ValidationResult validationResult = validator.validate(aisConsent, WRONG_ACCOUNT_ID, true);
//
//        assertFalse(validationResult.isValid());
//        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
//        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
//    }
//
//    @Test
//    void validate_withBalanceIsFalse_notValidByAccountId() {
//        ValidationResult validationResult = validator.validate(aisConsent, WRONG_ACCOUNT_ID, false);
//
//        assertFalse(validationResult.isValid());
//        assertEquals(ErrorType.AIS_401, validationResult.getMessageError().getErrorType());
//        assertEquals(CONSENT_INVALID, validationResult.getMessageError().getTppMessage().getMessageErrorCode());
//    }
//
//    private AisConsent buildAccountConsent() {
//        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
//
//        AccountAccess accountAccess = createAccountAccess();
//        AisConsentData consentData = new AisConsentData(accountAccess, accountAccess, false);
//        aisConsent.setConsentData(consentData);
//        aisConsent.setAuthorisations(buildAuthorization());
//        return aisConsent;
//    }
//
//    private static AccountAccess createAccountAccess() {
//        AccountReference accountReference = buildIbanAccountReference();
//        return new AccountAccess(Collections.singletonList(accountReference),
//                                 Collections.singletonList(accountReference),
//                                 Collections.singletonList(accountReference),
//                                 null,
//                                 null,
//                                 null,
//                                 null);
//    }
//
//    private static AccountReference buildIbanAccountReference() {
//        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, null, null, null, EUR_CURRENCY);
//    }
//
//    private List<AccountConsentAuthorization> buildAuthorization() {
//        AccountConsentAuthorization authorization = new AccountConsentAuthorization();
//        authorization.setId(AUTHORISATION_ID);
//        authorization.setScaStatus(ScaStatus.RECEIVED);
//        return Collections.singletonList(authorization);
//    }
//}
