package se.kth.karamel.webservice.calls.cluster;

import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/upload")
public class Upload extends AbstractCall {

  private static final Logger logger = Logger.getLogger(StartCluster.class);

  public Upload(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @POST
  @Consumes(MediaType.TEXT_PLAIN)
  public Response uploadClusterDefinition(String clusterDefinition) {
    logger.debug("Received cluster definition: " + clusterDefinition);

    Response response = null;
    try {
      karamelApi.loadClusterDefinition(clusterDefinition);
      response = Response.ok().build();
    } catch (KaramelException e) {
      response = buildExceptionResponse(e);
    }

    return response;
  }
}
