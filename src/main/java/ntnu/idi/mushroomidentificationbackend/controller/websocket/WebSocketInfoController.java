package ntnu.idi.mushroomidentificationbackend.controller.websocket;

import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketInfoController {

  private final SessionRegistry sessionRegistry;

  @GetMapping("/admins/online-count")
  public long getOnlineAdminCount() {
    return sessionRegistry.countActiveGlobalAdmins();
  }
}
