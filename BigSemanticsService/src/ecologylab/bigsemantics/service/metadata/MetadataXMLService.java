package ecologylab.bigsemantics.service.metadata;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.NDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ecologylab.bigsemantics.Utils;
import ecologylab.bigsemantics.service.SemanticServiceErrorMessages;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * metadata.xml root resource metadata request are made with url and for a graph span (default = 0)
 * 
 * @author ajit
 */
@Path("/metadata.xml")
@Component
@Scope("singleton")
public class MetadataXMLService
{

	static Logger	logger	= LoggerFactory.getLogger(MetadataXMLService.class);

	@GET
	@Produces("application/xml")
	public Response getMetadata(@Context HttpServletRequest request,
	                            @QueryParam("url") String url,
	                            @QueryParam("reload") boolean reload)
	{
	  String clientIp = request.getRemoteAddr();
	  String msg =
	      String.format("Request from %s: metadata.xml, reload=%s, url=%s", clientIp, reload, url);
	  byte[] fpBytes = Utils.fingerprintBytes("" + System.currentTimeMillis() + "|" + msg);
	  String fp = Utils.base64urlEncode(fpBytes);
	  logger.info("[FP{}] {}", fp, msg);
		NDC.push(String.format("[FP%s] ", fp));

		long requestTime = System.currentTimeMillis();

		Response resp = null;
		if (url != null)
		{
			ParsedURL purl = ParsedURL.getAbsolute(url);
			if (purl != null)
			{
				MetadataServiceHelper helper = new MetadataServiceHelper();
				resp = helper.getMetadataResponse(clientIp, purl, StringFormat.XML, reload);
			}
		}

		// invalid param
		if (resp == null)
		{
			resp = Response
			    .status(Status.BAD_REQUEST)
			    .entity(SemanticServiceErrorMessages.BAD_REQUEST)
					.type(MediaType.TEXT_PLAIN)
					.build();
		}

		logger.info("[FP{}] Total time (ms): {}", fp, System.currentTimeMillis() - requestTime);
		NDC.remove();

		return resp;
	}

}