package devops.tim9.postservice.config.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AcceptAgentEvent {
	private Integer acceptAgentEventId;
	private String username;

}
