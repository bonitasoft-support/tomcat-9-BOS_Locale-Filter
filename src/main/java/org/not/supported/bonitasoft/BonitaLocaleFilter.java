/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.not.supported.bonitasoft;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.catalina.filters.FilterBase;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.Cookie;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;


/**
 * Filter that explicitly sets the value of the BOS_Locale to configured language.
 */
public class BonitaLocaleFilter extends FilterBase {

    // Log must be non-static as loggers are created per class-loader and this
    // Filter may be used in multiple class loaders
    private final Log log = LogFactory.getLog(BonitaLocaleFilter.class); // must not be static

    private static final String DEFAULT_BONITA_LOCALE_COOKIE_NAME = "BOS_Locale";
    private static final String DEFAULT_LANGUAGE = "en";

    private String language = DEFAULT_LANGUAGE;
    private String bonitaLocaleCookieName = DEFAULT_BONITA_LOCALE_COOKIE_NAME;

    private boolean dryRun = false;

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setBonitaLocaleCookieName(String bonitaLocaleCookieName) {
        this.bonitaLocaleCookieName = bonitaLocaleCookieName;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    @Override
    protected Log getLogger() {
        return log;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        if (language == null || language.length() == 0) {
            language = DEFAULT_LANGUAGE;
        }
        if (bonitaLocaleCookieName == null || bonitaLocaleCookieName.length() == 0) {
            bonitaLocaleCookieName = DEFAULT_BONITA_LOCALE_COOKIE_NAME;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        // Wrap the response
        if (response instanceof HttpServletResponse) {
            ResponseWrapper wrapped =
                new ResponseWrapper((HttpServletResponse)response, language, bonitaLocaleCookieName, dryRun, log);
            chain.doFilter(request, wrapped);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Wrapper that adds BOS_Locale:language if htte Bonita product add a BOS_Locale cookie.
     */
    public static class ResponseWrapper extends HttpServletResponseWrapper {

        private static final String HANDLING_COOKIE = "handling addCookie ";
        private static final String SEP = ":";
        private static final String DRY_RUN = " ####   DRY RUN  #  NO CHANGE   #### ";
        private static final String VALUE_REPLACED = " -> value replaced with ";
        private Log log = null;
        private String language = null;
        private String bonitaLocaleCookieName = null;
        private boolean dryRun = false;

        public ResponseWrapper(HttpServletResponse response, String language, String bonitaLocaleCookieName, boolean dryRun, Log log) {
            super(response);
            this.log = log;
            this.language = language;
            this.bonitaLocaleCookieName = bonitaLocaleCookieName;
            this.dryRun = dryRun;
        }

        @Override
        public void addCookie(Cookie cookie) {
            if (cookie != null && cookie.getName() != null && bonitaLocaleCookieName.equals(cookie.getName())) {
              String debugMessage = HANDLING_COOKIE + cookie.getName() + SEP + cookie.getValue();      
              if (!dryRun) {
                Cookie clonedCookie = (Cookie) cookie.clone();
                clonedCookie.setValue(language);
                super.addCookie(clonedCookie);
                log.debug(debugMessage + VALUE_REPLACED + clonedCookie.getValue());
              } else {
                super.addCookie(cookie);
                log.debug(debugMessage + DRY_RUN);
              }
            } else {
              super.addCookie(cookie);
              if (log.isTraceEnabled()) {
                log.trace(HANDLING_COOKIE + cookie.getName() + SEP + cookie.getValue());
              }
            }
        }
    }
}
