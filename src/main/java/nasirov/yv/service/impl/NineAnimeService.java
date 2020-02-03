package nasirov.yv.service.impl;

import static java.lang.String.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static nasirov.yv.data.constants.BaseConstants.FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE;
import static nasirov.yv.data.constants.BaseConstants.NOT_FOUND_ON_FUNDUB_SITE_URL;
import static nasirov.yv.data.constants.FunDubSource.NINEANIME;
import static org.springframework.web.util.UriUtils.encode;

import com.google.common.primitives.Ints;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.Anime;
import nasirov.yv.data.mal.UserMALTitleInfo;
import nasirov.yv.data.properties.UrlsNames;
import nasirov.yv.http.feign.NineAnimeFeignClient;
import nasirov.yv.service.NineAnimeServiceI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NineAnimeService implements NineAnimeServiceI {

	private static final Pattern DATA_ID_PATTERN = Pattern.compile("^/watch/.+\\.(?<dataId>.+)$");

	private static final Pattern EPISODE_NUMBER_PATTERN = Pattern.compile("^.*?(?<episodeNumber>\\d+).*?$");

	private static final String DUB_SUFFIX = " (dub)";

	private final NineAnimeFeignClient nineAnimeFeignClient;

	private final UrlsNames urlsNames;

	@Override
	public Set<Anime> getMatchedAnime(Set<UserMALTitleInfo> watchingTitles) {
		return watchingTitles.stream()
				.map(this::handleTitle)
				.collect(Collectors.toSet());
	}

	private Anime handleTitle(UserMALTitleInfo watchingTitle) {
		String titleName = watchingTitle.getTitle();
		Element searchResultWithTitleLink = getSearchResultWithTitleLink(titleName);
		if (nonNull(searchResultWithTitleLink)) {
			return buildAnime(watchingTitle, searchResultWithTitleLink);
		} else {
			return buildNotFoundAnime(titleName);
		}
	}

	private Anime buildAnime(UserMALTitleInfo watchingTitle, Element elementWithTitleLink) {
		String titleLink = extractTitleLink(elementWithTitleLink);
		String dataId = extractDataId(titleLink);
		String titleEpisodesInfoHtml = getTitleEpisodesInfoHtml(dataId);
		int episode = getNextEpisodeForWatch(watchingTitle);
		String episodeLink = extractEpisodeLink(titleEpisodesInfoHtml, episode);
		Anime result = Anime.builder()
				.funDubSource(NINEANIME)
				.titleName(watchingTitle.getTitle())
				.episode(valueOf(episode))
				.link(episodeLink)
				.build();
		if (result.isAvailable()) {
			log.info("NEW EPISODE AVAILABLE {}", result.getLink());
		} else {
			log.info("NEW EPISODE IS NOT AVAILABLE {}", titleLink);
		}
		return result;
	}

	private Anime buildNotFoundAnime(String titleName) {
		log.error("TITLE [{}] WAS NOT FOUND ON 9Anime!", titleName);
		return Anime.builder()
				.funDubSource(NINEANIME)
				.titleName(titleName)
				.link(NOT_FOUND_ON_FUNDUB_SITE_URL)
				.build();
	}

	private Element getSearchResultWithTitleLink(String titleName) {
		Document document = Jsoup.parse(searchTitleByNameAndGetResultHtml(titleName));
		return document.getElementsByClass("name")
				.stream()
				.filter(x -> isTargetTitle(titleName, x))
				.max(comparing(x -> x.text()
						.length()))
				.orElse(null);
	}

	private String searchTitleByNameAndGetResultHtml(String titleName) {
		return nineAnimeFeignClient.searchTitleByName(encodeTitleName(titleName))
				.getHtml();
	}

	private String encodeTitleName(String titleName) {
		return titleName.contains(";") ? encode(titleName, UTF_8.name()) : titleName;
	}

	private boolean isTargetTitle(String titleName, Element element) {
		String titleNameFromSearchResult = element.text()
				.toLowerCase();
		return titleNameFromSearchResult.equals(titleName + DUB_SUFFIX) || titleNameFromSearchResult.equals(titleName);
	}

	private String extractTitleLink(Element elementWithTitleLink) {
		return elementWithTitleLink.attr("href");
	}

	private String extractDataId(String titleLink) {
		Matcher matcher = DATA_ID_PATTERN.matcher(titleLink);
		return matcher.find() ? matcher.group("dataId") : "";
	}

	private String getTitleEpisodesInfoHtml(String dataId) {
		return nineAnimeFeignClient.getTitleEpisodesInfo(dataId)
				.getHtml();
	}

	private String extractEpisodeLink(String titleEpisodesInfoHtml, int nextEpisodeForWatch) {
		Document document = Jsoup.parse(titleEpisodesInfoHtml);
		return document.select(".episodes.range > li > a")
				.stream()
				.filter(x -> isNewEpisodeAvailable(nextEpisodeForWatch, x))
				.map(x -> x.attr("href"))
				.map(x -> urlsNames.getNineAnimeUrls()
						.getNineAnimeTo() + x)
				.findFirst()
				.orElse(FINAL_URL_VALUE_IF_EPISODE_IS_NOT_AVAILABLE);
	}
	private boolean isNewEpisodeAvailable(int nextEpisodeForWatch, Element element) {
		String episodeNumber = element.text();
		Integer parsedEpisodeNumber = Ints.tryParse(episodeNumber);
		if (isNull(parsedEpisodeNumber)) {
			parsedEpisodeNumber = extractEpisodeInExtraWay(episodeNumber);
		}
		return parsedEpisodeNumber == nextEpisodeForWatch;
	}

	private int extractEpisodeInExtraWay(String episodeNumber) {
		Integer result = extractEpisodeViaRegexp(episodeNumber);
		return nonNull(result) ? result : extractEpisodeViaStubConstant(episodeNumber);
	}

	private Integer extractEpisodeViaRegexp(String episodeNumber) {
		Matcher matcher = EPISODE_NUMBER_PATTERN.matcher(episodeNumber);
		Integer parsedEpisodeNumber = null;
		if (matcher.find()) {
			String extractedEpisodeViaRegexp = matcher.group("episodeNumber");
			parsedEpisodeNumber = Integer.parseInt(extractedEpisodeViaRegexp);
			log.info("EPISODE_NUMBER_PATTERN extracted episode via regexp [{}] parsed [{}] from [{}]",
					extractedEpisodeViaRegexp,
					parsedEpisodeNumber,
					episodeNumber);
		}
		return parsedEpisodeNumber;
	}

	private int extractEpisodeViaStubConstant(String episodeNumber) {
		int result = 0;
		if ("Full".equals(episodeNumber)) {
			result = 1;
		} else {
			log.error("UNKNOWN EPISODE STUB CONSTANT [{}]!", episodeNumber);
		}
		return result;
	}

	private int getNextEpisodeForWatch(UserMALTitleInfo watchingTitle) {
		return watchingTitle.getNumWatchedEpisodes() + 1;
	}
}
