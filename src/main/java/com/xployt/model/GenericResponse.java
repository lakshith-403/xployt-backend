package com.xployt.model;

public class GenericResponse {
  private Object data;
  private boolean is_successful;
  private String error;
  private String trace;

  public GenericResponse() {
  }

  public GenericResponse(Object data, boolean is_successful, String error, String trace) {
      this.data = data;
      this.is_successful = is_successful;
      this.error = error;
      this.trace = trace;
  }
  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  public boolean isIs_successful() {
    return is_successful;
  }

  public void setIs_successful(boolean is_successful) {
    this.is_successful = is_successful;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getTrace() {
    return trace;
  }

  public void setTrace(String trace) {
    this.trace = trace;
  }

  // public String toString() {
  // return "{" +
  // "data=" + data +
  // ", is_successful=" + is_successful +
  // ", error='" + error + '\'' +
  // ", trace='" + trace + '\'' +
  // '}';
  // }
}