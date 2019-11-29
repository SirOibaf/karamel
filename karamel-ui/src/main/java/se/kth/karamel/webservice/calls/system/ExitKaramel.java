package se.kth.karamel.webservice.calls.system;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.webservice.calls.AbstractCall;

@Path("/system/exit")
public class ExitKaramel extends AbstractCall {

  public ExitKaramel(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @GET
  public Response exitKaramel() {
    Response response = Response.status(Response.Status.OK).build();

    new Thread(() -> {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ex) {
          LOGGER.warn(ex.getMessage());
        } finally {
          LOGGER.info("Karamel Shutdown finished.");
          System.exit(0);
        }
      }).start();

    return response;
  }

}
