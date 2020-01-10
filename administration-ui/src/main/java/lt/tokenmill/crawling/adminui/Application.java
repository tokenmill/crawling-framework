package lt.tokenmill.crawling.adminui;

import com.vaadin.server.VaadinServlet;
import lt.tokenmill.crawling.commonui.Configuration;
import org.eclipse.jetty.security.*;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static final Boolean PRODUCTION_MODE = true;

    private static SecurityHandler basicAuth(String username, String password, String realm) {

        HashLoginService l = new HashLoginService();
        l.putUser(username, Credential.getCredential(password), new String[]{"editor"});
        l.setName(realm);

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"editor"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("cf");
        csh.addConstraintMapping(cm);
        csh.setLoginService(l);

        return csh;

    }

    public static void main(String[] args) {
        int port = Configuration.INSTANCE.getInt("port", 8080);
        Server server = new Server(port);
        ServletContextHandler contextHandler
                = new ServletContextHandler(ServletContextHandler.SESSIONS);

        boolean authEnabled = Boolean.parseBoolean(Configuration.INSTANCE.getString("basicAuth", "false"));

        if(authEnabled) {
            contextHandler.setSecurityHandler(basicAuth(System.getenv("UI_USER"), System.getenv("UI_PASSWORD"), "editor"));
        }
        contextHandler.setContextPath("/");
        ServletHolder sh = new ServletHolder(new VaadinServlet());
        contextHandler.addServlet(sh, "/*");
        contextHandler.setInitParameter("ui", CrawlerAdminUI.class.getCanonicalName());
        contextHandler.setInitParameter("productionMode", String.valueOf(PRODUCTION_MODE));
        server.setHandler(contextHandler);


        try {
            server.start();
            server.join();
        } catch (Exception e) {
            LOG.error("Failed to start application", e);
        }
    }
}