package com.xployt.model;

public class GenericResponse {
    private Object data;
    private boolean is_successful;
    private String message;
    private String error;

    public GenericResponse() {
    }

    public GenericResponse(Object data, boolean is_successful, String message, String error) {
        this.data = data;
        this.is_successful = is_successful;
        this.message = message;
        this.error = error;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    // @Override
    // public String toString() {
    //     return "GenericResponse{" +
    //             "data=" + data +
    //             ", is_successful=" + is_successful +
    //             ", message='" + message + '\'' +
    //             ", error='" + error + '\'' +
    //             '}';
    // }
}