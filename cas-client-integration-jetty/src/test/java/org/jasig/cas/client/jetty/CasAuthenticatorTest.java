package org.jasig.cas.client.jetty;

import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for {@link CasAuthenticator}.
 *
 * @author Marvin S. Addison
 */
public class CasAuthenticatorTest {

    private static final Server server = new Server(8080);
    private static final CasAuthenticator authenticator = new CasAuthenticator();

    @BeforeClass
    public static void beforeClass() throws Exception {
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/webapp");
        String workingDir = new File(".").getAbsolutePath();
        workingDir = workingDir.substring(0, workingDir.length() - 2);
        final String webappDir;
        if (workingDir.endsWith("/cas-client-integration-jetty")) {
            webappDir = workingDir + "/src/test/webapp";
        } else {
            webappDir = workingDir + "/cas-client-integration-jetty/src/test/webapp";
        }
        context.setWar(webappDir);


        // JSP config from https://github.com/jetty-project/embedded-jetty-jsp/
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");
        context.setAttribute("javax.servlet.context.tempdir", getScratchDir());
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/.*taglibs.*\\.jar$");
        context.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        context.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        context.addBean(new ServletContainerInitializersStarter(context), true);
        context.addServlet(jspServletHolder(), "*.jsp");

        // Wire up CAS authentication
        authenticator.setServerNames("localhost:8080");
        authenticator.setTicketValidator(new Cas20ServiceTicketValidator("http://localhost:8081/cas"));

        // Configure security handling for webapp context
        final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        final Constraint constraint = new Constraint("CasRealm", Constraint.ANY_AUTH);
        constraint.setAuthenticate(true);
        final ConstraintMapping secureMapping = new ConstraintMapping();
        secureMapping.setPathSpec("/secure.jsp");
        secureMapping.setConstraint(constraint);
        securityHandler.addConstraintMapping(secureMapping);
        securityHandler.setAuthenticator(authenticator);
        context.setSecurityHandler(securityHandler);

        // Add webapp context and start the server
        server.setHandler(context);
        server.start();
    }

    @Test
    public void testValidateRequestPublicPageNoTicket() throws Exception {
        final HttpURLConnection uc = openConnection("http://localhost:8080/webapp/");
        try {
            assertEquals(200, uc.getResponseCode());
            assertTrue(readOutput(uc).contains("Welcome everyone"));
        } finally {
            uc.disconnect();
        }
    }

    @Test
    public void testValidateRequestPublicPageWithTicket() throws Exception {
        final HttpURLConnection uc = openConnection("http://localhost:8080/webapp/?ticket=ST-12345");
        try {
            assertEquals(200, uc.getResponseCode());
            assertTrue(readOutput(uc).contains("Welcome everyone"));
        } finally {
            uc.disconnect();
        }
    }

    @Test
    public void testValidateRequestSecurePageNoTicket() throws Exception {
        final HttpURLConnection uc = openConnection("http://localhost:8080/webapp/secure.jsp");
        try {
            assertEquals(302, uc.getResponseCode());
            assertEquals(
                    "http://localhost:8081/cas/login?service=http%3A%2F%2Flocalhost%3A8080%2Fwebapp%2Fsecure.jsp",
                    uc.getHeaderField("Location"));
        } finally {
            uc.disconnect();
        }
    }

    @Test
    public void testValidateRequestSecurePageWithTicket() throws Exception {
        final String successResponse = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>" +
                "<cas:authenticationSuccess>" +
                "<cas:user>bob</cas:user>" +
                "</cas:authenticationSuccess>" +
                "</cas:serviceResponse>";
        final PublicTestHttpServer server = PublicTestHttpServer.instance(8081);
        server.content = successResponse.getBytes(StandardCharsets.UTF_8);
        final HttpURLConnection uc = openConnection("http://localhost:8080/webapp/secure.jsp?ticket=ST-12345");
        try {
            assertEquals(200, uc.getResponseCode());
            assertTrue(readOutput(uc).contains("Hello bob"));
        } finally {
            uc.disconnect();
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    private String readOutput(final URLConnection connection) throws IOException {
        final InputStreamReader reader = new InputStreamReader(connection.getInputStream());
        final StringBuilder builder = new StringBuilder();
        final CharBuffer buffer = CharBuffer.allocate(1024);
        try {
            while (reader.read(buffer) > 0) {
                builder.append(buffer.flip());
                buffer.clear();
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }

    private static File getScratchDir() throws IOException
    {
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        final File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

        if (!scratchDir.exists())
        {
            if (!scratchDir.mkdirs())
            {
                throw new IOException("Unable to create scratch directory: " + scratchDir);
            }
        }
        return scratchDir;
    }

    /**
     * Ensure the jsp engine is initialized correctly
     */
    private static List<ContainerInitializer> jspInitializers()
    {
        return Collections.singletonList(new ContainerInitializer(new JettyJasperInitializer(), null));
    }

    /**
     * Create JSP Servlet (must be named "jsp")
     */
    private static ServletHolder jspServletHolder()
    {
        final ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.7");
        holderJsp.setInitParameter("compilerSourceVM", "1.7");
        holderJsp.setInitParameter("keepgenerated", "true");
        return holderJsp;
    }

    private static HttpURLConnection openConnection(final String url) throws IOException {
        final HttpURLConnection uc;
        try {
            uc = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Invalid URL: " + url, e);
        }
        uc.setInstanceFollowRedirects(false);
        uc.connect();
        return uc;
    }
}