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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link SamlUtils}.
 *
 * @author Marvin S. Addison
 */
public class SamlUtilsTest {

    @Test
    public void testParseUtcDate() {
        final Date expected = new Date(1424437961025L);
        assertEquals(expected, SamlUtils.parseUtcDate("2015-02-20T08:12:41.025-0500"));
    }
}