package ntnu.idi.mushroomidentificationbackend.config;

import org.apache.catalina.core.StandardHost;
import org.apache.catalina.valves.ErrorReportValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * Prevents the embedded Tomcat's default error page from disclosing the server software and
 * version (e.g. "Apache Tomcat/10.1.34") in error responses, which can help an attacker identify
 * known vulnerabilities affecting that specific version.
 */
@Component
public class TomcatServerInfoConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

  @Override
  public void customize(TomcatServletWebServerFactory factory) {
    factory.addConnectorCustomizers(connector -> connector.setXpoweredBy(false));
    factory.addContextCustomizers(context -> {
      if (context.getParent() instanceof StandardHost host) {
        host.setErrorReportValveClass(SilentErrorReportValve.class.getName());
      }
    });
  }

  /**
   * An ErrorReportValve that never renders server software/version information or the default
   * HTML error report body.
   */
  public static class SilentErrorReportValve extends ErrorReportValve {
    public SilentErrorReportValve() {
      setShowServerInfo(false);
      setShowReport(false);
    }
  }
}
