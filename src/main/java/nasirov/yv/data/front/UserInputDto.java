package nasirov.yv.data.front;

import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nasirov.yv.data.validator.ValidFanDubSources;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * Created by nasirov.yv
 */
@Data
@Builder
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class UserInputDto {

	@Pattern(regexp = "^[\\w-_]{2,16}$", message = "Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores "
			+ "and dashes only)")
	private String username;

	@ValidFanDubSources
	@NotEmpty(message = "Please specify at least one FanDub source!")
	private Set<FanDubSource> fanDubSources;

	public void setUsername(String username) {
		this.username = StringUtils.lowerCase(username);
	}
}
