/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.client.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * SAML utility class.
 *
 * @author Marvin S. Addison
 * @since 3.4
 */
public final class SamlUtils {

    private static final DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateTimeNoMillis();

    private SamlUtils() {
        // nothing to do
    }

    public static String formatForUtcTime(final Date date) {
        return ISO_FORMAT.print(new DateTime(date).withZone(DateTimeZone.UTC));
    }

    public static Date parseUtcDate(final String date) {
        if (CommonUtils.isEmpty(date)) {
            return null;
        }
        return ISODateTimeFormat.dateTimeParser().parseDateTime(date).toDate();
    }
}
