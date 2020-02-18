package se.kth.karamel.webservice.calls.deployment;

import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.node.Node;
import se.kth.karamel.core.execution.Task;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/deployment")
public class DeploymentService extends AbstractCall {

  public DeploymentService(KaramelApi karamelApi) {
    super(karamelApi);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDeploymentStatus() {
    Map<Node, List<Task>> deploymentStatus = karamelApi.getClusterDeploymentStatus();

    // For the computation, duplicate the name as key in the map and in the GroupStatusDTO object
    Map<String, GroupStatusDTO> groupStatusDTOMap = new HashMap<>();
    deploymentStatus.entrySet().forEach(e -> {
        // convert task list
        List<TaskStatusDTO> taskStatusDTOList =
          e.getValue().stream().map(TaskStatusDTO::new)
            .sorted(Comparator.comparingInt(TaskStatusDTO::getTaskId))
            .collect(Collectors.toList());
        // convert node information
        NodeStatusDTO nodeStatusDTO = new NodeStatusDTO(e.getKey(), taskStatusDTOList);
        // Add the result into the map
        if (groupStatusDTOMap.containsKey(e.getKey().getGroup().getName())) {
          groupStatusDTOMap.get(e.getKey().getGroup().getName()).nodeStatusList.add(nodeStatusDTO);
        } else {
          GroupStatusDTO groupStatusDTO = new GroupStatusDTO(e.getKey().getGroup().getName(),
            Arrays.asList(nodeStatusDTO));
          groupStatusDTOMap.put(e.getKey().getGroup().getName(), groupStatusDTO);
        }
      });

    // Return a set instead of a map, the group name is in the GroupStatusDTO Object
    return Response.ok().entity(groupStatusDTOMap.values()).build();
  }


  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response action(@NotNull @QueryParam("action") DeploymentActions action,
                         @QueryParam("task") Integer task,
                         @QueryParam("node") Integer node,
                         @QueryParam("group") String group)
    throws KaramelException {

    switch (action) {
      case RESUME:
        karamelApi.resume(task, node, group);
        break;
      case PAUSE:
        karamelApi.pause(task, node, group);
        break;
      case RETRY:
        karamelApi.retry(task, node, group);
        break;
      case SKIP:
        karamelApi.skip(task, node, group);
        break;
      default:
        throw new NotImplementedException("Unknown action");
    }

    return Response.ok().build();
  }
}
