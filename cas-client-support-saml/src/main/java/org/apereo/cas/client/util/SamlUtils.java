/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.cas.client.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

/**
 * SAML utility class.
 *
 * @author Marvin S. Addison
 * @since 3.4
 */
public final class SamlUtils {

    private static final DateTimeFormatter ISO_FORMATTER_NO_MILLIS = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss"))
            .parseLenient()
            .appendOffset("+HHMM", "Z")
            .parseStrict()
            .toFormatter();
    private static final DateTimeFormatter ISO_PARSER_WITH_MILLIS = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .parseLenient()
            .appendOffset("+HHMM", "Z")
            .parseStrict()
            .toFormatter();

    private SamlUtils() {
        // nothing to do
    }

    public static String formatForUtcTime(final Date date) {
        return ISO_FORMATTER_NO_MILLIS.format(ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC));
    }

    public static Date parseUtcDate(final String date) {
        if (CommonUtils.isEmpty(date)) {
            return null;
        }
        return Date.from(ZonedDateTime.parse(date, ISO_PARSER_WITH_MILLIS).toInstant());
    }
}
