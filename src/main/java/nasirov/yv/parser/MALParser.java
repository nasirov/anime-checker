package nasirov.yv.parser;

import nasirov.yv.exception.JSONNotFoundException;
import nasirov.yv.exception.MALUserAccountNotFoundException;
import nasirov.yv.exception.MALUserAnimeListAccessException;
import nasirov.yv.response.HttpResponse;
import nasirov.yv.serialization.UserMALTitleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser MAL html
 * Created by Хикка on 20.12.2018.
 */
@Component
public class MALParser {
	private static final Logger logger = LoggerFactory.getLogger(MALParser.class);
	
	private static final String JSON_ANIME_LIST = "<table class=\"list-table\" data-items=\"(?<jsonAnimeList>.*)\">";
	
	private static final String USER_ANIME_LIST_PRIVATE_ACCESS = "Access to this list has been restricted by the owner";
	
	private static final String NUMBER_OF_WATCHING_TITLES = "Watching</a><span class=\"di-ib fl-r lh10\">(?<numWatchingTitles>\\d*?)</span>";
	
	private WrappedObjectMapper wrappedObjectMapper;
	
	@Autowired
	public MALParser(WrappedObjectMapper wrappedObjectMapper) {
		this.wrappedObjectMapper = wrappedObjectMapper;
	}
	
	/**
	 * Search for user anime list
	 *
	 * @param response   mal response
	 * @param collection any collection
	 * @param <T>        class extends collection
	 * @return collection with user anime titles
	 * @throws MALUserAnimeListAccessException if user anime list has private access
	 * @throws JSONNotFoundException           if json anime list not found
	 */
	public <T extends Collection> T getUserTitlesInfo(HttpResponse response, Class<T> collection) throws MALUserAnimeListAccessException, JSONNotFoundException {
		if (response == null) {
			logger.error("MALResponse must be not null!");
			throw new RuntimeException("ClientResponse must be not null!");
		}
		logger.debug("Start Parsing");
		T malTitlesInfo = wrappedObjectMapper.unmarshal(getJsonAnimeListFromHtml(response.getContent()), UserMALTitleInfo.class, collection);
		logger.debug("End Parsing");
		return malTitlesInfo;
	}
	
	/**
	 * Search "Currently Watching" titles in user profile html
	 *
	 * @param response mal response
	 * @return number of watching titles
	 * @throws MALUserAccountNotFoundException if user not found
	 */
	public String getNumWatchingTitles(HttpResponse response) throws MALUserAccountNotFoundException {
		if (response == null) {
			logger.error("MALResponse must be not null!");
			throw new RuntimeException("ClientResponse must be not null!");
		}
		if (!isAccountExist(response)) {
			throw new MALUserAccountNotFoundException("MAL User Account Not Found!");
		}
		Pattern pattern = Pattern.compile(NUMBER_OF_WATCHING_TITLES);
		Matcher matcher = pattern.matcher(response.getContent());
		if (matcher.find()) {
			return matcher.group("numWatchingTitles");
		}
		return null;
	}
	
	/**
	 * Check user
	 *
	 * @param response mal response
	 * @return true if user exists
	 */
	private boolean isAccountExist(HttpResponse response) {
		return !response.getStatus().equals(HttpStatus.NOT_FOUND.value());
	}
	
	/**
	 * Search in mal html json anime list
	 *
	 * @param content mal html
	 * @return string json anime list
	 * @throws JSONNotFoundException           if json not found
	 * @throws MALUserAnimeListAccessException if user anime list has private access
	 */
	private String getJsonAnimeListFromHtml(String content) throws JSONNotFoundException, MALUserAnimeListAccessException {
		logger.debug("Start Searching JSON in html");
		String jsonAnimeList;
		Pattern pattern = Pattern.compile(JSON_ANIME_LIST);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			jsonAnimeList = matcher.group("jsonAnimeList").replaceAll("&quot;", "\"").replaceAll("&#039;", "'");
		} else if (content.contains(USER_ANIME_LIST_PRIVATE_ACCESS)) {
			logger.error(USER_ANIME_LIST_PRIVATE_ACCESS);
			throw new MALUserAnimeListAccessException(USER_ANIME_LIST_PRIVATE_ACCESS);
		} else {
			logger.error("JSON not found");
			throw new JSONNotFoundException(content);
		}
		logger.debug("End Searching JSON in html");
		return jsonAnimeList;
	}
}
