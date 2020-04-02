package nasirov.yv.service.impl;

import static nasirov.yv.data.constants.BaseConstants.JOINED_EPISODE_REGEXP;
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
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.parser.AnimediaEpisodeParserI;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.TitleReferenceUpdateServiceI;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TitleReferenceUpdateService implements TitleReferenceUpdateServiceI {

	private final AnimediaEpisodeParserI animediaEpisodeParser;

	private final AnimediaServiceI animediaService;

	/**
	 * Updates given references
	 *
	 * @param references references for update
	 */
	@Override
	public void updateReferences(Set<TitleReference> references) {
		references.stream()
				.filter(this::isReferenceNeedUpdate)
				.forEach(this::handleReference);
	}

	private boolean isReferenceNeedUpdate(TitleReference reference) {
		return !(isTitleUpdated(reference) || isTitleNotFoundOnMAL(reference));
	}

	private void handleReference(TitleReference reference) {
		List<String> episodesList = animediaService.getEpisodes(reference.getAnimeIdOnAnimedia(), reference.getDataListOnAnimedia());
		if (episodesList.isEmpty()) {
			return;
		}
		List<String> episodesRange = episodesList.stream()
				.map(animediaEpisodeParser::extractEpisodeNumber)
				.collect(Collectors.toList());
		if (isTitleConcretizedAndOngoing(reference)) {
			enrichConcretizedAndOngoingReference(reference, episodesRange);
		} else {
			enrichRegularReference(reference, episodesRange);
		}
	}

	private void enrichRegularReference(TitleReference reference, List<String> episodesList) {
		String correctFirstEpisodeAndMin = getCorrectFirstEpisodeAndMin(getFirstEpisode(episodesList));
		String correctCurrentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		//если в дата листах суммируют первую серию и последнюю с предыдущего дата листа, то нужна проверка для правильного максимума
		//например, всего серий ххх, 1 даталист: серии 1 из 100; 2 дата лист: серии 51 из 100
		reference.setMinOnAnimedia(correctFirstEpisodeAndMin);
		// TODO: 17.12.2019 uncomment and implement when animedia improve api object that will return max episode in season
//		reference.setMaxConcretizedEpisodeOnAnimedia("correctMaxInDataList");
		episodesList.stream()
				.filter(x -> x.matches(JOINED_EPISODE_REGEXP))
				.findFirst()
				.ifPresent(x -> reference.setEpisodesRangeOnAnimedia(episodesList));
		reference.setCurrentMaxOnAnimedia(correctCurrentMax);
	}

	private void enrichConcretizedAndOngoingReference(TitleReference reference, List<String> episodesList) {
		String currentMax = getCorrectCurrentMax(getLastEpisode(episodesList));
		reference.setCurrentMaxOnAnimedia(currentMax);
	}
}
