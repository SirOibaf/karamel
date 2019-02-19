package se.kth.karamel.webservice.calls.sshkeys;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.util.SshKeyPair;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

@Path("/ssh/loadKey")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoadSshKeys extends AbstractCall {

  public LoadSshKeys(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response load() {
    Response response = null;
    logger.debug(" Received request to load ssh keys.");
    try {
      SshKeyPair sshKeypair = karamelApi.loadSshKeysIfExist();
      karamelApi.registerSshKeys(sshKeypair);
      response = Response.status(Response.Status.OK).entity(sshKeypair).build();
    } catch (KaramelException ex) {
      response = buildExceptionResponse(ex);
    }

    return response;
  }

}
