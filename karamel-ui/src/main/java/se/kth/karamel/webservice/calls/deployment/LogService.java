package se.kth.karamel.webservice.calls.deployment;

import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/logs")
public class LogService extends AbstractCall {

  public LogService(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response getLogs(@QueryParam("task") Integer task) throws KaramelException {
    if (task == null) {
      throw new KaramelException("Task id cannot be null");
    }
    return Response.ok().entity(karamelApi.getLogs(task)).build();
  }
}
