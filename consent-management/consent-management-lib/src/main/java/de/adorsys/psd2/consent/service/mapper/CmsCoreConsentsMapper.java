package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.AccountAccess;
import de.adorsys.psd2.consent.domain.account.Consent;
import de.adorsys.psd2.core.consent.model.AccountReference;
import de.adorsys.psd2.core.consent.model.AdditionalInformationAccess;
import de.adorsys.psd2.core.consent.model.Consents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CmsCoreConsentsMapper {
    private final ConsentMapper consentMapper;

    public Consents mapToTppConsents(Consent consent) {
        return new Consents()
                   .access(mapToTppAccountAccess(consentMapper.mapToAisAccountAccess(consent)))
                   .recurringIndicator(consent.isRecurringIndicator())
                   .validUntil(consent.getValidUntil())
                   .frequencyPerDay(consent.getAllowedFrequencyPerDay())
                   .combinedServiceIndicator(consent.isCombinedServiceIndicator());
    }

    private de.adorsys.psd2.core.consent.model.AccountAccess mapToTppAccountAccess(AccountAccess access) {
        return new de.adorsys.psd2.core.consent.model.AccountAccess()
                   .accounts(mapToTppAccountReferences(access.getAccounts()))
                   .balances(mapToTppAccountReferences(access.getBalances()))
                   .transactions(mapToTppAccountReferences(access.getTransactions()))
                   .availableAccounts(de.adorsys.psd2.core.consent.model.AccountAccess.AvailableAccountsEnum.fromValue(access.getAvailableAccounts()))
                   .availableAccountsWithBalance(de.adorsys.psd2.core.consent.model.AccountAccess.AvailableAccountsWithBalanceEnum.fromValue(access.getAvailableAccountsWithBalance()))
                   .allPsd2(de.adorsys.psd2.core.consent.model.AccountAccess.AllPsd2Enum.fromValue(access.getAllPsd2()))
                   .additionalInformation(mapToTppAdditionalInformationAccess(access.getAccountAdditionalInformationAccess()).get());
    }

    private Optional<AdditionalInformationAccess> mapToTppAdditionalInformationAccess(de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess additionalInformationAccess) {
        return Optional.of(additionalInformationAccess)
                   .map(adi -> new de.adorsys.psd2.core.consent.model.AdditionalInformationAccess()
                                   .ownerName(mapToTppAccountReferences(adi.getOwnerName())));
    }

    private List<AccountReference> mapToTppAccountReferences(List<de.adorsys.psd2.xs2a.core.profile.AccountReference> accountReferences) {
        return accountReferences
                   .stream()
                   .map(accountReference -> new de.adorsys.psd2.core.consent.model.AccountReference()
                                                .iban(accountReference.getIban())
                                                .bban(accountReference.getBban())
                                                .pan(accountReference.getPan())
                                                .maskedPan(accountReference.getMaskedPan())
                                                .msisdn(accountReference.getMsisdn())
                                                .currency(accountReference.getCurrency().getDisplayName()))
                   .collect(Collectors.toList());
    }

}
