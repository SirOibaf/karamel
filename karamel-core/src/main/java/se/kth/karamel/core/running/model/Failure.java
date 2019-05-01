/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.core.running.model;

/**
 *
 * @author kamal
 */
public class Failure {

  public static enum Type {

    TASK_FAILED, CLEANUP_FAILE, CREATING_SEC_GROUPS_FAILE, DAG_FAILURE, 
    FORK_MACHINE_FAILURE, SSH_KEY_NOT_AUTH;
  }

  private Type type;
  private String message;
  private String id;

  public Failure(Type code, String message) {
    this.type = code;
    this.message = message;
  }

  public Failure(Type code, String id, String message) {
    this.type = code;
    this.message = message;
    this.id = id;
  }

  public Type getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public String getId() {
    return id;
  }

  public String hash() {
    return (id == null) ? type.name() : type.name() + id;
  }

  public static String hash(Type type, String id) {
    return (id == null) ? type.name() : type.name() + id;
  }
}
