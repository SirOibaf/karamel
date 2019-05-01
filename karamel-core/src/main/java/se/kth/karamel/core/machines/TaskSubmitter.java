/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.core.machines;

import se.kth.karamel.core.running.model.tasks.Task;
import se.kth.karamel.common.exception.KaramelException;

public interface TaskSubmitter {

  void prepareToStart(Task task) throws KaramelException;

  void submitTask(Task task) throws KaramelException;
  
  void killMe(Task task) throws KaramelException;
  
  void retryMe(Task task) throws KaramelException;

  void skipMe(Task task) throws KaramelException;

  void terminate(Task task) throws KaramelException;
}
