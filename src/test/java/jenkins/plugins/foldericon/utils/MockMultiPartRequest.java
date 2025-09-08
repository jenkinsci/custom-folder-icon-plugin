package jenkins.plugins.foldericon.utils;

import jakarta.annotation.Nonnull;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ReadListener;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileItemHeaders;
import org.apache.commons.fileupload2.core.FileItemHeadersProvider;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.BindInterceptor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.lang.Klass;

/**
 * Mock request for multi-part uploads.
 */
public class MockMultiPartRequest implements StaplerRequest2 {

    private final byte[] buffer;
    private ByteArrayInputStream stream = null;

    /**
     * @param buffer buffer
     */
    public MockMultiPartRequest(byte[] buffer) {
        this.buffer = buffer;
        if (buffer != null) {
            this.stream = new ByteArrayInputStream(buffer);
        }
    }

    @Override
    public int getContentLength() {
        if (buffer != null) {
            return buffer.length;
        } else {
            return 0;
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        if (buffer != null) {
            return new ServletInputStream() {
                @Override
                public int read() {
                    return stream.read();
                }

                @Override
                public int read(@Nonnull byte[] b) throws IOException {
                    return stream.read(b);
                }

                @Override
                public int read(@Nonnull byte[] b, int off, int len) {
                    return stream.read(b, off, len);
                }

                @Override
                public boolean isFinished() {
                    return stream.available() != 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // NOP
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public String getContentType() {
        return "multipart/form-data; boundary=myboundary";
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public void setCharacterEncoding(String s) {
        // NOP
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public String getAuthType() {
        return "";
    }

    @Override
    public Cookie[] getCookies() {
        return null;
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return "";
    }

    @Override
    public String getPathInfo() {
        return "";
    }

    @Override
    public String getPathTranslated() {
        return "";
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return "";
    }

    @Override
    public String getRemoteUser() {
        return "";
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return "";
    }

    @Override
    public String getRequestURI() {
        return "";
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer();
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getParameter(String name) {
        return "";
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return "";
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public String getServerName() {
        return "";
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return "";
    }

    @Override
    public String getRemoteHost() {
        return "";
    }

    @Override
    public void setAttribute(String name, Object o) {
        // NOP
    }

    @Override
    public void removeAttribute(String name) {
        // NOP
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    @Deprecated
    public String getRealPath(String path) {
        return "";
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "";
    }

    @Override
    public String getLocalAddr() {
        return "";
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return "";
    }

    @Override
    public boolean authenticate(HttpServletResponse response) {
        return false;
    }

    @Override
    public void login(String username, String password) {
        // NOP
    }

    @Override
    public void logout() {
        // NOP
    }

    @Override
    public Collection<Part> getParts() {
        return null;
    }

    @Override
    public Part getPart(String name) {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
        return null;
    }

    @Override
    public Stapler getStapler() {
        return null;
    }

    @Override
    public WebApp getWebApp() {
        return null;
    }

    @Override
    public String getRestOfPath() {
        return "";
    }

    @Override
    public String getOriginalRestOfPath() {
        return "";
    }

    @Override
    public String getRequestURIWithQueryString() {
        return "";
    }

    @Override
    public StringBuffer getRequestURLWithQueryString() {
        return new StringBuffer();
    }

    @Override
    public RequestDispatcher getView(Object it, String viewName) {
        return null;
    }

    @Override
    public RequestDispatcher getView(Class clazz, String viewName) {
        return null;
    }

    @Override
    public RequestDispatcher getView(Klass<?> clazz, String viewName) {
        return null;
    }

    @Override
    public String getRootPath() {
        return "";
    }

    @Override
    public String getReferer() {
        return "";
    }

    @Override
    public List<Ancestor> getAncestors() {
        return null;
    }

    @Override
    public Ancestor findAncestor(Class type) {
        return null;
    }

    @Override
    public <T> T findAncestorObject(Class<T> type) {
        return null;
    }

    @Override
    public Ancestor findAncestor(Object o) {
        return null;
    }

    @Override
    public boolean hasParameter(String name) {
        return false;
    }

    @Override
    public String getOriginalRequestURI() {
        return "";
    }

    @Override
    public boolean checkIfModified(long timestampOfResource, StaplerResponse2 rsp) {
        return false;
    }

    @Override
    public boolean checkIfModified(Date timestampOfResource, StaplerResponse2 rsp) {
        return false;
    }

    @Override
    public boolean checkIfModified(Calendar timestampOfResource, StaplerResponse2 rsp) {
        return false;
    }

    @Override
    public boolean checkIfModified(long timestampOfResource, StaplerResponse2 rsp, long expiration) {
        return false;
    }

    @Override
    public void bindParameters(Object bean) {
        // NOP
    }

    @Override
    @Deprecated
    public void bindParameters(Object bean, String prefix) {
        // NOP
    }

    @Override
    @Deprecated
    public <T> List<T> bindParametersToList(Class<T> type, String prefix) {
        return null;
    }

    @Override
    @Deprecated
    public <T> T bindParameters(Class<T> type, String prefix) {
        return null;
    }

    @Override
    @Deprecated
    public <T> T bindParameters(Class<T> type, String prefix, int index) {
        return null;
    }

    @Override
    public <T> T bindJSON(Class<T> type, JSONObject src) {
        return null;
    }

    @Override
    public <T> T bindJSON(Type genericType, Class<T> erasure, Object json) {
        return null;
    }

    @Override
    public void bindJSON(Object bean, JSONObject src) {
        // NOP
    }

    @Override
    public <T> List<T> bindJSONToList(Class<T> type, Object src) {
        return null;
    }

    @Override
    public BindInterceptor getBindInterceptor() {
        return null;
    }

    @Override
    @Deprecated
    public BindInterceptor setBindListener(BindInterceptor bindListener) {
        return null;
    }

    @Override
    @Deprecated
    public BindInterceptor setBindInterceptpr(BindInterceptor bindListener) {
        return null;
    }

    @Override
    public BindInterceptor setBindInterceptor(BindInterceptor bindListener) {
        return null;
    }

    @Override
    public JSONObject getSubmittedForm() {
        return null;
    }

    @Override
    public FileItem<?> getFileItem2(String name) throws ServletException, IOException {
        if (buffer != null && name != null && name.equals("file")) {
            return new FileItem() {
                @Override
                public InputStream getInputStream() {
                    return MockMultiPartRequest.this.getInputStream();
                }

                @Override
                public String getContentType() {
                    return MockMultiPartRequest.this.getContentType();
                }

                @Override
                public String getName() {
                    return "file";
                }

                @Override
                public boolean isInMemory() {
                    return true;
                }

                @Override
                public long getSize() {
                    return MockMultiPartRequest.this.getContentLength();
                }

                @Override
                public byte[] get() {
                    return new byte[0];
                }

                @Override
                public String getString(Charset encoding) {
                    return null;
                }

                @Override
                public String getString() {
                    return null;
                }

                @Override
                public FileItem<?> write(Path file) {
                    return this;
                }

                @Override
                public FileItem<?> delete() {
                    return this;
                }

                @Override
                public String getFieldName() {
                    return null;
                }

                @Override
                public FileItem<?> setFieldName(String name) {
                    return this;
                }

                @Override
                public boolean isFormField() {
                    return true;
                }

                @Override
                public FileItem<?> setFormField(boolean state) {
                    return this;
                }

                @Override
                public OutputStream getOutputStream() {
                    return null;
                }

                @Override
                public FileItemHeaders getHeaders() {
                    return null;
                }

                @Override
                public FileItemHeadersProvider<?> setHeaders(FileItemHeaders headers) {
                    return null;
                }
            };
        } else {
            return null;
        }
    }

    @Override
    @Deprecated
    public org.apache.commons.fileupload.FileItem getFileItem(String name) throws ServletException, IOException {
        FileItem<?> fileItem = getFileItem2(name);
        return fileItem != null ? org.apache.commons.fileupload.FileItem.fromFileUpload2FileItem(fileItem) : null;
    }

    @Override
    public boolean isJavaScriptProxyCall() {
        return false;
    }

    @Override
    public BoundObjectTable getBoundObjectTable() {
        return null;
    }

    @Override
    public String createJavaScriptProxy(Object toBeExported) {
        return null;
    }

    @Override
    public RenderOnDemandParameters createJavaScriptProxyParameters(Object toBeExported) {
        return null;
    }
}
