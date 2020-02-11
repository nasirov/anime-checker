package nasirov.yv.data.mal;

import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.Data;
import nasirov.yv.data.constants.FunDubSource;
import org.springframework.validation.annotation.Validated;

/**
 * Created by nasirov.yv
 */
@Data
@Validated
public class MALUser {

	@Pattern(regexp = "^[\\w-]{2,16}$", message = "Please enter a valid mal username between 2 and 16 characters(latin letters, numbers, underscores "
			+ "and dashes only)")
	private String username;

	@NotEmpty(message = "Please specify at least one FunDub source!")
	private Set<FunDubSource> funDubSources;
}
