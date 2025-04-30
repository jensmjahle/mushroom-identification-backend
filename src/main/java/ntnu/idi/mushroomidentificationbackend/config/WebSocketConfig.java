package ntnu.idi.mushroomidentificationbackend.config;

import ntnu.idi.mushroomidentificationbackend.security.WebSocketAuthInterceptor;
import ntnu.idi.mushroomidentificationbackend.security.WebSocketHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;
  private final WebSocketAuthInterceptor webSocketAuthInterceptor;

  public WebSocketConfig(WebSocketHandshakeInterceptor webSocketHandshakeInterceptor,
      WebSocketAuthInterceptor webSocketAuthInterceptor) {
    this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
    this.webSocketAuthInterceptor = webSocketAuthInterceptor;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic","/queue"); // Clients subscribe to this topic
    config.setApplicationDestinationPrefixes("/app"); // Clients send messages here
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws") // WebSocket connection URL
        .setAllowedOriginPatterns("*") // Allow frontend connections
        .addInterceptors(webSocketHandshakeInterceptor); // Apply the handshake interceptor
  }

  
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(webSocketAuthInterceptor); 
  }
}
