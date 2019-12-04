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

package de.adorsys.psd2.validator.signature.service;

public final class Constants {
    // header names:
    public static final String DIGEST_HEADER_NAME = "Digest";
    public static final String SIGNATURE_HEADER_NAME = "Signature";
    public static final String TPP_SIGNATURE_CERTIFICATE_HEADER_NAME = "TPP-Signature-Certificate";

    // signature header attributes names:
    public static final String KEY_ID_ATTRIBUTE_NAME = "keyId";
    public static final String ALGORITHM_ATTRIBUTE_NAME = "algorithm";
    public static final String HEADERS_ATTRIBUTE_NAME = "headers";
    public static final String SIGNATURE_ATTRIBUTE_NAME = "signature";

    // separators:
    public static final String EQUALS_SIGN_SEPARATOR = "=";
    public static final String COLON_SEPARATOR = ":";
    public static final String COMMA_SEPARATOR = ",";
    public static final String QUOTE_SEPARATOR = "\"";
    public static final String SPACE_SEPARATOR = " ";
    public static final String HEXADECIMAL_SPACE_SEPARATOR = "%20";
    public static final String LINE_BREAK_SEPARATOR = "\n";

    // certificates:
    public static final String CERTIFICATE_SERIAL_NUMBER_ATTRIBUTE = "SN";
    public static final String CERTIFICATION_AUTHORITY_ATTRIBUTE = "CA";

    private Constants() {
    }
}
