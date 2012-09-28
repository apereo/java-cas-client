/**
 * Copyright (c) 2005-2007, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name tuckey.org nor the names of its contributors
 *     may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.jasig.cas.client.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements a regex like interface on top of WildcardMatcher, this is really just a convinience class
 * so that it's easier for us to program against regex and wildcard patterns.
 */
public class WildcardMatcher implements StringMatchingMatcher {

    private static Log log = LogFactory.getLog(WildcardMatcher.class);


    private WildcardHelper wh;
    private int[] compiledPattern;
    private String matchStr;
    private Map resultMap = new HashMap();
    private boolean findCalled = false;
    private boolean found = false;


    public WildcardMatcher(WildcardHelper wh, String patternStr, String matchStr) {
        this.wh = wh;
        this.compiledPattern = wh.compilePattern(patternStr);
        this.matchStr = matchStr;
    }

    public boolean find() {
        found = wh.match(resultMap, matchStr, compiledPattern);
        return found;
    }

    public boolean isFound() {
        return found;
    }

    // the pattern for finding $1 $2 etc
    private static Pattern variablePattern = Pattern.compile("(?<!\\\\)\\$([0-9])");
    private static Pattern escapedVariablePattern = Pattern.compile("\\\\(\\$[0-9])");

    public String replaceAll(String subjectOfReplacement) {
        if (! findCalled) find();

        int lastCondMatcherGroupCount = this.groupCount();

        Matcher variableMatcher = variablePattern.matcher(subjectOfReplacement);

        StringBuffer sb = new StringBuffer();

        while (variableMatcher.find()) {
            int groupCount = variableMatcher.groupCount();
            if (groupCount < 1) {
                log.error("group count on variable finder regex is not as expected");
                if (log.isDebugEnabled()) {
                    log.error("variableMatcher: " + variableMatcher.toString());
                }
                continue;
            }
            String varStr = variableMatcher.group(1);

            boolean validVariable = false;
            int varInt = 0;
            log.debug("found " + varStr);
            try {
                varInt = Integer.parseInt(varStr);
                if (varInt > lastCondMatcherGroupCount) {
                    log.error("variable $" + varInt + " not found");
                    if (log.isDebugEnabled()) {
                        log.debug("wildcard matcher: " + this.toString());
                    }
                } else {
                    validVariable = true;
                }
            } catch (NumberFormatException nfe) {
                log.error("could not parse variable " + varStr + " to number");
            }
            String conditionMatch = "";
            if (validVariable) {
                conditionMatch = this.group(varInt);
            }
            variableMatcher.appendReplacement(sb, conditionMatch);
        }
        variableMatcher.appendTail(sb);
        if (log.isDebugEnabled()) log.debug("replaced sb is " + sb);
        String result = sb.toString();

        Matcher escapedVariableMatcher = escapedVariablePattern.matcher(result);
        result = escapedVariableMatcher.replaceAll("$1");

        return result;
    }

    public int groupCount() {
        if (resultMap == null) return 0;
        return resultMap.size() == 0 ? 0 : resultMap.size() - 1;
    }

    public String group(int groupId) {
        if (resultMap == null) return null;
        return (String) resultMap.get("" + groupId);
    }

}
