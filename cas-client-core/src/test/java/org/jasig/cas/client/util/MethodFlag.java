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

/**
 * A mutable boolean-like flag for unit tests which use
 * anonymous classes.
 * <p>
 * A simple boolean would be ideal, except Java requires us
 * to mark enclosing local variables as final.
 * 
 * @author Brad Cupit (brad [at] lsu {dot} edu)
 */
public class MethodFlag {
    boolean called = false;

    public boolean wasCalled() {
        return called;
    }

    public void setCalled() {
        called = true;
    }
}
