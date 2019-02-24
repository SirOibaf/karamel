package se.kth.karamel.webservice.calls.cluster;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;
import se.kth.karamel.webservicemodel.KaramelBoardJSON;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

import java.io.IOException;

@Path("/cluster/start")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StartCluster extends AbstractCall {

  private static final Logger logger = Logger.getLogger(StartCluster.class);
  private ObjectMapper objectMapper = new ObjectMapper();

  public StartCluster(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response startCluster(KaramelBoardJSON boardJSON) {

    Response response = null;
    logger.debug("Start cluster: " + System.lineSeparator() + boardJSON.getJson());

    try {
      Cluster cluster = objectMapper.readValue(boardJSON.getJson(), Cluster.class);
      karamelApi.startCluster(cluster);
      response = Response.status(Response.Status.OK).
          entity(new StatusResponseJSON(StatusResponseJSON.SUCCESS_STRING, "success")).build();
    } catch (IOException | KaramelException e) {
      response = buildExceptionResponse(e);
    }
    
    return response;
  }
}
