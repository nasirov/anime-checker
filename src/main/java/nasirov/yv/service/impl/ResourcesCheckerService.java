package nasirov.yv.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nasirov.yv.data.constants.BaseConstants.FIRST_DATA_LIST;
import static nasirov.yv.data.constants.BaseConstants.FIRST_EPISODE;
import static nasirov.yv.util.AnimediaUtils.isAnnouncement;
import static nasirov.yv.util.AnimediaUtils.isTitleNotFoundOnMAL;

import feign.template.UriUtils;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.animedia.AnimediaSearchListTitle;
import nasirov.yv.data.animedia.TitleReference;
import nasirov.yv.data.properties.GithubResources;
import nasirov.yv.data.properties.ResourcesNames;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.service.AnimediaServiceI;
import nasirov.yv.service.GithubResourcesServiceI;
import nasirov.yv.service.MALServiceI;
import nasirov.yv.service.ResourcesCheckerServiceI;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Slf4j
@Service
@Profile(value = {"local", "test"})
@RequiredArgsConstructor
public class ResourcesCheckerService implements ResourcesCheckerServiceI {

	private final GithubResourcesServiceI githubResourcesService;

	private final AnimediaServiceI animediaService;

	private final MALServiceI malService;

	private final ResourcesNames resourcesNames;

	private final WrappedObjectMapperI wrappedObjectMapper;

	private final GithubResources githubResources;

	@Override
	@Scheduled(cron = "${application.cron.resources-check-cron-expression}")
	public void checkReferencesNames() {
		log.info("START CHECKING REFERENCES NAMES ON MAL ...");
		Set<TitleReference> allReferences = githubResourcesService.getResource(githubResources.getAnimediaTitles(), TitleReference.class);
		List<TitleReference> referencesWithInvalidMALTitleName = new LinkedList<>();
		for (TitleReference reference : allReferences) {
			String titleOnMAL = reference.getTitleNameOnMAL();
			Integer titleIdOnMAL = reference.getTitleIdOnMAL();
			if (!isTitleNotFoundOnMAL(reference)) {
				boolean titleExist = malService.isTitleExist(titleOnMAL, titleIdOnMAL);
				if (!titleExist) {
					log.error("TITLE {} WITH ID {} DOESN'T EXIST!", titleOnMAL, titleIdOnMAL);
					referencesWithInvalidMALTitleName.add(TitleReference.builder()
							.urlOnAnimedia(reference.getUrlOnAnimedia())
							.animeIdOnAnimedia(reference.getAnimeIdOnAnimedia())
							.dataListOnAnimedia(reference.getDataListOnAnimedia())
							.minOnAnimedia(reference.getMinOnAnimedia())
							.titleNameOnMAL(titleOnMAL)
							.titleIdOnMAL(titleIdOnMAL)
							.build());
				}
			}
		}
		marshallToTempFolder(resourcesNames.getTempReferencesWithInvalidMALTitleName(), referencesWithInvalidMALTitleName);
		log.info("END CHECKING REFERENCES NAMES ON MAL.");
	}

	@Override
	@Scheduled(cron = "${application.cron.resources-check-cron-expression}")
	public void checkReferences() {
		log.info("START CHECKING REFERENCES ...");
		Set<AnimediaSearchListTitle> animediaSearchList = animediaService.getAnimediaSearchList();
		Set<TitleReference> allReferences = githubResourcesService.getResource(githubResources.getAnimediaTitles(), TitleReference.class);
		List<TitleReference> notFoundInReferences = new LinkedList<>();
		for (AnimediaSearchListTitle titleSearchInfo : animediaSearchList) {
			List<TitleReference> references = getMatchedReferences(allReferences, titleSearchInfo);
			if (isAnnouncement(titleSearchInfo)) {
				if (references.isEmpty()) {
					log.error("ANNOUNCEMENT MUST BE PRESENT IN ONE REFERENCE {}", titleSearchInfo);
					notFoundInReferences.add(buildTempAnnouncementReference(titleSearchInfo));
				}
			} else {
				List<String> dataLists = titleSearchInfo.getDataLists();
				for (String dataList : dataLists) {
					boolean titleIsNotPresentInReferences = references.stream()
							.noneMatch(x -> x.getDataListOnAnimedia()
									.equals(dataList));
					if (titleIsNotPresentInReferences) {
						log.error("TITLE IS NOT PRESENT IN REFERENCES {}/{}", titleSearchInfo.getUrl(), dataList);
						notFoundInReferences.add(buildTempReference(titleSearchInfo, dataList));
					}
				}
			}
		}
		marshallToTempFolder(resourcesNames.getTempRawReferences(), notFoundInReferences);
		log.info("END CHECKING REFERENCES.");
	}

	private TitleReference buildTempReference(AnimediaSearchListTitle titleSearchInfo, String dataList) {
		return TitleReference.builder()
				.urlOnAnimedia(titleSearchInfo.getUrl())
				.animeIdOnAnimedia(titleSearchInfo.getAnimeId())
				.dataListOnAnimedia(dataList)
				.build();
	}

	private TitleReference buildTempAnnouncementReference(AnimediaSearchListTitle titleSearchInfo) {
		return TitleReference.builder()
				.urlOnAnimedia(titleSearchInfo.getUrl())
				.dataListOnAnimedia(FIRST_DATA_LIST)
				.minOnAnimedia(FIRST_EPISODE)
				.build();
	}

	private List<TitleReference> getMatchedReferences(Set<TitleReference> allReference, AnimediaSearchListTitle titleSearchInfo) {
		return allReference.stream()
				.filter(x -> titleSearchInfo.getUrl()
						.equals(UriUtils.decode(x.getUrlOnAnimedia(), UTF_8)))
				.collect(Collectors.toList());
	}

	private void marshallToTempFolder(String tempFileName, Collection<?> content) {
		if (!content.isEmpty()) {
			wrappedObjectMapper.marshal(buildResultFile(tempFileName), content);
		}
	}

	private File buildResultFile(String tempFileName) {
		return new File(new File(resourcesNames.getTempFolder()), tempFileName);
	}
}
