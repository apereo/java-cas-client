/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.authorization;

import java.util.ArrayList;
import java.util.List;

public final class DefaultAuthorizedDeciderImplTests extends
        AbstractAuthorizedDeciderTests {

    public AuthorizedDecider getCasAuthorizedDeciderImpl() {
        final List list = new ArrayList();
        list.add("scott");

        final DefaultAuthorizedDeciderImpl impl = new DefaultAuthorizedDeciderImpl(list);
        return impl;
    }

}
