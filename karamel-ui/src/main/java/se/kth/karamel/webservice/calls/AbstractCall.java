package se.kth.karamel.webservice.calls;

import org.apache.log4j.Logger;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.webservicemodel.StatusResponseJSON;

import javax.ws.rs.core.Response;

public abstract class AbstractCall {

  protected static final Logger LOGGER = Logger.getLogger(AbstractCall.class);
  protected KaramelApi karamelApi;
  
  public AbstractCall(KaramelApi karamelApi) {
    this.karamelApi = karamelApi;
  }
  
  protected Response buildExceptionResponse(Exception e) {
    LOGGER.error("", e);
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
        entity(new StatusResponseJSON(StatusResponseJSON.ERROR_STRING, e.getMessage())).build();
  }
}
