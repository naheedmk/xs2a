package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.core.data.ais.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConsentFilteringServiceTest {
    @InjectMocks
    private ConsentFilteringService consentFilteringService;
    @Mock
    private ConsentDataMapper consentDataMapper;

    private static final Random RANDOM = new Random();

    private static final String ASPSP_ACCOUNT_ID_1 = "aspsp account id 1";
    private static final String ASPSP_ACCOUNT_ID_2 = "aspsp account id 2";
    private static final String ASPSP_ACCOUNT_ID_3 = "aspsp account id 3";

    private static final byte[] CONSENT_BYTES_1 = {1};
    private static final byte[] CONSENT_BYTES_2 = {2};
    private static final byte[] CONSENT_BYTES_3 = {3};

    @Test
    void filterAisConsentsByAspspAccountId_emptyAccountId_shouldReturnInputEntities() {
        List<ConsentEntity> consentEntitiesInput = Arrays.asList(buildConsentEntity(CONSENT_BYTES_1), buildConsentEntity(CONSENT_BYTES_2), buildConsentEntity(CONSENT_BYTES_3));

        List<ConsentEntity> consentEntities = consentFilteringService.filterAisConsentsByAspspAccountId(consentEntitiesInput, null);

        assertEquals(consentEntities, consentEntitiesInput);
    }

    @Test
    void filterAisConsentsByAspspAccountId_success_withoutAdditionalInformationAccesses() {
        List<ConsentEntity> consentEntitiesInput = Arrays.asList(buildConsentEntity(CONSENT_BYTES_1),
                                                                 buildConsentEntity(CONSENT_BYTES_2),
                                                                 buildConsentEntity(CONSENT_BYTES_3));
        List<AisConsentData> aisConsentDatas = Arrays.asList(buildAisConsentData(ASPSP_ACCOUNT_ID_1, false),
                                                             buildAisConsentData(ASPSP_ACCOUNT_ID_2, false),
                                                             buildAisConsentData(ASPSP_ACCOUNT_ID_3, false));

        for (int i = 0; i < consentEntitiesInput.size(); i++) {
            AisConsentData aisConsentData = aisConsentDatas.get(i);
            when(consentDataMapper.mapToAisConsentData(consentEntitiesInput.get(i).getData())).thenReturn(aisConsentData);
        }

        List<ConsentEntity> consentEntities = consentFilteringService.filterAisConsentsByAspspAccountId(consentEntitiesInput, ASPSP_ACCOUNT_ID_2);

        assertEquals(consentEntities.size(), 1);
        assertEquals(consentEntities.get(0), consentEntitiesInput.get(1));
    }

    @Test
    void filterAisConsentsByAspspAccountId_success_withAdditionalInformationAccesses() {
        List<ConsentEntity> consentEntitiesInput = Arrays.asList(buildConsentEntity(CONSENT_BYTES_1),
                                                                 buildConsentEntity(CONSENT_BYTES_2),
                                                                 buildConsentEntity(CONSENT_BYTES_3));
        List<AisConsentData> aisConsentDatas = Arrays.asList(buildAisConsentData(ASPSP_ACCOUNT_ID_1, true),
                                                             buildAisConsentData(ASPSP_ACCOUNT_ID_2, true),
                                                             buildAisConsentData(ASPSP_ACCOUNT_ID_3, true));

        for (int i = 0; i < consentEntitiesInput.size(); i++) {
            AisConsentData aisConsentData = aisConsentDatas.get(i);
            when(consentDataMapper.mapToAisConsentData(consentEntitiesInput.get(i).getData())).thenReturn(aisConsentData);
        }

        List<ConsentEntity> consentEntities = consentFilteringService.filterAisConsentsByAspspAccountId(consentEntitiesInput, ASPSP_ACCOUNT_ID_2);

        assertEquals(consentEntities.size(), 1);
        assertEquals(consentEntities.get(0), consentEntitiesInput.get(1));
    }


    private ConsentEntity buildConsentEntity(byte[] data) {
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setData(data);
        return consentEntity;
    }

    private AccountReference buildAccountReference(String aspspAccountId) {
        return new AccountReference(aspspAccountId, null, null, null, null, null, null, Currency.getInstance("EUR"));
    }

    private List<AccountReference> buildAccountReferences(String aspspAccountId) {
        return Arrays.asList(buildAccountReference(aspspAccountId));
    }

    private AisConsentData buildAisConsentData(String aspspAccountId, boolean withAdditionalInformation) {
        return new AisConsentData(
            buildAccountAccesses(buildAccountReferences(aspspAccountId), withAdditionalInformation),
            buildAccountAccesses(buildAccountReferences(aspspAccountId), withAdditionalInformation),
            false
        );
    }

    private AccountAccess buildAccountAccesses(List<AccountReference> accountReferences, boolean withAdditionalInformation) {
        return new AccountAccess(
            accountReferences,
            accountReferences,
            accountReferences,
            null,
            null,
            null,
            withAdditionalInformation ? new AdditionalInformationAccess(accountReferences) : null
        );
    }
}
