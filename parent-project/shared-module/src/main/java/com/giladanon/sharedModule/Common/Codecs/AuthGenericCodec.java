package com.giladanon.sharedModule.Common.Codecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;

/**
 * @author https://gist.github.com/OneManCrew/f4665f4c52f26f72034b597c7909e43b#file-genericcodec-java
 * @see https://levidoro.medium.com/vert-x-event-bus-send-any-object-with-generic-codec-t-a0bc1feab13a
 */
public class AuthGenericCodec<T> implements MessageCodec<T, T> {
    private final Class<T> cls;
    public AuthGenericCodec(Class<T> cls) {
        super();
        this.cls = cls;
    }
    
    
    @Override
    public void encodeToWire(Buffer buffer, T s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;

        System.out.println("Data as json: " + Json.encode(s));

        // new JsonSchema()

        Json.load();

        Buffer jsonBuffer = Json.encodeToBuffer(s);
        buffer.appendInt(jsonBuffer.length());
        System.out.println("Written len=" + jsonBuffer.length());
        buffer.appendBuffer(jsonBuffer);
        System.out.println("Written json=" + jsonBuffer.toString());

        // JsonObject decoded = (JsonObject) Json.decodeValue(jsonBuffer);
        // T backToObj = decoded.mapTo(cls);
        // System.out.println(backToObj);
        // System.out.println(backToObj.getClass());

        System.out.println("Successfylly encoded message to wire !");
    }

    @Override
    public T decodeFromWire(int pos, Buffer buffer) {
        // // My custom message starting from this *position* of buffer
        // int _pos = pos;

        // // Length of JSON
        // int length = buffer.getInt(_pos);

        // // Jump 4 because getInt() == 4 bytes
        // byte[] yourBytes = buffer.getBytes(_pos += 4, _pos += length);
        // ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
        // try {
        //     ObjectInputStream ois = new ObjectInputStream(bis);
        //     @SuppressWarnings("unchecked")
        //     T msg = (T) ois.readObject();
        //     ois.close();
        //     return msg;
        // } catch (IOException | ClassNotFoundException e) {
        // 	System.out.println("Listen failed "+e.getMessage());
        // } finally {
        //     try {
        //         bis.close();
        //     } catch (IOException e) {}
        // }
        return null;
    }

    @Override
    public T transform(T customMessage) {
        // If a message is sent *locally* across the event bus.
        // This example sends message just as is
        return customMessage;
    }

    @Override
    public String name() {
        // Each codec must have a unique name.
        // This is used to identify a codec when sending a message and for unregistering
        // codecs.
        System.out.println("CODEC CLASS NAME: " + cls.getSimpleName()+"Codec");
        return cls.getSimpleName()+"Codec";
    }

    @Override
    public byte systemCodecID() {
        // Always -1
        return -1;
    }
}
