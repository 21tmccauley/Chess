package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson;

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
        gson = new Gson();
    }

    public void clear() throws Exception {
        var path = "/db";
        makeRequest("DELETE", path, null, null, null);
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var path = "/user";
        var userData = new UserData(username, password, email);
        return makeRequest("POST", path, userData, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var path = "/session";
        var loginRequest = new UserData(username, password, null);
        return makeRequest("POST", path, loginRequest, null, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        makeRequest("DELETE", path, null, authToken, null);
    }

    public int createGame(String gameName, String authToken) throws Exception {
        var path = "/game";
        var createGameRequest = Map.of("gameName", gameName);
        var response = makeRequest("POST", path, createGameRequest, authToken, Map.class);
        return ((Double) response.get("gameID")).intValue();
    }

    public Collection<GameData> listGames(String authToken) throws Exception {
        var path = "/game";
        record ListGamesResponse(Collection<GameData> games) {}
        var response = makeRequest("GET", path, null, authToken, ListGamesResponse.class);
        return response.games();
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        var path = "/game";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("gameID", gameID);

        // Only add playerColor to request if it's specified
        if (playerColor != null && !playerColor.trim().isEmpty()) {
            requestBody.put("playerColor", playerColor.toUpperCase());
        }

        makeRequest("PUT", path, requestBody, authToken, null);
    }

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws Exception {
        try {
            URL url = new URI(serverUrl + path).toURL();
            System.out.println(url);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method.toUpperCase());
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("Authorization", authToken);
            }

            if (request != null) {
                writeBody(request, http);
            }


            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = gson.fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private static void writeBody(Object request, HttpURLConnection http) throws Exception {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, Exception {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new Exception(readError(http));
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

    private String readError(HttpURLConnection http) throws IOException {
        record ErrorResponse(String message) {
        }
        try (InputStream respBody = http.getErrorStream()) {
            InputStreamReader reader = new InputStreamReader(respBody);
            return gson.fromJson(reader, ErrorResponse.class).message();
        }
    }

    // Add other methods for login, register, etc.
}