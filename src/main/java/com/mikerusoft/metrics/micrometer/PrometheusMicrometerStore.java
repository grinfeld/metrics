package com.mikerusoft.metrics.micrometer;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

@Slf4j
public class PrometheusMicrometerStore extends MicrometerStore {

    public PrometheusMicrometerStore(String prefix, boolean createWebEndpoint, int port, boolean disableHost) {
        super(prefix, new PrometheusMeterRegistry(new PrometheusConfig() {
            @Override
            public String prefix() {
                return prefix;
            }

            @Override
            public String get(String key) {
                return null;
            }
        }), disableHost);
        if (createWebEndpoint) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
                server.createContext("/metrics", httpExchange -> {
                    String response = ((PrometheusMeterRegistry) this.registry).scrape();
                    httpExchange.sendResponseHeaders(200, response.getBytes().length);
                    try (OutputStream os = httpExchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                });

                new Thread(server::start).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
