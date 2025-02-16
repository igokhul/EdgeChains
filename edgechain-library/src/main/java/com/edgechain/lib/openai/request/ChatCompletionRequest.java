package com.edgechain.lib.openai.request;

import java.util.List;
import java.util.Objects;

public class ChatCompletionRequest {

  private String model;
  private Double temperature;
  private List<ChatMessage> messages;

  public ChatCompletionRequest(String model, List<ChatMessage> messages, Double temperature) {
    this.model = model;
    this.messages = messages;
    this.temperature = Objects.isNull(temperature) ? 0.7 : temperature;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public List<ChatMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<ChatMessage> messages) {
    this.messages = messages;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ChatCompletionRequest{");
    sb.append("model='").append(model).append('\'');
    sb.append(", temperature=").append(temperature);
    sb.append(", messages=").append(messages);
    sb.append('}');
    return sb.toString();
  }

  public static ChatCompletionRequestBuilder builder() {
    return new ChatCompletionRequestBuilder();
  }

  public static class ChatCompletionRequestBuilder {
    private String model;
    private Double temperature;
    private List<ChatMessage> messages;

    public ChatCompletionRequestBuilder model(String model) {
      this.model = model;
      return this;
    }

    public ChatCompletionRequestBuilder temperature(Double temperature) {
      this.temperature = temperature;
      return this;
    }

    public ChatCompletionRequestBuilder messages(List<ChatMessage> messages) {
      this.messages = messages;
      return this;
    }

    public ChatCompletionRequest build() {
      return new ChatCompletionRequest(model, messages, temperature);
    }
  }
}
