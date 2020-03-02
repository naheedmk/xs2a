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

import com.google.common.base.Charsets;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.event.persist.EventReportRepository;
import de.adorsys.psd2.event.persist.EventRepository;
import de.adorsys.psd2.event.persist.model.EventPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.report.EventReportRepositoryImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@DataJpaTest
class EventReportRepositoryImplIT {
    private static final String INSTANCE_ID = "3de76f19-1df7-44d8-b760-ca972d2f945c";
    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String PAYMENT_ID = "j-t4XyLJTzQkonfSTnyxIMc";
    private static final byte[] PAYLOAD = "payload".getBytes(Charsets.UTF_8);
    private static final OffsetDateTime CREATED_DATETIME = OffsetDateTime.now();

    @Autowired
    private EventReportRepository repository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    private JsonReader jsonReader = new JsonReader();
    private EventPO eventPO;
    private OffsetDateTime start;
    private OffsetDateTime end;

    @BeforeEach
    void setUp() {
        eventPO = jsonReader.getObjectFromFile("json/event.json", EventPO.class);
        eventPO.setTimestamp(CREATED_DATETIME);
        eventPO.setPayload(PAYLOAD);

        start = CREATED_DATETIME.minusHours(1);
        end = CREATED_DATETIME.plusHours(1);

        populateWithInitialData();
    }

    private void populateWithInitialData() {
        eventRepository.save(eventPO);
        entityManager.flush();
    }

    @Test
    void getEventsForPeriod() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriod(start, end, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEvents(eventPO, eventsForPeriod.get(0));
    }

    @Test
    void getEventsForPeriodAndConsentId() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndConsentId(start, end, CONSENT_ID, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEvents(eventPO, eventsForPeriod.get(0));
    }

    @Test
    void getEventsForPeriodAndPaymentId() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndPaymentId(start, end, PAYMENT_ID, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEvents(eventPO, eventsForPeriod.get(0));
    }

    @Test
    void getEventsForPeriodAndEventOrigin() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndEventOrigin(start, end, EventOrigin.TPP, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEvents(eventPO, eventsForPeriod.get(0));
    }

    @Test
    void getEventsForPeriodAndEventType() {
        List<ReportEvent> eventsForPeriod = repository.getEventsForPeriodAndEventType(start, end, EventType.PAYMENT_INITIATION_REQUEST_RECEIVED, INSTANCE_ID);
        assertNotNull(eventsForPeriod);
        assertEquals(1, eventsForPeriod.size());
        assertEvents(eventPO, eventsForPeriod.get(0));
    }

    private void assertEvents(EventPO eventPO, ReportEvent reportEvent) {
        assertEquals(eventPO.getTimestamp(), reportEvent.getTimestamp());
        assertEquals(eventPO.getConsentId(), reportEvent.getConsentId());
        assertEquals(eventPO.getPaymentId(), reportEvent.getPaymentId());
        assertEquals(new String(eventPO.getPayload()), new String(reportEvent.getPayload()));
        assertEquals(eventPO.getEventOrigin(), reportEvent.getEventOrigin());
        assertEquals(eventPO.getEventType(), reportEvent.getEventType());
        assertEquals(eventPO.getInstanceId(), reportEvent.getInstanceId());
        assertEquals(eventPO.getTppAuthorisationNumber(), reportEvent.getTppAuthorisationNumber());
        assertEquals(eventPO.getXRequestId(), reportEvent.getXRequestId());
        assertEquals(eventPO.getPsuIdData(), reportEvent.getPsuIdData().iterator().next());
        assertEquals(eventPO.getInternalRequestId(), reportEvent.getInternalRequestId());
    }
}
