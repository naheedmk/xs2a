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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.consent.domain.account.Consent;
import de.adorsys.psd2.consent.service.mapper.CmsCoreConsentsMapper;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class CoreConsentsConvertService extends CoreConvertService<Consent,ConsentType> {
    private final CmsCoreConsentsMapper cmsCoreConsentsMapper;

    public CoreConsentsConvertService(Xs2aObjectMapper xs2aObjectMapper, CmsCoreConsentsMapper cmsCoreConsentsMapper) {
        super(xs2aObjectMapper);
        this.cmsCoreConsentsMapper = cmsCoreConsentsMapper;
    }

    @Override
    public Map<ConsentType, Function<Consent, Object>> statusTransformer() {
        Map<ConsentType, Function<Consent, Object>> map = new HashMap<>();
        map.put(ConsentType.AIS, cmsCoreConsentsMapper::mapToTppConsents);
        return map;
    }
}
