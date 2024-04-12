package com.giladanon.extraTestModule;

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

/**
 * @author https://gist.github.com/OneManCrew/f4665f4c52f26f72034b597c7909e43b#file-genericcodec-java
 * @see https://levidoro.medium.com/vert-x-event-bus-send-any-object-with-generic-codec-t-a0bc1feab13a
 */
public class GenericCodec<T> implements MessageCodec<T, T> {
    private final Class<T> cls;
    public GenericCodec(Class<T> cls) {
        super();
        this.cls = cls;
    }
    
    
    @Override
    public void encodeToWire(Buffer buffer, T s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(s);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            buffer.appendInt(yourBytes.length);
            buffer.appendBytes(yourBytes);
            out.close();
        } catch (IOException e) {
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {}
        }        
    }

    @Override
    public T decodeFromWire(int pos, Buffer buffer) {
        // My custom message starting from this *position* of buffer
        int _pos = pos;

        // Length of JSON
        int length = buffer.getInt(_pos);
        System.out.println("Reciever: reading int ==> len=" + length);

        // Jump 4 because getInt() == 4 bytes
        Buffer dataBuffer = buffer.getBuffer(_pos += 4, _pos += length);
        System.out.println("Reciever: Got relevant bytes ==> data.len=" + dataBuffer.length());

        JsonObject decoded = (JsonObject) Json.decodeValue(dataBuffer);
        T backToObj = decoded.mapTo(cls);
        System.out.println(backToObj);
        System.out.println(backToObj.getClass());

        return backToObj;

        // ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
        // System.out.println("Reciever: ByteArrayInputStream created from bytes");
        // try {
        //     ObjectInputStream ois = new ObjectInputStream(bis);
        //     @SuppressWarnings("unchecked")
        //     T msg = (T) ois.readObject();
        //     System.out.println("Reciever: raed & created (T)msg object successfylly");
        //     ois.close();
        //     return msg;
        // } catch (IOException | ClassNotFoundException e) {
        // 	System.out.println("Listen failed "+e.getMessage());
        //     System.out.println(e.getCause());
        //     e.printStackTrace();
        //     System.out.println("Reciever: END OF CATCH BLOCK");
        // } finally {
        //     try {
        //         bis.close();
        //         System.out.println("Reciever: ByteArrayInputStream closed.");
        //     } catch (IOException e) {
        //         System.out.println("Reciever: ByteArrayInputStream error while closing");
        //         System.out.println(e.getMessage());
        //     }
        // }
        // return null;
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
