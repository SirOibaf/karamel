package se.kth.karamel.webservice.calls.deployment;

import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;
import org.apache.commons.lang.NotImplementedException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/deployment")
public class DeploymentService extends AbstractCall {

  public DeploymentService(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDeploymentStatus() {
    return Response.ok().entity(karamelApi.getClusterDeploymentStatus()).build();
  }


  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response action(@NotNull @QueryParam("action") DeploymentActions action)
    throws KaramelException {

    switch (action) {
      case START:
        karamelApi.startCluster();
        break;
      case STOP:
        karamelApi.terminateCluster();
        break;
      case PAUSE:
        karamelApi.pauseCluster();
        break;
      default:
        throw new NotImplementedException("Unknown action");
    }

    return Response.ok().build();
  }
}
