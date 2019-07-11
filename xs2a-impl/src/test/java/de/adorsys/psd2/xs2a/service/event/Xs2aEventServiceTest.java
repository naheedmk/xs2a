/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.event;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.service.Xs2aEventServiceEncrypted;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.event.service.model.PsuIdDataBO;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.RequestData;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.event.mapper.EventMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aEventServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String PAYMENT_ID = "0795805d-651b-4e00-88fb-a34248337bbd";
    private static final String URI = "/v1/consents";
    private static final UUID REQUEST_ID = UUID.fromString("0d7f200e-09b4-46f5-85bd-f4ea89fccace");
    private static final String TPP_IP = "1.2.3.4";
    private static final EventType EVENT_TYPE = EventType.PAYMENT_INITIATION_REQUEST_RECEIVED;
    private static final String AUTHORISATION_NUMBER = "999";
    private static PsuIdDataBO PSU_ID_DATA;

    @InjectMocks
    private Xs2aEventService xs2aEventService;

    @Mock
    private TppService tppService;
    @Mock
    private Xs2aEventServiceEncrypted eventService;
    @Mock
    private RequestProviderService requestProviderService;
    @Spy
    private EventMapper eventMapper = Mappers.getMapper(EventMapper.class);

    @Captor
    private ArgumentCaptor<EventBO> eventCaptor;

    @Before
    public void setUp() {
        PSU_ID_DATA = buildPsuIdData();
        when(eventService.recordEvent(eventCaptor.capture())).thenReturn(true);
        when(requestProviderService.getRequestData()).thenReturn(buildRequestData());
        when(tppService.getTppInfo()).thenReturn(buildTppInfo());
    }

    @Test
    public void recordAisTppRequest_Success() {
        // Given

        // When
        xs2aEventService.recordAisTppRequest(CONSENT_ID, EVENT_TYPE, null);

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(PSU_ID_DATA);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(REQUEST_ID);
    }

    @Test
    public void recordPisTppRequest_Success() {
        // Given

        // When
        xs2aEventService.recordPisTppRequest(PAYMENT_ID, EVENT_TYPE, null);

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(PSU_ID_DATA);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(REQUEST_ID);
    }

    @Test
    public void recordTppRequest_Success() {
        // Given

        // When
        xs2aEventService.recordTppRequest(EVENT_TYPE, null);

        // Then
        verify(eventService, times(1)).recordEvent(any(EventBO.class));
        EventBO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTimestamp()).isNotNull();
        assertThat(capturedEvent.getEventOrigin()).isEqualTo(EventOrigin.TPP);
        assertThat(capturedEvent.getEventType()).isEqualTo(EVENT_TYPE);
        assertThat(capturedEvent.getPayload()).isNotNull();
        assertThat(capturedEvent.getPsuIdData()).isEqualTo(PSU_ID_DATA);
        assertThat(capturedEvent.getTppAuthorisationNumber()).isEqualTo(AUTHORISATION_NUMBER);
        assertThat(capturedEvent.getXRequestId()).isEqualTo(REQUEST_ID);
    }

    private RequestData buildRequestData() {
        return new RequestData(URI, REQUEST_ID, TPP_IP, Collections.emptyMap(), new de.adorsys.psd2.xs2a.core.psu.PsuIdData("ID",
                                                                                                                            "TYPE",
                                                                                                                            "CORPORATE_ID",
                                                                                                                            "CORPORATE_ID_TYPE"));
    }

    private PsuIdDataBO buildPsuIdData() {
        return new PsuIdDataBO("ID",
                             "TYPE",
                             "CORPORATE_ID",
                             "CORPORATE_ID_TYPE");
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(AUTHORISATION_NUMBER);
        return tppInfo;
    }
}
