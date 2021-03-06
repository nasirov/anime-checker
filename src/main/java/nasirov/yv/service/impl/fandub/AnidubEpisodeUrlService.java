package nasirov.yv.service.impl.fandub;

import java.util.List;
import nasirov.yv.data.properties.CommonProps;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.AnidubParserI;
import nasirov.yv.fandub.service.spring.boot.starter.properties.FanDubProps;
import nasirov.yv.fandub.service.spring.boot.starter.service.HttpRequestServiceI;
import nasirov.yv.service.HttpRequestServiceDtoBuilderI;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@Service
public class AnidubEpisodeUrlService extends AbstractEpisodeUrlService {

	private final AnidubParserI anidubParser;

	public AnidubEpisodeUrlService(FanDubProps fanDubProps, CommonProps commonProps, AnidubParserI anidubParser,
			HttpRequestServiceI httpRequestService,
			HttpRequestServiceDtoBuilderI httpRequestServiceDtoBuilder) {
		super(fanDubProps, commonProps, httpRequestService, httpRequestServiceDtoBuilder, FanDubSource.ANIDUB);
		this.anidubParser = anidubParser;
	}

	@Override
	protected Mono<List<FandubEpisode>> getEpisodes(CommonTitle commonTitle) {
		return httpRequestService.performHttpRequest(httpRequestServiceDtoBuilder.anidub(commonTitle))
				.map(Jsoup::parse)
				.map(anidubParser::extractEpisodes);
	}

}
