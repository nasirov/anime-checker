package nasirov.yv.service.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nasirov.yv.data.github.GitHubResource;
import nasirov.yv.data.properties.GitHubAuthProps;
import nasirov.yv.data.properties.GitHubResourceProps;
import nasirov.yv.http.feign.GitHubFeignClient;
import nasirov.yv.parser.WrappedObjectMapperI;
import nasirov.yv.service.GitHubResourcesServiceI;
import org.springframework.stereotype.Service;

/**
 * Created by nasirov.yv
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GitHubResourcesService implements GitHubResourcesServiceI {

	private final GitHubFeignClient gitHubFeignClient;

	private final GitHubAuthProps gitHubAuthProps;

	private final WrappedObjectMapperI wrappedObjectMapper;

	/**
	 * Searches for a resource from GitHub
	 *
	 * @param resourceName a github resource name from {@link GitHubResourceProps}
	 * @param targetClass  a subclass dto for deserialization
	 * @param <T>          type of deserialization class
	 * @return set with dtos
	 */
	@Override
	public <T extends GitHubResource> Set<T> getResource(String resourceName, Class<T> targetClass) {
		log.debug("Trying to get resource [{}] for class [{}] from GitHub...", resourceName, targetClass);
		String resourceString = gitHubFeignClient.getResource("token " + gitHubAuthProps.getToken(), resourceName);
		Set<T> result = wrappedObjectMapper.unmarshalToSet(resourceString, targetClass, LinkedHashSet.class);
		log.debug("Got result with size [{}] for class [{}] from GitHub.", result.size(), targetClass);
		return result;
	}
}
