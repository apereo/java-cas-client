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
package org.jasig.cas.client.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * Test cases for {@link SamlUtils}.
 *
 * @author Marvin S. Addison
 * @author Emil Sierżęga
 */
public class SamlUtilsTests {

    @Test
    public void testParseUtcDate() {
        final Date expected = new Date(1424437961025L);
        Assert.assertEquals(expected, SamlUtils.parseUtcDate("2015-02-20T08:12:41.025-0500"));
        final Date expectedNoMillis = new Date(1424437961000L);
        Assert.assertEquals(expectedNoMillis, SamlUtils.parseUtcDate("2015-02-20T08:12:41-0500"));
    }

    @Test
    public void testFormatUtcDate() {
        final Calendar calendar = Calendar.getInstance();
        final String expected = "2015-02-20T13:12:41Z";

        calendar.setTimeInMillis(1424437961025L);
        Assert.assertEquals(expected, SamlUtils.formatForUtcTime(calendar.getTime()));

        calendar.setTimeInMillis(1424437961000L);
        Assert.assertEquals(expected, SamlUtils.formatForUtcTime(calendar.getTime()));
    }
}