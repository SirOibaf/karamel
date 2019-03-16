package se.kth.karamel.webservice.calls.cluster;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.webservice.calls.AbstractCall;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Path("/upload")
public class UploadService extends AbstractCall {

  private static final Logger logger = Logger.getLogger(ClusterService.class);

  public UploadService(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadClusterDefinition(
      @FormDataParam("file0") InputStream uploadedInputStream) {
    Response response = null;
    try {
      String clusterDefinition = IOUtils.toString(uploadedInputStream, Charset.forName("UTF-8"));
      logger.debug("Received cluster definition: " + clusterDefinition);

      karamelApi.loadClusterDefinition(clusterDefinition);

      response = Response.ok().build();
    } catch (KaramelException | IOException e) {
      response = buildExceptionResponse(e);
    } finally {
      try {
        uploadedInputStream.close();
      } catch (IOException e) {
        // Swallow the exception
      }
    }

    return response;
  }
}
