/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.client.authentication;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Simple security group implementation
 *
 * @author Marvin S. Addison
 * @version $Revision$
 * @since 3.1.11
 *
 */
public final class SimpleGroup extends SimplePrincipal implements Group {

    private static final long serialVersionUID = 1541943977571896383L;

    private final Set members = new HashSet();

    /**
     * Creates a new group with the given name.
     * @param name Group name.
     */
    public SimpleGroup(final String name) {
        super(name);
    }

    public boolean addMember(final Principal user) {
        return this.members.add(user);
    }

    public boolean isMember(final Principal member) {
        return this.members.contains(member);
    }

    public Enumeration members() {
        return new EnumerationAdapter(this.members.iterator());
    }

    public boolean removeMember(final Principal user) {
        return this.members.remove(user);
    }
    
    public String toString() {
        return super.toString() + ": " + members.toString();
    }

    /**
     * Adapts a {@link java.util.Iterator} onto an {@link java.util.Enumeration}.
     */
    private static class EnumerationAdapter implements Enumeration {

        /** Iterator backing enumeration operations */
        private Iterator iterator;

        /**
         * Creates a new instance backed by the given iterator.
         * @param i Iterator backing enumeration operations.
         */
        public EnumerationAdapter(final Iterator i) {
            this.iterator = i;
        }

        public boolean hasMoreElements() {
            return this.iterator.hasNext();
        }

        public Object nextElement() {
            return this.iterator.next();
        }
    }
}
