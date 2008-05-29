/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/index.html
 */
package org.jasig.cas.client.validation;

import org.jasig.cas.client.util.CommonUtils;

import java.beans.PropertyEditorSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Convert a String-formatted list of acceptable proxies to an array.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 * 
 */
public final class ProxyListEditor extends PropertyEditorSupport {

	public void setAsText(final String text) throws IllegalArgumentException {
		final BufferedReader reader = new BufferedReader(new StringReader(text));
		final List proxyChains = new ArrayList();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				if (CommonUtils.isNotBlank(line)) {
					proxyChains.add(line.trim().split(" "));
				}
			}
		} catch (final IOException e) {
			// ignore this
		} finally {
			try {
				reader.close();
			} catch (final IOException e) {
				// nothing to do
			}
		}

		setValue(new ProxyList(proxyChains));
	}
}
