package nasirov.yv.http.fallback;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.nine_anime.SearchDto;
import nasirov.yv.http.feign.NineAnimeFeignClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NineAnimeFeignClientFallbackFactory implements FallbackFactory<NineAnimeFeignClient> {

	@Override
	public NineAnimeFeignClient create(Throwable cause) {
		return new NineAnimeFeignClient() {
			@Override
			public SearchDto searchTitleByName(String titleName) {
				log.error("NineAnimeFeignClient fallback during call /ajax/film/search?keyword={} | Cause message [{}]", titleName, cause.getMessage());
				return buildSafeSearchDto();
			}
			@Override
			public SearchDto getTitleEpisodesInfo(String dataId) {
				log.error("NineAnimeFeignClient fallback during call /ajax/film/servers/{} | Cause message [{}]", dataId, cause.getMessage());
				return buildSafeSearchDto();
			}
		};
	}

	private SearchDto buildSafeSearchDto() {
		return new SearchDto("");
	}
}
