package nasirov.yv.http.feign;

import java.util.List;
import nasirov.yv.data.anime_pik.site.AnimePikEpisode;
import nasirov.yv.http.config.FeignClientConfig;
import nasirov.yv.http.fallback.AnimePikResourcesFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by nasirov.yv
 */
@FeignClient(value = "anime-pik-resources-feign-client", configuration = FeignClientConfig.class, fallbackFactory =
		AnimePikResourcesFeignClientFallbackFactory.class)
@RequestMapping(headers = {"User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0", "Accept-Encoding=gzip, br"})
public interface AnimePikResourcesFeignClient {

	@GetMapping(value = "/{titleId}.txt", produces = "text/html; charset=UTF-8")
	List<AnimePikEpisode> getTitleEpisodes(@PathVariable int titleId);
}
