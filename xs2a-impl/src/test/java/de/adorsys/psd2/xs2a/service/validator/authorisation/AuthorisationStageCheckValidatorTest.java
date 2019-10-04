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

package de.adorsys.psd2.xs2a.service.validator.authorisation;

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthorisationStageCheckValidatorTest {
    private static final String TEST_PASSWORD = "123";
    private static final String TEST_AUTH_METHOD_ID = "SMS";
    private static final String TEST_AUTH_DATA = "123456";
    private static final ScaStatus RECEIVED_STATUS = ScaStatus.RECEIVED;
    private static final ScaStatus PSUIDENTIFIED_STATUS = ScaStatus.PSUIDENTIFIED;
    private static final ScaStatus PSUAUTHENTICATED_STATUS = ScaStatus.PSUAUTHENTICATED;
    private static final ScaStatus SCAMETHODSELECTED_STATUS = ScaStatus.SCAMETHODSELECTED;
    private static final ScaStatus FINALISED_STATUS = ScaStatus.FINALISED;
    private static final AuthorisationServiceType PIS_AUTHORISATION = AuthorisationServiceType.PIS;

    private AuthorisationStageCheckValidator checkValidator;

    @Before
    public void setUp() {
        checkValidator = new AuthorisationStageCheckValidator();
    }

    @Test
    public void test_received_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setPassword(TEST_PASSWORD);

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, RECEIVED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isValid());
    }

    @Test
    public void test_received_failure_noPassword() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, RECEIVED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isNotValid());
    }

    @Test
    public void test_psuIdentified_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setPassword(TEST_PASSWORD);

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, PSUIDENTIFIED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isValid());
    }

    @Test
    public void test_psuIdentified_failure_noPassword() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, PSUIDENTIFIED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isNotValid());
    }

    @Test
    public void test_psuAuthenticated_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setAuthenticationMethodId(TEST_AUTH_METHOD_ID);

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, PSUAUTHENTICATED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isValid());
    }

    @Test
    public void test_psuAuthenticated_failure_noAuthenticationMethodId() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, PSUAUTHENTICATED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isNotValid());
    }

    @Test
    public void test_scaMethodSelected_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();
        updateRequest.setScaAuthenticationData(TEST_AUTH_DATA);

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, SCAMETHODSELECTED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isValid());
    }

    @Test
    public void test_scaMethodSelected_failure_noScaAuthenticationData() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, SCAMETHODSELECTED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isNotValid());
    }

    @Test
    public void test_finalised_success() {
        //Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildPisUpdateRequest();

        //When
        ValidationResult actualResult = checkValidator.validate(updateRequest, FINALISED_STATUS, PIS_AUTHORISATION);

        //Then
        assertTrue(actualResult.isValid());
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildPisUpdateRequest() {
        return new Xs2aUpdatePisCommonPaymentPsuDataRequest();
    }
}
