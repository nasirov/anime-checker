package nasirov.yv.service.impl.fandub;

import static nasirov.yv.utils.TestConstants.JISEDAI_URL;
import static nasirov.yv.utils.TestConstants.REGULAR_TITLE_JISEDAI_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.Lists;
import java.util.List;
import nasirov.yv.fandub.service.spring.boot.starter.constant.FanDubSource;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.CommonTitle;
import nasirov.yv.fandub.service.spring.boot.starter.dto.fandub.common.FandubEpisode;
import nasirov.yv.fandub.service.spring.boot.starter.dto.http_request_service.HttpRequestServiceDto;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.EpisodesExtractorI;
import nasirov.yv.fandub.service.spring.boot.starter.extractor.parser.JisedaiParserI;
import nasirov.yv.service.EpisodeUrlServiceI;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;

/**
 * @author Nasirov Yuriy
 */
@RunWith(MockitoJUnitRunner.class)
public class JisedaiEpisodeUrlServiceTest extends AbstractEpisodeUrlsServiceTest {

	@Mock
	private JisedaiParserI jisedaiParser;

	@InjectMocks
	private JisedaiEpisodeUrlService jisedaiEpisodeUrlService;

	@Test
	@Override
	public void shouldReturnUrlWithAvailableEpisode() {
		super.shouldReturnUrlWithAvailableEpisode();
	}

	@Test
	@Override
	public void shouldReturnUrlWithAvailableEpisodeInRuntime() {
		super.shouldReturnUrlWithAvailableEpisodeInRuntime();
	}

	@Test
	@Override
	public void shouldReturnNotFoundOnFandubSiteUrl() {
		super.shouldReturnNotFoundOnFandubSiteUrl();
	}

	@Test
	@Override
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailable() {
		super.shouldReturnFinalUrlValueIfEpisodeIsNotAvailable();
	}

	@Test
	@Override
	public void shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime() {
		super.shouldReturnFinalUrlValueIfEpisodeIsNotAvailableInRuntime();
	}

	@Override
	protected String getFandubUrl() {
		return JISEDAI_URL;
	}

	@Override
	protected EpisodesExtractorI<Document> getParser() {
		return jisedaiParser;
	}

	@Override
	protected void mockGetTitlePage(String titlePageContent, CommonTitle commonTitle) {
		HttpRequestServiceDto<String> httpRequestServiceDto = mock(HttpRequestServiceDto.class);
		doReturn(httpRequestServiceDto).when(httpRequestServiceDtoBuilder)
				.jisedai(commonTitle);
		doReturn(Mono.just(titlePageContent)).when(httpRequestService)
				.performHttpRequest(httpRequestServiceDto);
	}

	@Override
	protected EpisodeUrlServiceI getEpisodeUrlService() {
		return jisedaiEpisodeUrlService;
	}

	@Override
	protected FanDubSource getFandubSource() {
		return FanDubSource.JISEDAI;
	}

	@Override
	protected List<FandubEpisode> getFandubEpisodes() {
		return Lists.newArrayList(FandubEpisode.builder()
						.name("1 эпизод")
						.id(1)
						.number("1")
						.url(REGULAR_TITLE_JISEDAI_URL)
						.build(),
				FandubEpisode.builder()
						.name("2 эпизод")
						.id(2)
						.number("2")
						.url(REGULAR_TITLE_JISEDAI_URL)
						.build());
	}

	@Override
	protected void checkUrlWithAvailableEpisode(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_JISEDAI_URL, actualUrl);
	}

	@Override
	protected void checkUrlWithAvailableEpisodeInRuntime(String actualUrl) {
		assertEquals(getFandubUrl() + REGULAR_TITLE_JISEDAI_URL, actualUrl);
	}
}