package devops.tim9.postservice.config.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DisableUserEvent {
	private Integer disableUserEventId;
	private String username;

}
