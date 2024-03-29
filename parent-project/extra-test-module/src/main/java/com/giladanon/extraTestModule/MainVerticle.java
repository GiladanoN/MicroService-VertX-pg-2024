package com.giladanon.extraTestModule;

import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        System.out.println("Hello! from '" + this.getClass().getCanonicalName() + "' ~");
    }

}
