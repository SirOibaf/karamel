package se.kth.karamel.webservice.calls.sshkeys;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.util.SSHKeyPair;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

import java.util.List;

@Path("/sshkeys")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SSHKeys extends AbstractCall {

  public SSHKeys(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAvailableKeys() {
    try {
      List<SSHKeyPair> sshKeyPairs = karamelApi.getAvailableSSHKeys();
      return Response.status(Response.Status.OK).entity(sshKeyPairs).build();
    } catch (KaramelException ex) {
      return buildExceptionResponse(ex);
    }
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response registerKeys(SSHKeyPair sshKeyPair) {
    try {
      karamelApi.registerSshKeys(sshKeyPair);
      return Response.status(Response.Status.OK).build();
    } catch (KaramelException ex) {
      return buildExceptionResponse(ex);
    }
  }
}
