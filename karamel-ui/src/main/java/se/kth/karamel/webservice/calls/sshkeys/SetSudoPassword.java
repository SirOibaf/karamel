package se.kth.karamel.webservice.calls.sshkeys;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.util.SudoPassword;
import se.kth.karamel.webservice.calls.AbstractCall;

@Path("/sudopassword")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SetSudoPassword extends AbstractCall {

  public SetSudoPassword(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @PUT
  public Response sudoPassword(SudoPassword sudoPassword) {
    try {
      karamelApi.registerSudoPassword(sudoPassword.getPassword());
      return Response.status(Response.Status.OK).build();
    } catch (KaramelException e) {
      return buildExceptionResponse(e);
    }
  }
}
