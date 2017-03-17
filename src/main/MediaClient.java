package main;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@ClientEndpoint
public class MediaClient {

	private static final Gson gson = new GsonBuilder().create();
	private UserRegistry registry = UserRegistry.getInstance();
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		final JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
		String user = jsonMessage.get("user").getAsString();
		UserSession sess = registry.getByName(user);
		sess.sendMessage(jsonMessage);
	}
}
