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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.authorization.processor.*;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.AisAuthorisationProcessorServiceImpl;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.AuthorisationProcessorService;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.PisAuthorisationProcessorServiceImpl;
import de.adorsys.psd2.xs2a.service.authorization.processor.service.PisCancellationAuthorisationProcessorServiceImpl;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthorisationChainResponsibilityService {

    private ApplicationContext applicationContext;

    private AuthorisationProcessor receivedAuthorisationProcessor;

    @Autowired
    public AuthorisationChainResponsibilityService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        initAuthorisationChains();
    }

    public AuthorisationProcessorResponse process(AuthorisationProcessorRequest request) {
        AuthorisationProcessorResponse processorResponse = receivedAuthorisationProcessor.process(request);

        //update authorisation
        getProcessorService(request).updateAuthorisation(request, processorResponse);
        return processorResponse;
    }

    private AuthorisationProcessorService getProcessorService(AuthorisationProcessorRequest request) {
        if (request.getServiceType() == ServiceType.AIS) {
            return applicationContext.getBean(AisAuthorisationProcessorServiceImpl.class);
        } else if (request.getServiceType() == ServiceType.PIS &&
                       request.getPaymentAuthorisationType() == PaymentAuthorisationType.CREATED) {
            return applicationContext.getBean(PisAuthorisationProcessorServiceImpl.class);
        } else if (request.getServiceType() == ServiceType.PIS &&
                       request.getPaymentAuthorisationType() == PaymentAuthorisationType.CANCELLED) {
            return applicationContext.getBean(PisCancellationAuthorisationProcessorServiceImpl.class);
        }
        throw new IllegalArgumentException("Authorisation processor service is unknown: " + request);
    }

    private void initAuthorisationChains() {
        receivedAuthorisationProcessor = new ReceivedAuthorisationProcessor();
        AuthorisationProcessor psuIdentifiedAuthorisationProcessor = new PsuIdentifiedAuthorisationProcessor();
        AuthorisationProcessor psuAuthenticatedAuthorisationProcessor = new PsuAuthenticatedAuthorisationProcessor();
        AuthorisationProcessor scaMethodSelectedAuthorisationProcessor = new ScaMethodSelectedAuthorisationProcessor();
        AuthorisationProcessor startedAuthorisationProcessor = new StartedAuthorisationProcessor();
        AuthorisationProcessor finalisedAuthorisationProcessor = new FinalisedAuthorisationProcessor();
        AuthorisationProcessor failedAuthorisationProcessor = new FailedAuthorisationProcessor();
        AuthorisationProcessor exemptedAuthorisationProcessor = new ExemptedAuthorisationProcessor();

        receivedAuthorisationProcessor.setNext(psuIdentifiedAuthorisationProcessor);
        psuIdentifiedAuthorisationProcessor.setNext(psuAuthenticatedAuthorisationProcessor);
        psuAuthenticatedAuthorisationProcessor.setNext(scaMethodSelectedAuthorisationProcessor);
        scaMethodSelectedAuthorisationProcessor.setNext(startedAuthorisationProcessor);
        startedAuthorisationProcessor.setNext(finalisedAuthorisationProcessor);
        finalisedAuthorisationProcessor.setNext(failedAuthorisationProcessor);
        failedAuthorisationProcessor.setNext(exemptedAuthorisationProcessor);
    }
}
