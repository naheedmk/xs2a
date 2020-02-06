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

package de.adorsys.psd2.consent.repository.impl;

import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingFactory;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisConsentRepositoryImplTest {
    private static final String CORRECT_PSU_ID = "987654321";
    private static final byte[] CHECKSUM = "checksum in consent".getBytes();

    private PsuData psuData;
    private PsuIdData psuIdData;
    private AisConsent aisConsent;
    private ConsentEntity consentEntity;

    @InjectMocks
    private AisConsentRepositoryImpl aisConsentVerifyingRepository;
    @Mock
    private ConsentJpaRepository aisConsentRepository;
    @Mock
    private ChecksumCalculatingFactory calculatingFactory;
    @Mock
    private AisConsentMapper aisConsentMapper;
    @Mock
    private AuthorisationRepository authorisationRepository;

    @Mock
    private ChecksumCalculatingService checksumCalculatingService;

    @BeforeEach
    void setUp() {
        psuData = buildPsuData(CORRECT_PSU_ID);
        psuIdData = buildPsuIdData(CORRECT_PSU_ID);

        consentEntity = buildConsentEntity(ConsentStatus.VALID);
        aisConsent = buildConsent(ConsentStatus.VALID);
    }

    @Test
    void verifyAndSave_ReceivedToValidStatus_success() throws WrongChecksumException {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS))).thenReturn(Optional.of(checksumCalculatingService));
        ConsentEntity previousConsentEntity = buildConsentEntity(ConsentStatus.RECEIVED);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(previousConsentEntity));
        when(aisConsentRepository.save(consentEntity)).thenReturn(consentEntity);

        // When
        ConsentEntity actualResult = aisConsentVerifyingRepository.verifyAndSave(consentEntity);

        // Then
        assertEquals(consentEntity, actualResult);
        verify(aisConsentRepository, times(1)).save(consentEntity);
    }

    @Test
    void verifyAndSave_finalisedStatus_failedSha() {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS))).thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any())).thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(false);
        consentEntity.setConsentStatus(ConsentStatus.REJECTED);
        consentEntity.setChecksum(CHECKSUM);

        // When
        assertThrows(WrongChecksumException.class, () -> aisConsentVerifyingRepository.verifyAndSave(consentEntity));

        // Then
        verify(aisConsentRepository, times(0)).save(consentEntity);
    }

    @Test
    void verifyAndSave_failedSha() {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS))).thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any())).thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(false);
        consentEntity.setChecksum(CHECKSUM);

        // When
        assertThrows(WrongChecksumException.class, () -> aisConsentVerifyingRepository.verifyAndSave(consentEntity));

        // Then
        verify(aisConsentRepository, times(0)).save(consentEntity);
    }

    @Test
    void verifyAndSave_correctSha() throws WrongChecksumException {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS))).thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any())).thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(consentEntity)).thenReturn(consentEntity);
        consentEntity.setChecksum(CHECKSUM);

        // When
        ConsentEntity actualResult = aisConsentVerifyingRepository.verifyAndSave(consentEntity);

        // Then
        assertEquals(consentEntity, actualResult);
        verify(aisConsentRepository, times(1)).save(consentEntity);
    }

    @Test
    void verifyAndUpdate_success() throws WrongChecksumException {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS))).thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any())).thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(consentEntity)).thenReturn(consentEntity);
        consentEntity.setChecksum(CHECKSUM);

        // When
        ConsentEntity actualResult = aisConsentVerifyingRepository.verifyAndUpdate(consentEntity);

        // Then
        assertEquals(consentEntity, actualResult);
        verify(aisConsentRepository, times(1)).save(consentEntity);
    }

    @Test
    void verifyAndUpdate_failedSha() {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS))).thenReturn(Optional.of(checksumCalculatingService));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any())).thenReturn(aisConsent);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(consentEntity));
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(false);
        consentEntity.setChecksum(CHECKSUM);

        // When
        assertThrows(WrongChecksumException.class, () -> aisConsentVerifyingRepository.verifyAndUpdate(consentEntity));

        // Then
        verify(aisConsentRepository, times(0)).save(consentEntity);
    }

    @Test
    void verifyAndSaveAll_success() throws WrongChecksumException {
        // Given
        when(calculatingFactory.getServiceByChecksum(any(), eq(ConsentType.AIS))).thenReturn(Optional.of(checksumCalculatingService));
        List<ConsentEntity> asList = Collections.singletonList(consentEntity);
        when(checksumCalculatingService.verifyConsentWithChecksum(aisConsent, CHECKSUM)).thenReturn(true);
        when(aisConsentRepository.save(consentEntity)).thenReturn(consentEntity);
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(consentEntity));
        when(aisConsentMapper.mapToAisConsent(eq(consentEntity), any())).thenReturn(aisConsent);
        consentEntity.setChecksum(CHECKSUM);

        // When
        List<ConsentEntity> actualResult = aisConsentVerifyingRepository.verifyAndSaveAll(asList);

        // Then
        assertEquals(asList, actualResult);
        verify(aisConsentRepository, times(1)).save(consentEntity);
    }

    @Test
    void getActualAisConsent_success(){
        // Given
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(consentEntity));

        // When
        Optional<ConsentEntity> optionalConsentEntity = aisConsentVerifyingRepository.getActualAisConsent(consentEntity.getExternalId());

        assertEquals(Optional.of(consentEntity), optionalConsentEntity);
    }

    @Test
    void getActualAisConsent_empty(){
        consentEntity.setConsentStatus(ConsentStatus.REJECTED);
        // Given
        when(aisConsentRepository.findByExternalId(consentEntity.getExternalId())).thenReturn(Optional.of(consentEntity));

        // When
        Optional<ConsentEntity> optionalConsentEntity = aisConsentVerifyingRepository.getActualAisConsent(consentEntity.getExternalId());

        assertEquals(Optional.empty(), optionalConsentEntity);
    }

    private ConsentEntity buildConsentEntity(ConsentStatus currentStatus) {
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setId(1L);
        consentEntity.setExternalId("1");
        consentEntity.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        consentEntity.setValidUntil(LocalDate.now().plusDays(1));
        consentEntity.setLastActionDate(LocalDate.now());
        consentEntity.setPsuDataList(Collections.singletonList(psuData));
        consentEntity.setConsentStatus(currentStatus);
        consentEntity.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return consentEntity;
    }

    private AisConsent buildConsent(ConsentStatus currentStatus) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId("1");
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        aisConsent.setValidUntil(LocalDate.now().plusDays(1));
        aisConsent.setLastActionDate(LocalDate.now());
        aisConsent.setPsuIdDataList(Collections.singletonList(psuIdData));
        aisConsent.setConsentStatus(currentStatus);
        aisConsent.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return aisConsent;
    }

    private PsuData buildPsuData(String psuId) {
        return new PsuData(psuId, "", "", "", "");
    }

    private PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, "", "", "", "");
    }
}
