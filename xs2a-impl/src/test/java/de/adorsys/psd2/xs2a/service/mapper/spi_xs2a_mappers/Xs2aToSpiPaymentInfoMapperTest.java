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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aToSpiPaymentInfoMapperTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PSU_ID_1 = "First";
    private static final String PSU_ID_2 = "Second";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final byte [] PAYMENT_DATA = PAYMENT_ID.getBytes();
    private static final List<PsuIdData> psuDataList = new ArrayList<>();
    private static final List<SpiPsuData> spiPsuDataList = new ArrayList<>();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.of(LocalDate.now(),
                                                                                    LocalTime.NOON,
                                                                                    ZoneOffset.UTC);

    @InjectMocks
    private Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    @Before
    public void setUp() {
        psuDataList.addAll(Arrays.asList(buildPsu(PSU_ID_1), buildPsu(PSU_ID_2)));
        spiPsuDataList.addAll(Arrays.asList(buildSpiPsu(PSU_ID_1), buildSpiPsu(PSU_ID_2)));
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(psuDataList))
            .thenReturn(spiPsuDataList);
    }

    @Test
    public void mapToSpiPaymentInfoSuccess() {
        //Given
        CommonPayment commonPayment = buildCommonPayment();
        //When
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPayment);
        //Then
        assertEquals(PAYMENT_ID, spiPaymentInfo.getPaymentId());
        assertEquals(PAYMENT_PRODUCT, spiPaymentInfo.getPaymentProduct());
        assertEquals(PaymentType.SINGLE, spiPaymentInfo.getPaymentType());
        assertEquals(TRANSACTION_STATUS, spiPaymentInfo.getStatus());
        assertEquals(PAYMENT_DATA, spiPaymentInfo.getPaymentData());
        assertEquals(spiPsuDataList, spiPaymentInfo.getPsuDataList());
        assertEquals(STATUS_CHANGE_TIMESTAMP, spiPaymentInfo.getStatusChangeTimestamp());
        assertEquals(commonPayment.getCreationTimestamp(), spiPaymentInfo.getCreationTimestamp());
    }

    @Test
    public void mapToSpiPaymentInfo_CommonPaymentData_Success() {
        //Given
        CommonPaymentData commonPaymentData = buildCommonPaymentData();
        //When
        SpiPaymentInfo spiPaymentInfo = xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo(commonPaymentData);
        //Then
        assertEquals(commonPaymentData.getExternalId(), spiPaymentInfo.getPaymentId());
        assertEquals(commonPaymentData.getPaymentType(), spiPaymentInfo.getPaymentType());
        assertEquals(commonPaymentData.getTransactionStatus(), spiPaymentInfo.getPaymentStatus());
        assertEquals(commonPaymentData.getPaymentData(), spiPaymentInfo.getPaymentData());
        assertEquals(spiPsuDataList, spiPaymentInfo.getPsuDataList());
        assertEquals(commonPaymentData.getCreationTimestamp(), spiPaymentInfo.getCreationTimestamp());
        assertEquals(commonPaymentData.getStatusChangeTimestamp(), spiPaymentInfo.getStatusChangeTimestamp());
    }

    @Test
    public void xs2aToSpiPaymentInfo_mapToSpiPaymentRequest() {
        //Given
        CommonPayment commonPayment = buildCommonPayment();
        //When
        SpiPaymentInfo spiPaymentInfo = new Xs2aToSpiPaymentInfo().mapToSpiPaymentRequest(commonPayment, PAYMENT_PRODUCT);
        //Then
        assertEquals(PAYMENT_ID, spiPaymentInfo.getPaymentId());
        assertEquals(PAYMENT_PRODUCT, spiPaymentInfo.getPaymentProduct());
        assertEquals(PaymentType.SINGLE, spiPaymentInfo.getPaymentType());
        assertEquals(PAYMENT_DATA, spiPaymentInfo.getPaymentData());
        assertEquals(STATUS_CHANGE_TIMESTAMP, spiPaymentInfo.getStatusChangeTimestamp());
        assertEquals(commonPayment.getCreationTimestamp(), spiPaymentInfo.getCreationTimestamp());
    }

    private CommonPaymentData buildCommonPaymentData() {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setExternalId(UUID.randomUUID().toString());
        pisCommonPaymentResponse.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentResponse.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentResponse.setPaymentData("test".getBytes());
        pisCommonPaymentResponse.setPsuData(psuDataList);
        pisCommonPaymentResponse.setCreationTimestamp(OffsetDateTime.now());
        pisCommonPaymentResponse.setStatusChangeTimestamp(OffsetDateTime.now().minusMinutes(1));
        return pisCommonPaymentResponse;
    }


    private CommonPayment buildCommonPayment() {
        CommonPayment commonPayment = new CommonPayment();
        commonPayment.setPaymentId(PAYMENT_ID);
        commonPayment.setPaymentProduct(PAYMENT_PRODUCT);
        commonPayment.setPaymentType(PaymentType.SINGLE);
        commonPayment.setTransactionStatus(TRANSACTION_STATUS);
        commonPayment.setPaymentData(PAYMENT_DATA);
        commonPayment.setPsuDataList(psuDataList);
        commonPayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        commonPayment.setCreationTimestamp(OffsetDateTime.now());
        return commonPayment;
    }

    private PsuIdData buildPsu(String psuId) {
        return new PsuIdData(psuId, null, null, null);
    }

    private SpiPsuData buildSpiPsu(String psuId) {
        return new SpiPsuData(psuId, null, null, null, null);
    }
}

