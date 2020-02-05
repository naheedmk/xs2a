package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.account.AisConsentAction;
import de.adorsys.psd2.consent.domain.account.Consent;
import de.adorsys.psd2.consent.repository.AisConsentActionRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.ConsentVerifyingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.EXPIRED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AisConsentServiceInternal implements AisConsentService {
    private final ConsentVerifyingRepository consentVerifyingRepository;
    private final ConsentJpaRepository consentJpaRepository;
    private final ConsentConfirmationExpirationService aisConsentConfirmationExpirationService;

    private final AisOneOffConsentExpirationService oneOffConsentExpirationService;
    private final AisConsentUsageService aisConsentUsageService;
    private final AisConsentActionRepository aisConsentActionRepository;

    /**
     * Saves information about consent usage and consent's sub-resources usage.
     *
     * @param request {@link AisConsentActionRequest} needed parameters for logging usage AIS consent
     */
    @Override
    @Transactional(rollbackFor = WrongChecksumException.class)
    public CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) throws WrongChecksumException {
        Optional<Consent> consentOpt = getActualAisConsent(request.getConsentId());
        if (consentOpt.isPresent()) {
            Consent consent = consentOpt.get();
            aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(consent);
            checkAndUpdateOnExpiration(consent);
            // In this method sonar claims that NPE is possible:
            // https://rules.sonarsource.com/java/RSPEC-3655
            // but we have isPresent in the code before.
            updateAisConsentUsage(consent, request); //NOSONAR
            logConsentAction(consent.getExternalId(), resolveConsentActionStatus(request, consent), request.getTppId()); //NOSONAR
        }

        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    private Optional<Consent> getActualAisConsent(String consentId) {
        return consentJpaRepository.findByExternalId(consentId)
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private Consent checkAndUpdateOnExpiration(Consent consent) {
        if (consent != null && consent.shouldConsentBeExpired()) {
            return aisConsentConfirmationExpirationService.expireConsent(consent);
        }

        return consent;
    }

    private void updateAisConsentUsage(Consent consent, AisConsentActionRequest request) throws WrongChecksumException {
        if (!request.isUpdateUsage()) {
            return;
        }
        aisConsentUsageService.incrementUsage(consent, request);

        if (!consent.isRecurringIndicator() && consent.getAllowedFrequencyPerDay() == 1 && oneOffConsentExpirationService.isConsentExpired(consent)) {
            consent.setConsentStatus(EXPIRED);
        }

        consent.setLastActionDate(LocalDate.now());

        consentVerifyingRepository.verifyAndSave(consent);
    }

    private void logConsentAction(String requestedConsentId, ActionStatus actionStatus, String tppId) {
        AisConsentAction action = new AisConsentAction();
        action.setActionStatus(actionStatus);
        action.setRequestedConsentId(requestedConsentId);
        action.setTppId(tppId);
        action.setRequestDate(LocalDate.now());
        aisConsentActionRepository.save(action);
    }

    private ActionStatus resolveConsentActionStatus(AisConsentActionRequest request, Consent consent) {

        if (consent == null) {
            log.info("Consent ID: [{}]. Consent action status resolver received null consent",
                     request.getConsentId());
            return ActionStatus.BAD_PAYLOAD;
        }
        return request.getActionStatus();
    }
}
