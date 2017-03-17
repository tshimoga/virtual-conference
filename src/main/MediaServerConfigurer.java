
package main;

import javax.websocket.server.ServerEndpointConfig;

public class MediaServerConfigurer extends ServerEndpointConfig.Configurator{
	
	
	private static final MediaServer MEDIA_SERVER = new MediaServer();

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (MediaServer.class.equals(endpointClass)) {
            return (T) MEDIA_SERVER;
        } else {
            throw new InstantiationException();
        }
    }

}
