
package main;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RoomManager {

  private final Logger log = LoggerFactory.getLogger(RoomManager.class);

  private KurentoClient kurento = KurentoClient.create("ws://10.1.20.129:8888/kurento");

  private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

  private static RoomManager roomManager;
  
  private RoomManager(){}
  
  public static RoomManager getInstance() {
	  if(roomManager==null) {
		  roomManager = new RoomManager();
	  }
	  
	  return roomManager;
  }
  
  public Room getRoom(String roomName) {
    log.debug("Searching for room {}", roomName);
    Room room = rooms.get(roomName);

    if (room == null) {
      log.debug("Room {} not existent. Will create now!", roomName);
      room = new Room(roomName, kurento.createMediaPipeline());
      rooms.put(roomName, room);
    }
    log.debug("Room {} found!", roomName);
    return room;
  }

  
  public void removeRoom(Room room) {
    this.rooms.remove(room.getName());
    room.close();
    log.info("Room {} removed and closed", room.getName());
  }
  
  public ConcurrentMap<String, Room> getRooms() {
	  return rooms;
  }

}
