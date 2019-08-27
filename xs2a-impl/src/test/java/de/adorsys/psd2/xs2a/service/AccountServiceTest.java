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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.service.ais.AccountDetailsService;
import de.adorsys.psd2.xs2a.service.ais.AccountListService;
import de.adorsys.psd2.xs2a.service.ais.BalanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {
    private static final String CONSENT_ID = "Test consentId";
    private static final boolean WITH_BALANCE = true;
    private static final String REQUEST_URI = "request/uri";
    private static final String ACCOUNT_ID = UUID.randomUUID().toString();

    @InjectMocks
    private AccountService accountService;

    @Mock
    private BalanceService balanceService;
    @Mock
    private AccountListService accountListService;
    @Mock
    private AccountDetailsService accountDetailsService;

    @Test
    public void getAccountList_Success() {
        // When
        accountService.getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);
        // Then
        verify(accountListService, times(1)).getAccountList(CONSENT_ID, WITH_BALANCE, REQUEST_URI);
    }

    @Test
    public void getAccountDetails_Success() {
        // When
        accountService.getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);
        // Then
        verify(accountDetailsService, times(1)).getAccountDetails(CONSENT_ID, ACCOUNT_ID, WITH_BALANCE, REQUEST_URI);
    }

    @Test
    public void getBalancesReport_Success() {
        // When
        accountService.getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);
        // Then
        verify(balanceService, times(1)).getBalancesReport(CONSENT_ID, ACCOUNT_ID, REQUEST_URI);
    }
}
