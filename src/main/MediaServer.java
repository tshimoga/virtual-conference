package main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.ServerEndpoint;

import org.kurento.client.IceCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@ServerEndpoint(value = "/groupcall", configurator = MediaServerConfigurer.class)
public class MediaServer {

	private static final Logger log = LoggerFactory
			.getLogger(MediaServer.class);

	private static final Gson gson = new GsonBuilder().create();

	private RoomManager roomManager = RoomManager.getInstance();

	private UserRegistry registry = UserRegistry.getInstance();

	@OnMessage
	public void onMessage(ByteBuffer message, Session session) throws Exception {
		byte[] yourBytes = new byte[message.capacity()];
		ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			UserSession o = (UserSession) in.readObject();
			o.getRoomName();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
			}
		}
	}

	@OnMessage
	public void onMessage(String message, Session session) throws Exception {
		final JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
		UserSession user=null;
		//Session clientSession = getServerSession();
		switch (jsonMessage.get("id").getAsString()) {
		case "joinRoom":
			joinRoom(jsonMessage, session);
			/*if (!jsonMessage.has("fromServer")) {
				
				jsonMessage.addProperty("fromServer", true);
				//clientSession.getBasicRemote().sendText(jsonMessage.getAsString());
			}*/ 
			break;
		case "receiveVideoFrom":
			final String senderName = jsonMessage.get("sender").getAsString();
			final UserSession sender = registry.getByName(senderName);
			final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
			user = registry.getBySession(session);
			if(user!=null) {
				if(jsonMessage.has("direct")) {
					sender.sendMessage(jsonMessage);
				} else {
					user.receiveVideoFrom(sender, sdpOffer);
				}
				
			}
			
			/*if (!jsonMessage.has("fromServer")) {
				jsonMessage.addProperty("fromServer", true);
				clientSession.getBasicRemote().sendText(jsonMessage.getAsString());
			}*/
			break;
		case "leaveRoom":
			user = registry.getBySession(session);
			leaveRoom(user);
			/*if (!jsonMessage.has("fromServer")) {
				jsonMessage.addProperty("fromServer", true);
				clientSession.getBasicRemote().sendText(jsonMessage.getAsString());
			}*/
			break;
		case "onIceCandidate":
			JsonObject candidate = jsonMessage.get("candidate")
					.getAsJsonObject();
			user = registry.getBySession(session);
			if (user != null) {
				IceCandidate cand = new IceCandidate(candidate.get("candidate")
						.getAsString(), candidate.get("sdpMid").getAsString(),
						candidate.get("sdpMLineIndex").getAsInt());
				user.addCandidate(cand, jsonMessage.get("name").getAsString());
			}
			/*if (!jsonMessage.has("fromServer")) {
				jsonMessage.addProperty("fromServer", true);
				clientSession.getBasicRemote().sendText(jsonMessage.getAsString());
			}*/
			
			break;
		default:
			break;
		}
	}

	public Session getServerSession() throws DeploymentException, IOException,
			URISyntaxException {
		WebSocketContainer container = ContainerProvider
				.getWebSocketContainer();
		URI uri = new URI("ws://10.140.144.23:8080/GroupCall/groupcall");
		Session session = container.connectToServer(MediaClient.class, uri);
		return session;

	}
	
	

	@OnClose
	public void onClose(Session session) throws IOException {
		UserSession user = registry.removeBySession(session);
		roomManager.getRoom(user.getRoomName()).leave(user);
	}

	private UserSession joinRoom(JsonObject params, Session session)
			throws IOException {
		final String roomName = params.get("room").getAsString();
		final String name = params.get("name").getAsString();
		log.info("PARTICIPANT {}: trying to join room {}", name, roomName);

		Room room = roomManager.getRoom(roomName);
		final UserSession user = room.join(name, session);
		registry.register(user);
		return user;
	}

	private void leaveRoom(UserSession user) throws IOException {
		final Room room = roomManager.getRoom(user.getRoomName());
		room.leave(user);
		if (room.getParticipants().isEmpty()) {
			roomManager.removeRoom(room);
		}
	}
}
