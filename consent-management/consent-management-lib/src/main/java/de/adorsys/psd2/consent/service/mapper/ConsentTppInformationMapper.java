package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.consent.ConsentTppInformationEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring", uses = {TppInfoMapper.class})
public interface ConsentTppInformationMapper {
    ConsentTppInformation mapToConsentTppInformation(ConsentTppInformationEntity consentTppInformationEntity);

    ConsentTppInformationEntity mapToConsentTppInformationEntity(ConsentTppInformation consentTppInformation);
}
