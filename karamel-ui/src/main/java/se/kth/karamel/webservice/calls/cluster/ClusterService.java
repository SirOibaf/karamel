package se.kth.karamel.webservice.calls.cluster;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.NotImplementedException;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.clusterdef.Cluster;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

@Path("/cluster")
public class ClusterService extends AbstractCall {

  public ClusterService(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCluster() {
    try {
      Cluster cluster = karamelApi.getCluster();
      return Response.ok().entity(cluster).build();
    } catch (KaramelException e) {
      return buildExceptionResponse(e);
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response action(@NotNull @QueryParam("action") ClusterActions action)
    throws KaramelException, InterruptedException {
    switch (action) {
      case VALIDATE:
        // TODO(Fabio): return validation answer to the user
        karamelApi.getCluster().validate();
        // If the cluster is valid, no exceptions are thrown, so respond OK
        return Response.ok().build();
      default:
        throw new NotImplementedException("Unknown action");
    }
  }
}
