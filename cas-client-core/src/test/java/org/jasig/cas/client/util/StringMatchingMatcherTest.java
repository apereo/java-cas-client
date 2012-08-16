package org.jasig.cas.client.util;

import java.util.List;

import junit.framework.TestCase;

public class StringMatchingMatcherTest extends TestCase {

	public void testIsFound() {
		String p1 = "/services/**/*.do";
		String p2 = "/**/webservice/**";
		WildcardPattern wp = new WildcardPattern(p2);
//		String path = "/services/processmodel/s/aaaa.do,/portal/index.jsp";
		String path = "/suite/webservice/processmodel/bindingApply?WSDL";
		List<String> pList = Strings.csvToList(path);
		for (String p : pList) {
			StringMatchingMatcher smat = wp.matcher(p);
			System.out.println("pattern:"+p+"-----"+smat.find());
		}
	}

}
