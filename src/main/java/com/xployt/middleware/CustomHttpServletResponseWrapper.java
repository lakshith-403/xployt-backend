package com.xployt.middleware;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CustomHttpServletResponseWrapper extends HttpServletResponseWrapper {
  private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private PrintWriter writer = new PrintWriter(outputStream);

  public CustomHttpServletResponseWrapper(HttpServletResponse response) {
    super(response);
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
        outputStream.write(b);
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setWriteListener(WriteListener writeListener) {
        // No-op
      }
    };
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return writer;
  }

  @Override
  public void flushBuffer() throws IOException {
    super.flushBuffer();
    writer.flush();
  }

  public String getContent() {
    writer.flush();
    return outputStream.toString();
  }
}