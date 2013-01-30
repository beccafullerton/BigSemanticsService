package ecologylab.semantic.service.mmdrepository;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.NDC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ecologylab.logging.ILogger;
import ecologylab.semantic.service.SemanticServiceScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * mmdrepository.xml root resource
 * 
 * @author ajit
 * 
 */

@Path("/mmdrepository.xml")
@Component
@Scope("singleton")
public class MMDRepositoryXMLService
{

  static ILogger  logger;

  static Response resp = null;

  static
  {
    logger =
        SemanticServiceScope.get().getLoggerFactory().getLogger(MMDRepositoryXMLService.class);
  }

  @GET
  @Produces("application/xml")
  public Response getMmdRepository()
  {
    NDC.push("mmdrepository | format: xml");
    long requestTime = System.currentTimeMillis();
    logger.debug("Requested at: " + (new Date(requestTime)));

    if (resp == null)
      resp = MMDRepositoryServiceHelper.getMmdRepository(StringFormat.XML);

    logger.debug("Time taken (ms): " + (System.currentTimeMillis() - requestTime));
    NDC.remove();
    return resp;
  }
}
