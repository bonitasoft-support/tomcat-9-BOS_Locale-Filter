package org.not.supported.bonitasoft;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.catalina.filters.FilterBase;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.Cookie;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;


/**
 * Filter that explicitly sets the value of the BOS_Locale to configured language.
 */
public class BonitaLocaleFilter extends FilterBase {

    private final Log log = LogFactory.getLog(BonitaLocaleFilter.class); // must not be static

    private static final String ICOMING_HTTP_REQUEST = "First call addCookie - incoming HTTP request";
    private static final String SPACE = " ";
    private static final String REMOTE_IP = "Remote IP: ";
    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    private static final String ACCEPT_LANGUAGE = ACCEPT_LANGUAGE_HEADER + ": ";
    private static final String PARAMETERS = "Parameters: ";
    private static final String QUERY_STRING_STARTER = "?";
    private static final String PARAMETER_SEP = ", ";
    private static final Pattern USER_LOGIN_PASS_QUERY_STRING_PATTERN = Pattern.compile("(user[^=]*|login[^=]*|pass[^=]*)=[^&]*", Pattern.CASE_INSENSITIVE);
    private static final Pattern USER_LOGIN_PATTERN = Pattern.compile("[[:blank:]]*(user|login).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASS_PATTERN = Pattern.compile("[[:blank:]]*pass.*", Pattern.CASE_INSENSITIVE);
    private static final String KEY_VALUE_MAP_SEP = ":";
    private static final String OBFUSCATED_STRING = "[***]";
    private static final String COOKIE_HEADER = "Cookie";
    private static final String COOKIE = COOKIE_HEADER + ": ";
    private static final String NO_COOKIE = "No cookie header found in the incoming HTTP request.";
    private static final String REAL_PATH = "Real path: ";
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
        ResponseWrapper wrapped = null;
        if (log.isTraceEnabled()) {
          StringWriter messageWriter = null;
          PrintWriter messagePrinter = null;
          try {
            messageWriter = new StringWriter();
            messagePrinter = new PrintWriter(messageWriter);
            messagePrinter.println(ICOMING_HTTP_REQUEST);
            messagePrinter.print(request.getRemoteAddr());
            messagePrinter.print(SPACE);
            if (request instanceof HttpServletRequest) {
              HttpServletRequest httpRequest = ((HttpServletRequest)request);
              messagePrinter.print(httpRequest.getMethod());
              messagePrinter.print(SPACE);
              messagePrinter.print(httpRequest.getRequestURL());
              if (httpRequest.getQueryString() != null) {
                messagePrinter.print(QUERY_STRING_STARTER);
                messagePrinter.println(USER_LOGIN_PASS_QUERY_STRING_PATTERN.matcher(httpRequest.getQueryString()).replaceAll("$1=***"));
              } else {
                messagePrinter.println();
              }
              boolean first_entry_handled = false;
              for(Map.Entry<String,String[]> entry : request.getParameterMap().entrySet()) {
                if (first_entry_handled) {
                  messagePrinter.print(PARAMETER_SEP);
                } else {
                  messagePrinter.print(PARAMETERS);
                  first_entry_handled = true;
                }
                messagePrinter.print(entry.getKey()); messagePrinter.print(KEY_VALUE_MAP_SEP);
                if (entry.getKey() != null && ( USER_LOGIN_PATTERN.matcher(entry.getKey()).matches()) || PASS_PATTERN.matcher(entry.getKey()).matches()) {
                  messagePrinter.print(OBFUSCATED_STRING);
                } else {
                  messagePrinter.print(Arrays.toString(entry.getValue()));
                }
              }
              if (request.getParameterMap().size() > 0) {
                messagePrinter.println();
              }
              messagePrinter.print(ACCEPT_LANGUAGE); messagePrinter.println(httpRequest.getHeader(ACCEPT_LANGUAGE_HEADER));
              if (httpRequest.getHeader(COOKIE_HEADER) != null) {
                messagePrinter.print(COOKIE); messagePrinter.print(httpRequest.getHeader(COOKIE_HEADER));
              } else {
                messagePrinter.println(NO_COOKIE);
              }
            }
            wrapped = new ResponseWrapper((HttpServletResponse)response, language, bonitaLocaleCookieName, dryRun, log, messageWriter.toString());
          } finally {
            if (messagePrinter != null) {
              messagePrinter.close();
            }
          }
        }
        if (wrapped == null) {
          wrapped = new ResponseWrapper((HttpServletResponse)response, language, bonitaLocaleCookieName, dryRun, log, null);
        }
        chain.doFilter(request, wrapped );
      } else {
        chain.doFilter(request, response);
      }
    }

    /**
     * Wrapper that adds BOS_Locale:language if htte Bonita product add a BOS_Locale cookie.
     */
    public static class ResponseWrapper extends HttpServletResponseWrapper {

        private static final String HANDLING_COOKIE = "Handling addCookie ";
        private static final String NULL = "null";
        private static final String COOKIE_ASSIGN = "=";
        private static final String DRY_RUN = " ####   DRY RUN  #  NO CHANGE   #### ";
        private static final String VALUE_REPLACED = " -> value replaced with ";
        private Log log = null;
        private String language = null;
        private String bonitaLocaleCookieName = null;
        private boolean dryRun = false;
        private String messageHeader = null;
        private boolean messageHeaderPrinted = false;

        public ResponseWrapper(HttpServletResponse response, String language, String bonitaLocaleCookieName, boolean dryRun, Log log, String messageHeader) {
            super(response);
            this.log = log;
            this.language = language;
            this.bonitaLocaleCookieName = bonitaLocaleCookieName;
            this.dryRun = dryRun;
            this.messageHeader = messageHeader;
            this.messageHeaderPrinted = false;
        }

        @Override
        public void addCookie(Cookie cookie) {
          StringWriter messageWriter = null;
          PrintWriter messagePrinter = null;
          try {
            if (log.isDebugEnabled()) {
              messageWriter = new StringWriter();
              messagePrinter = new PrintWriter(messageWriter);
              if (!messageHeaderPrinted) {
                if (messageHeader != null) {
                  log.trace(messageHeader);
                }
                messageHeaderPrinted=true;
              }
            }
            if (cookie == null) {
              super.addCookie(cookie);
              if (log.isTraceEnabled()) {
                messagePrinter.print(HANDLING_COOKIE);
                messagePrinter.print(NULL);
                messagePrinter.flush();
                log.trace(messageWriter.toString());
              }
            } else if (cookie.getName() != null && bonitaLocaleCookieName.equals(cookie.getName())) {
              if (log.isDebugEnabled()) {
                messagePrinter.print(HANDLING_COOKIE);
                messagePrinter.print(cookie.getName()); messagePrinter.print(COOKIE_ASSIGN); messagePrinter.print(cookie.getValue()); 
              }
              if (!dryRun) {
                Cookie clonedCookie = (Cookie) cookie.clone();
                clonedCookie.setValue(language);
                super.addCookie(clonedCookie);
                if (log.isDebugEnabled()) {
                  messagePrinter.print(VALUE_REPLACED); messagePrinter.print(clonedCookie.getValue());
                }
              } else {
                super.addCookie(cookie);
                if (log.isDebugEnabled()) {
                  messagePrinter.print(DRY_RUN);
                }
              }
              if (log.isDebugEnabled()) {
                messagePrinter.flush();
                log.debug(messageWriter.toString());
              }
            } else {
              super.addCookie(cookie);
              if (log.isTraceEnabled()) {
                messagePrinter.print(HANDLING_COOKIE);
                messagePrinter.print(cookie.getName()); messagePrinter.print(COOKIE_ASSIGN); messagePrinter.print(cookie.getValue());
                messagePrinter.flush();
                log.trace(messageWriter.toString());
              }
            }
          } finally {
            if (messagePrinter != null) {
              messagePrinter.close();
            }
          }
        }
    }
}
