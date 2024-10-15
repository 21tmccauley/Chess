package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        // Set the correct location of static files
        Spark.staticFiles.location("/resources/web");

        // Register your endpoints and handle exceptions here.
        // TODO: Add your API endpoints here

        Spark.init();
        Spark.awaitInitialization();

        System.out.println("Server started on port " + Spark.port());
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}