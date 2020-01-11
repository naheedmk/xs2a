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

package de.adorsys.psd2.consent.web.aspsp.config;

import com.google.common.base.Predicates;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@RequiredArgsConstructor
public class AspspApiSwaggerConfig {
    @Value("${license.url}")
    private String licenseUrl;
    private final BuildProperties buildProperties;

    @SuppressWarnings("Guava")  // Intellij IDEA claims that Guava predicates could be replaced with Java API,
    // but actually it is not possible
    @Bean(name = "aspsp-api")
    public Docket apiDocklet() {
        return new Docket(DocumentationType.SWAGGER_2)
                   .groupName("CMS-ASPSP-API")
                   .apiInfo(getApiInfo())
                   .tags(new Tag("ASPSP Export AIS Consents", "Provides access to the consent management system for exporting AIS consents by ASPSP"),
                         new Tag("ASPSP Events", "Provides access to the consent management system for ASPSP Events"),
                         new Tag("ASPSP PIIS, Consents", "Controller for CMS-ASPSP-API providing access to PIIS consents"),
                         new Tag("ASPSP PIIS Consents Export", "Provides access to the consent management system for exporting PIIS consents by ASPSP"),
                         new Tag("ASPSP Export Payments", "Provides access to the consent management system for exporting PIS payments by ASPSP"),
                         new Tag("ASPSP TPP Stop List", "Provides access to the consent management system TPP Stop List"),
                         new Tag("ASPSP TPP Info", "Provides access to the consent management system TPP Info"))
                   .select()
                   .apis(RequestHandlerSelectors.basePackage("de.adorsys.psd2.consent.web.aspsp"))
                   .paths(Predicates.not(PathSelectors.regex("/error.*?")))
                   .paths(Predicates.not(PathSelectors.regex("/connect.*")))
                   .paths(Predicates.not(PathSelectors.regex("/management.*")))
                   .build();
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                   .title("XS2A CMS ASPSP REST API")
                   .contact(new Contact("adorsys GmbH & Co. KG", "https://adorsys-platform.de/solutions/", "psd2@adorsys.de"))
                   .version(buildProperties.getVersion() + " " + buildProperties.get("build.number"))
                   .license("Apache License 2.0")
                   .licenseUrl(licenseUrl)
                   .build();
    }
}
