package nasirov.yv.service.impl.fandub.animedia;

import static nasirov.yv.data.constants.BaseConstants.JOINED_EPISODES_PATTERN;
import static nasirov.yv.util.AnimediaUtils.getCorrectCurrentMax;
import static nasirov.yv.util.AnimediaUtils.getCorrectFirstEpisodeAndMin;
import static nasirov.yv.util.AnimediaUtils.getFirstEpisode;
import static nasirov.yv.util.AnimediaUtils.getLastEpisode;
import static nasirov.yv.util.AnimediaUtils.isTitleConcretizedAndOngoing;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;
import static nasirov.yv.util.AnimediaUtils.isTitleUpdated;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaTitle;
import nasirov.yv.parser.AnimediaEpisodeParserI;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.AnimediaTitlesUpdateServiceI;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnimediaTitlesUpdateService implements AnimediaTitlesUpdateServiceI {

	private final AnimediaEpisodeParserI animediaEpisodeParser;

	private final AnimediaServiceI animediaService;

	/**
	 * Updates given animedia titles
	 *
	 * @param animediaTitles animedia titles for update
	 */
	@Override
	public void updateAnimediaTitles(Set<AnimediaTitle> animediaTitles) {
		animediaTitles.stream()
				.filter(this::isTitleNeedUpdate)
				.forEach(this::handleTitle);
	}

	private boolean isTitleNeedUpdate(AnimediaTitle animediaTitle) {
		return !(isTitleUpdated(animediaTitle) || isTitleNotFoundOnMAL(animediaTitle));
	}

	private void handleTitle(AnimediaTitle animediaTitle) {
		List<String> episodesList = animediaService.getEpisodes(animediaTitle.getAnimeIdOnAnimedia(), animediaTitle.getDataListOnAnimedia());
		if (episodesList.isEmpty()) {
			return;
		}
		List<String> episodesRange = episodesList.stream()
				.map(animediaEpisodeParser::extractEpisodeNumber)
				.collect(Collectors.toList());
		if (isTitleConcretizedAndOngoing(animediaTitle)) {
			enrichConcretizedAndOngoingTitle(animediaTitle, episodesRange);
		} else {
			enrichRegularTitle(animediaTitle, episodesRange);
		}
	}

	private void enrichRegularTitle(AnimediaTitle animediaTitle, List<String> episodesList) {
		String correctFirstEpisodeAndMin = getCorrectFirstEpisodeAndMin(getFirstEpisode(episodesList));
		String correctCurrentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		//если в дата листах суммируют первую серию и последнюю с предыдущего дата листа, то нужна проверка для правильного максимума
		//например, всего серий ххх, 1 даталист: серии 1 из 100; 2 дата лист: серии 51 из 100
		animediaTitle.setMinOnAnimedia(correctFirstEpisodeAndMin);
		// TODO: 17.12.2019 uncomment and implement when animedia improve api object that will return max episode in season
//		animediaTitle.setMaxConcretizedEpisodeOnAnimedia("correctMaxInDataList");
		episodesList.stream()
				.filter(x -> JOINED_EPISODES_PATTERN.matcher(x)
						.find())
				.findFirst()
				.ifPresent(x -> animediaTitle.setEpisodesRangeOnAnimedia(episodesList));
		animediaTitle.setCurrentMaxOnAnimedia(correctCurrentMax);
	}

	private void enrichConcretizedAndOngoingTitle(AnimediaTitle animediaTitle, List<String> episodesList) {
		String currentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		animediaTitle.setCurrentMaxOnAnimedia(currentMax);
	}
}
