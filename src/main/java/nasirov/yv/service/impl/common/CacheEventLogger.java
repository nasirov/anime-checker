package nasirov.yv.service.impl.common;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.stereotype.Component;

/**
 * @author Nasirov Yuriy
 */
@Slf4j
@Component
public class CacheEventLogger implements CacheEventListener<Object, Object> {

	@Override
	public void onEvent(CacheEvent cacheEvent) {
		log.debug("CACHE EVENT type [{}], cache key[{}], old value is null:[{}], new value is null:[{}] ",
				cacheEvent.getType(),
				cacheEvent.getKey(),
				Objects.isNull(cacheEvent.getOldValue()),
				Objects.isNull(cacheEvent.getNewValue()));
	}
}