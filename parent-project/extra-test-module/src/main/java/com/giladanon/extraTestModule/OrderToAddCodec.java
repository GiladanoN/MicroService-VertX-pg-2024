package com.giladanon.extraTestModule;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class OrderToAddCodec implements MessageCodec<OrderToAdd,OrderToAdd> {  

  @Override
  public void encodeToWire(Buffer buffer, OrderToAdd o) {
      System.out.println("encodeToWire");
  }

  @Override
  public OrderToAdd decodeFromWire(int pos, Buffer buffer) {
      System.out.println("decodeFromWire");
      return new OrderToAdd();
  }

  @Override
  public OrderToAdd transform(OrderToAdd o) {
      System.out.println("transform");
      return o;
  }

  @Override
  public String name() {
      return "BrokenSerializedObjectCodec";
  }

  @Override
  public byte systemCodecID() {
      return -1;
  }

}
