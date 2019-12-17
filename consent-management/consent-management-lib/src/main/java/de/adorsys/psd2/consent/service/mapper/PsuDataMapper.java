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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR, componentModel = "spring")
public interface PsuDataMapper {
    List<PsuData> mapToPsuDataList(List<PsuIdData> psuIdDataList);

    List<PsuIdData> mapToPsuIdDataList(List<PsuData> psuIdDataList);

    @Mapping(source = "additionalPsuIdData", target = "additionalPsuData")
    PsuData mapToPsuData(PsuIdData psuIdData);

    @Mapping(source = "additionalPsuData", target = "additionalPsuIdData")
    PsuIdData mapToPsuIdData(PsuData psuData);

    default String mapToStringFromUUID(UUID value) {
        return Optional.ofNullable(value).map(UUID::toString).orElse(null);
    }

    default UUID mapToUUIDFromString(String value) {
        return Optional.ofNullable(value).map(UUID::fromString).orElse(null);
    }
}
