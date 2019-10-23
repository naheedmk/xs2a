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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;

/**
 * Implementations of this interface contain the business logic, needed to perform embedded and decoupled SCA
 */
public interface AuthorisationProcessorService {

    /**
     * Updates authorisation in the CMS after each successful authorisation step execution
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @param response the result object, containing the successful result of authorisation or the error data
     */
    void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response);

    /**
     * Contains business logic to perform at the `received` SCA status of authorisation
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error data
     */
    AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `psuIdentified` SCA status of authorisation
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error data
     */
    AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `psuAuthenticated` SCA status of authorisation
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error data
     */
    AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `scaMethodSelected` SCA status of authorisation
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error data
     */
    AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `started` SCA status of authorisation
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error data
     */
    AuthorisationProcessorResponse doScaStarted(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `finalised` SCA status of authorisation
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error data
     */
    AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `failed` SCA status of authorisation
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error data
     */
    AuthorisationProcessorResponse doScaFailed(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `exempted` SCA status of authorisation
     *
     * @param request the request object, containing controller incoming data and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error data
     */
    AuthorisationProcessorResponse doScaExempted(AuthorisationProcessorRequest request);
}
