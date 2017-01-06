package com.bwfcwalshy.flarebot.web;

import org.apache.commons.lang3.exception.ExceptionUtils;
import spark.Spark;

public class ApiFactory {
    public static void bind() {
        Spark.get("/data/:provider", (request, response) -> {
            try {
                DataProviders provider;
                try {
                    provider = DataProviders.valueOf(request.params("provider").toUpperCase());
                } catch (Exception e) {
                    response.status(404);
                    return "Unknown provider: " + request.params("provider");
                }
                return provider.process(request, response);
            } catch (Throwable e) {
                response.status(500);
                return "<pre>" + ExceptionUtils.getStackTrace(e) + "<pre>";
            }
        });
        Spark.post("/setters/:provider", (request, response) -> {
            try {
                DataSetters setters;
                try {
                    setters = DataSetters.valueOf(request.params("provider").toUpperCase());
                } catch (Exception e) {
                    response.status(404);
                    return "Unknown setter: " + request.params("provider");
                }
                return setters.process(request, response);
            } catch (Throwable e) {
                response.status(500);
                return "<pre>" + ExceptionUtils.getStackTrace(e) + "<pre>";
            }
        });
    }
}