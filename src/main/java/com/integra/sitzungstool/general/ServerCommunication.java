package com.integra.sitzungstool.general;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.integra.sitzungstool.model.Integraner;
import com.integra.sitzungstool.model.Sitzung;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerCommunication {

    private static OkHttpClient client;

    public static boolean loginVorstand(String username, String password) {
        try {
            ServerCommunication.client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthenticationInterceptor(username, password))
                    .build();
            Request request = new Request.Builder()
                    .url("https://integranet-dev.integra-ev.de/module/sitzungsanwesenheit/api/anwesenheit-api.php?method=login")
                    .build();
            Response response = ServerCommunication.client.newCall(request).execute();
            boolean wasSuccessful = response.body().string().equals("Verified");
            DataInterface.setHasIntegranetConnection(wasSuccessful);
            return wasSuccessful;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            DataInterface.setHasIntegranetConnection(false);
            return false;
        }
    }

    public static ArrayList<Integraner> getIntegraner() {
        if (DataInterface.hasIntegranetConnection()) {
            try {
                Request request = new Request.Builder()
                        .url("https://integranet-dev.integra-ev.de/module/sitzungsanwesenheit/api/anwesenheit-api.php?method=users")
                        .build();
                Response response = ServerCommunication.client.newCall(request).execute();
                String body = response.body().string();
                Gson gson = new Gson();
                ArrayList<Integraner> integraner = gson.fromJson(body, new TypeToken<List<Integraner>>() {
                }.getType());
                return integraner;
            } catch (JsonSyntaxException | IOException e) {
                System.out.println(e.getMessage());
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    public static String getProfilePicture(String benutzerkennung) {
        if (DataInterface.hasIntegranetConnection()) {
            try {
                Request request = new Request.Builder()
                        .url("https://integranet-dev.integra-ev.de/module/sitzungsanwesenheit/api/anwesenheit-api.php?method=bild&id=" + benutzerkennung)
                        .build();
                Response response = ServerCommunication.client.newCall(request).execute();
                String body = response.body().string();
                if (body.startsWith("data:image/")) {
                    String[] imageStrings = body.split(",");
                    if (imageStrings.length > 1) {
                        return imageStrings[1];
                    }
                    return null;
                }
                return null;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static ArrayList<Sitzung> getSitzungen(String semester) {
        if (DataInterface.hasIntegranetConnection()) {
            try {
                Request request = new Request.Builder()
                        .url("https://integranet-dev.integra-ev.de/module/sitzungsanwesenheit/api/anwesenheit-api.php?method=sitzungen&semester=" + semester)
                        .build();
                Response response = ServerCommunication.client.newCall(request).execute();
                String body = response.body().string();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Sitzung.class, new SitzungDeserializer());
                Gson gson = gsonBuilder.create();
                ArrayList<Sitzung> sitzungen = gson.fromJson(body, new TypeToken<List<Sitzung>>() {
                }.getType());
                return sitzungen;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}
