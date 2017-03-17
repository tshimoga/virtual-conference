package main;

import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;



public class UserRegistry {

  private final ConcurrentHashMap<String, UserSession> usersByName = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();
  
  private static UserRegistry registry;
  
  private UserRegistry(){}
  
  public static UserRegistry getInstance() {
	  if(registry==null) {
		  registry = new UserRegistry();
	  }
	  
	  return registry;
  }
  

  public void register(UserSession user) {
    usersByName.put(user.getName(), user);
    usersBySessionId.put(user.getSession().getId(), user);
  }

  public UserSession getByName(String name) {
    return usersByName.get(name);
  }

  public UserSession getBySession(Session session) {
    return usersBySessionId.get(session.getId());
  }

  public boolean exists(String name) {
    return usersByName.keySet().contains(name);
  }

  public UserSession removeBySession(Session session) {
    final UserSession user = getBySession(session);
    usersByName.remove(user.getName());
    usersBySessionId.remove(session.getId());
    return user;
  }
  
  public ConcurrentHashMap<String,UserSession> getUsersByName() {
	  return usersByName;
  }
  
  public ConcurrentHashMap<String,UserSession> getUsersBySession() {
	  return usersBySessionId;
  }
  
}
