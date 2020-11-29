package nasirov.yv.service.impl.common;

import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.front.UserInputDto;
import nasirov.yv.service.CacheCleanerServiceI;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Service
public class CacheCleanerService implements CacheCleanerServiceI {

	@Override
	@CacheEvict(cacheNames = "sse", key = "#userInputDto.getUsername()")
	public void clearSseCache(UserInputDto userInputDto) {
		log.info("Received request for eviction sse cache for [{}] by key [{}].", userInputDto, userInputDto.getUsername());
	}
}
